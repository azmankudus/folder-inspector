import { createSignal, createResource, Show, For } from "solid-js";
import { useNavigate } from "@solidjs/router";
import { FaSolidFolderTree, FaSolidPlus, FaSolidPen, FaSolidTrash, FaSolidSpinner, FaSolidCheck, FaSolidXmark } from "solid-icons/fa";
import { api, getToken } from "../lib/api";
import AppLayout from "../components/AppLayout";

type DirectoryProfile = { id: number, adHost: string, adPort: number, adUsername: string, adPassword?: string };

const fetchDirectories = async () => api.get("/directories");

export default function DirectoryProfiles() {
  const navigate = useNavigate();
  if (globalThis.window !== undefined && !getToken()) {
    setTimeout(() => navigate("/login", { replace: true }), 0);
  }

  const [directories, { refetch }] = createResource(
    () => (globalThis.window !== undefined && getToken() ? "fetch" : null),
    fetchDirectories
  );

  const [newDirHost, setNewDirHost] = createSignal("");
  const [newDirPort, setNewDirPort] = createSignal("389");
  const [newDirUser, setNewDirUser] = createSignal("");
  const [newDirPass, setNewDirPass] = createSignal("");

  const handleAddDirectory = async () => {
    if (!newDirHost() || !newDirUser()) return alert("Fill required fields");
    try {
      await api.post("/directories", {
        adHost: newDirHost(), adPort: Number(newDirPort()), adUsername: newDirUser(), adPassword: newDirPass()
      });
      setNewDirHost(""); setNewDirUser(""); setNewDirPass("");
      refetch();
    } catch (e: any) { alert(e.message); }
  };

  const handleDeleteDirectory = async (id: number) => {
    if (confirm("Delete Directory?")) {
      try { await api.delete(`/directories/${id}`); refetch(); } catch (e: any) { alert(e.message); }
    }
  };

  const [editingId, setEditingId] = createSignal<number | null>(null);
  const [editFormData, setEditFormData] = createSignal<Partial<DirectoryProfile>>({});

  const handleEditClick = (dir: DirectoryProfile) => {
    setEditingId(dir.id);
    setEditFormData({ ...dir, adPassword: "" });
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditFormData({});
  };

  const handleSaveEdit = async () => {
    const id = editingId();
    if (!id) return;
    try {
      await api.put(`/directories/${id}`, editFormData());
      setEditingId(null);
      refetch();
    } catch (e: any) { alert(e.message); }
  };

  // Pagination
  const [page, setPage] = createSignal(1);
  const rowsPerPage = 10;
  const paginatedData = () => {
    const data = directories();
    if (!data) return [];
    return data.slice((page() - 1) * rowsPerPage, page() * rowsPerPage);
  };
  const totalPages = () => Math.ceil((directories()?.length || 0) / rowsPerPage);

  return (
    <AppLayout title="Directory Profiles">
      <div class="bg-[#111111] border border-zinc-800 rounded-lg shadow-sm overflow-hidden flex flex-col min-h-[500px]">
        <div class="p-4 border-b border-zinc-800 bg-[#161616]">
          <h2 class="text-sm font-semibold text-zinc-100 flex items-center gap-2">
            <FaSolidFolderTree class="text-purple-500" /> Identity Providers (AD/LDAP)
          </h2>
        </div>
        <div class="p-4 border-b border-zinc-800/80 bg-[#111111] grid grid-cols-1 md:grid-cols-5 gap-3">
          <input type="text" placeholder="LDAP Host" value={newDirHost()} onInput={e => setNewDirHost(e.currentTarget.value)} class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm" />
          <input type="text" placeholder="Port (389)" value={newDirPort()} onInput={e => setNewDirPort(e.currentTarget.value)} class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm" />
          <input type="text" placeholder="Domain Username" value={newDirUser()} onInput={e => setNewDirUser(e.currentTarget.value)} class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm" />
          <input type="password" placeholder="Password" value={newDirPass()} onInput={e => setNewDirPass(e.currentTarget.value)} class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm" />
          <button class="bg-purple-600 hover:bg-purple-500 text-white rounded text-sm font-medium flex items-center justify-center gap-2 transition-colors" onClick={handleAddDirectory}><FaSolidPlus /> Add Provider</button>
        </div>
        
        <div class="overflow-x-auto flex-1 bg-[#111111]">
          <table class="w-full text-left text-sm whitespace-nowrap">
            <thead class="text-xs text-zinc-500 uppercase bg-[#18181A] border-b border-zinc-800">
              <tr>
                <th class="px-5 py-3 font-medium">Server Host (DC/LDAP)</th>
                <th class="px-5 py-3 font-medium">Port</th>
                <th class="px-5 py-3 font-medium">Domain Username</th>
                <th class="px-5 py-3 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-zinc-800/80">
              <Show when={directories()} fallback={<tr><td colspan="4" class="px-5 py-8 text-center text-zinc-500"><FaSolidSpinner class="animate-spin text-purple-500 mx-auto" /></td></tr>}>
                <For each={paginatedData()}>
                  {(d) => (
                    <tr class="hover:bg-[#1a1a1c] transition-colors">
                      <Show when={editingId() === d.id} fallback={
                        <>
                          <td class="px-5 py-3 text-zinc-200 font-mono font-medium flex items-center gap-2">
                            <span class="w-2 h-2 rounded-full bg-purple-500"></span>{d.adHost}
                          </td>
                          <td class="px-5 py-3 text-zinc-400 font-mono text-xs">{d.adPort}</td>
                          <td class="px-5 py-3 text-zinc-400">{d.adUsername}</td>
                          <td class="px-5 py-3 text-right">
                            <button class="text-zinc-500 hover:text-blue-400 p-2 border border-transparent hover:bg-zinc-800 rounded transition-colors" onClick={() => handleEditClick(d)} title="Edit"><FaSolidPen /></button>
                            <button class="text-zinc-500 hover:text-red-400 p-2 border border-transparent hover:bg-zinc-800 rounded transition-colors ml-1" onClick={() => handleDeleteDirectory(d.id)} title="Delete"><FaSolidTrash /></button>
                          </td>
                        </>
                      }>
                        <td class="px-5 py-3">
                            <input type="text" value={editFormData().adHost || ""} onInput={e => setEditFormData({...editFormData(), adHost: e.currentTarget.value})} class="bg-[#161616] border border-zinc-700 rounded px-2 py-1 text-zinc-200 text-xs w-full outline-none focus:border-purple-500" />
                        </td>
                        <td class="px-5 py-3">
                            <input type="number" value={editFormData().adPort || 389} onInput={e => setEditFormData({...editFormData(), adPort: Number(e.currentTarget.value)})} class="bg-[#161616] border border-zinc-700 rounded px-2 py-1 text-zinc-200 text-xs w-full outline-none focus:border-purple-500" />
                        </td>
                        <td class="px-5 py-3 flex items-center gap-2 mt-1">
                            <input type="text" value={editFormData().adUsername || ""} onInput={e => setEditFormData({...editFormData(), adUsername: e.currentTarget.value})} class="bg-[#161616] border border-zinc-700 rounded px-2 py-1 text-zinc-200 text-xs w-1/2 min-w-[100px] outline-none focus:border-purple-500 placeholder-zinc-500" placeholder="Username" />
                            <input type="password" value={editFormData().adPassword || ""} onInput={e => setEditFormData({...editFormData(), adPassword: e.currentTarget.value})} class="bg-[#161616] border border-zinc-700 rounded px-2 py-1 text-zinc-200 text-xs w-1/2 min-w-[100px] outline-none focus:border-purple-500 placeholder-zinc-500" placeholder="New Pwd (Opt)" />
                        </td>
                        <td class="px-5 py-3 text-right">
                            <button class="text-emerald-500 hover:text-emerald-400 p-2 border border-emerald-500/20 bg-emerald-500/10 hover:bg-emerald-500/20 rounded transition-colors" onClick={handleSaveEdit} title="Save"><FaSolidCheck /></button>
                            <button class="text-zinc-500 hover:text-red-400 p-2 border border-transparent hover:bg-zinc-800 rounded transition-colors ml-1" onClick={cancelEdit} title="Cancel"><FaSolidXmark /></button>
                        </td>
                      </Show>
                    </tr>
                  )}
                </For>
                {paginatedData().length === 0 && <tr><td colspan="4" class="py-10 text-center text-zinc-600">No LDAP connectivity registered.</td></tr>}
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
