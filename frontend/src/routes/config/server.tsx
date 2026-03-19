import { createSignal, createResource, Show, For } from "solid-js";
import { useNavigate } from "@solidjs/router";
import { FaSolidNetworkWired, FaSolidPlus, FaSolidPen, FaSolidTrash, FaSolidSpinner, FaSolidCheck, FaSolidXmark } from "solid-icons/fa";
import { api, getToken } from "~/lib/api";
import AppLayout from "~/components/AppLayout";

type ConnectionProfile = { id: number, protocol: string, host: string, port: number, username: string, password?: string };

const fetchConnections = async () => api.get("/config/server");

export default function Connections() {
  const navigate = useNavigate();
  if (globalThis.window !== undefined && !getToken()) {
    setTimeout(() => navigate("/access/login", { replace: true }), 0);
  }

  const [connections, { refetch }] = createResource(
    () => (globalThis.window !== undefined && getToken() ? "fetch" : null),
    fetchConnections
  );

  const [newConnType, setNewConnType] = createSignal("SMBv3");
  const [newConnHost, setNewConnHost] = createSignal("");
  const [newConnPort, setNewConnPort] = createSignal("445");
  const [newConnUser, setNewConnUser] = createSignal("");
  const [newConnPass, setNewConnPass] = createSignal("");

  const handleAddConnection = async () => {
    if (!newConnHost() || !newConnUser() || !newConnPass()) return alert("Fill required fields");
    try {
      await api.post("/config/server", {
        protocol: newConnType(), host: newConnHost(), port: Number(newConnPort()), username: newConnUser(), password: newConnPass()
      });
      setNewConnHost(""); setNewConnUser(""); setNewConnPass("");
      refetch();
    } catch (e: any) { alert(e.message); }
  };

  const handleDeleteConnection = async (id: number) => {
    if (confirm("Delete connection?")) {
      try { await api.delete(`/config/server/${id}`); refetch(); } catch (e: any) { alert(e.message); }
    }
  };

  const [editingId, setEditingId] = createSignal<number | null>(null);
  const [editFormData, setEditFormData] = createSignal<Partial<ConnectionProfile>>({});

  const handleEditClick = (conn: ConnectionProfile) => {
    setEditingId(conn.id);
    setEditFormData({ ...conn, password: "" });
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditFormData({});
  };

  const handleSaveEdit = async () => {
    const id = editingId();
    if (!id) return;
    try {
      await api.put(`/config/server/${id}`, editFormData());
      setEditingId(null);
      refetch();
    } catch (e: any) { alert(e.message); }
  };

  // Pagination logic
  const [page, setPage] = createSignal(1);
  const rowsPerPage = 10;
  const paginatedData = () => {
    const data = connections();
    if (!data) return [];
    return data.slice((page() - 1) * rowsPerPage, page() * rowsPerPage);
  };
  const totalPages = () => Math.ceil((connections()?.length || 0) / rowsPerPage);

  return (
    <AppLayout title="Connection Profiles">
      <div class="bg-[#111111] border border-zinc-800 rounded-lg shadow-sm overflow-hidden flex flex-col min-h-[500px]">
        <div class="p-4 border-b border-zinc-800 bg-[#161616]">
          <h2 class="text-sm font-semibold text-zinc-100 flex items-center gap-2">
            <FaSolidNetworkWired class="text-blue-500" /> Server Connections
          </h2>
        </div>
        <div class="p-4 border-b border-zinc-800/80 bg-[#111111] grid grid-cols-1 md:grid-cols-6 gap-3">
          <input type="text" placeholder="Method (SMBv3)" value={newConnType()} onInput={e => setNewConnType(e.currentTarget.value)} class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm" />
          <input type="text" placeholder="Address (Host/IP)" value={newConnHost()} onInput={e => setNewConnHost(e.currentTarget.value)} class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm" />
          <input type="text" placeholder="Port (445)" value={newConnPort()} onInput={e => setNewConnPort(e.currentTarget.value)} class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm" />
          <input type="text" placeholder="Username" value={newConnUser()} onInput={e => setNewConnUser(e.currentTarget.value)} class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm" />
          <input type="password" placeholder="Password" value={newConnPass()} onInput={e => setNewConnPass(e.currentTarget.value)} class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm" />
          <button class="bg-blue-600 hover:bg-blue-500 text-white rounded text-sm font-medium flex items-center justify-center gap-2 transition-colors" onClick={handleAddConnection}><FaSolidPlus /> Add Server</button>
        </div>
        
        <div class="overflow-x-auto flex-1 bg-[#111111]">
          <table class="w-full text-left text-sm whitespace-nowrap">
            <thead class="text-xs text-zinc-500 uppercase bg-[#18181A] border-b border-zinc-800">
              <tr>
                <th class="px-5 py-3 font-medium">Server Host</th>
                <th class="px-5 py-3 font-medium">Method</th>
                <th class="px-5 py-3 font-medium">Port</th>
                <th class="px-5 py-3 font-medium">Username</th>
                <th class="px-5 py-3 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-zinc-800/80">
              <Show when={connections()} fallback={<tr><td colspan="5" class="px-5 py-8 text-center text-zinc-500"><FaSolidSpinner class="animate-spin text-blue-500 mx-auto" /></td></tr>}>
                <For each={paginatedData()}>
                  {(c) => (
                    <tr class="hover:bg-[#1a1a1c] transition-colors">
                      <Show when={editingId() === c.id} fallback={
                        <>
                          <td class="px-5 py-3 text-zinc-200 font-mono font-medium flex items-center gap-2">
                            <span class="w-2 h-2 rounded-full bg-blue-500"></span>{c.host}
                          </td>
                          <td class="px-5 py-3 text-zinc-400">{c.protocol}</td>
                          <td class="px-5 py-3 text-zinc-400 font-mono text-xs">{c.port}</td>
                          <td class="px-5 py-3 text-zinc-400">{c.username}</td>
                          <td class="px-5 py-3 text-right">
                            <button class="text-zinc-500 hover:text-blue-400 p-2 border border-transparent hover:bg-zinc-800 rounded transition-colors" onClick={() => handleEditClick(c)} title="Edit"><FaSolidPen /></button>
                            <button class="text-zinc-500 hover:text-red-400 p-2 border border-transparent hover:bg-zinc-800 rounded transition-colors ml-1" onClick={() => handleDeleteConnection(c.id)} title="Delete"><FaSolidTrash /></button>
                          </td>
                        </>
                      }>
                        <td class="px-5 py-3">
                            <input type="text" value={editFormData().host || ""} onInput={e => setEditFormData({...editFormData(), host: e.currentTarget.value})} class="bg-[#161616] border border-zinc-700 rounded px-2 py-1 text-zinc-200 text-xs w-full" />
                        </td>
                        <td class="px-5 py-3">
                            <input type="text" value={editFormData().protocol || ""} onInput={e => setEditFormData({...editFormData(), protocol: e.currentTarget.value})} class="bg-[#161616] border border-zinc-700 rounded px-2 py-1 text-zinc-200 text-xs w-full outline-none focus:border-blue-500" />
                        </td>
                        <td class="px-5 py-3">
                            <input type="number" value={editFormData().port || 445} onInput={e => setEditFormData({...editFormData(), port: Number(e.currentTarget.value)})} class="bg-[#161616] border border-zinc-700 rounded px-2 py-1 text-zinc-200 text-xs w-full outline-none focus:border-blue-500" />
                        </td>
                        <td class="px-5 py-3 flex items-center gap-2 mt-1">
                            <input type="text" value={editFormData().username || ""} onInput={e => setEditFormData({...editFormData(), username: e.currentTarget.value})} class="bg-[#161616] border border-zinc-700 rounded px-2 py-1 text-zinc-200 text-xs w-1/2 min-w-[100px] outline-none focus:border-blue-500 placeholder-zinc-500" placeholder="Username" />
                            <input type="password" value={editFormData().password || ""} onInput={e => setEditFormData({...editFormData(), password: e.currentTarget.value})} class="bg-[#161616] border border-zinc-700 rounded px-2 py-1 text-zinc-200 text-xs w-1/2 min-w-[100px] outline-none focus:border-blue-500 placeholder-zinc-500" placeholder="New Pwd (Opt)" />
                        </td>
                        <td class="px-5 py-3 text-right">
                            <button class="text-emerald-500 hover:text-emerald-400 p-2 border border-emerald-500/20 bg-emerald-500/10 hover:bg-emerald-500/20 rounded transition-colors" onClick={handleSaveEdit} title="Save"><FaSolidCheck /></button>
                            <button class="text-zinc-500 hover:text-red-400 p-2 border border-transparent hover:bg-zinc-800 rounded transition-colors ml-1" onClick={cancelEdit} title="Cancel"><FaSolidXmark /></button>
                        </td>
                      </Show>
                    </tr>
                  )}
                </For>
                {paginatedData().length === 0 && <tr><td colspan="5" class="py-10 text-center text-zinc-600">No active connections configured.</td></tr>}
              </Show>
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        <div class="p-3 border-t border-zinc-800 bg-[#161616] flex items-center justify-between text-xs text-zinc-500">
          <span>Showing Page {page()} of {totalPages() || 1}</span>
          <div class="flex items-center gap-1">
            <button class="px-3 py-1 rounded border border-zinc-700 hover:bg-zinc-800 hover:text-zinc-300 transition-colors disabled:opacity-50" onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page() === 1}>Prev</button>
            <button class="px-3 py-1 rounded border border-zinc-700 hover:bg-zinc-800 hover:text-zinc-300 transition-colors disabled:opacity-50" onClick={() => setPage(p => Math.min(totalPages(), p + 1))} disabled={page() >= totalPages()}>Next</button>
          </div>
        </div>

      </div>
    </AppLayout>
  );
}
