import { createSignal, createResource, Show, For } from "solid-js";
import { useNavigate } from "@solidjs/router";
import { FaSolidSatelliteDish, FaSolidPlus, FaSolidPen, FaSolidTrash, FaSolidSpinner, FaSolidPlay, FaSolidStop, FaSolidCheck, FaSolidXmark } from "solid-icons/fa";
import { api, getToken } from "../lib/api";
import AppLayout from "../components/AppLayout";

type ConnectionProfile = { id: number, protocol: string, host: string, port: number, username: string };
type ScanProfile = { id: number, connectionProfileId: number, rootPath: string, schedulerCron: string, scanMode: 'FULL' | 'AUDIT' };

const fetchConnections = async () => api.get("/connections");
const fetchScans = async () => api.get("/scans");

export default function ScanProfiles() {
  const navigate = useNavigate();
  if (globalThis.window !== undefined && !getToken()) {
    setTimeout(() => navigate("/login", { replace: true }), 0);
  }

  const [connections] = createResource(
    () => (globalThis.window !== undefined && getToken() ? "fetch" : null),
    fetchConnections
  );
  const [scans, { refetch }] = createResource(
    () => (globalThis.window !== undefined && getToken() ? "fetch" : null),
    fetchScans
  );

  const [newScanConn, setNewScanConn] = createSignal<number | "">("");
  const [newScanRoot, setNewScanRoot] = createSignal("");
  const [newScanCron, setNewScanCron] = createSignal("0 0 * * *");
  const [newScanMode, setNewScanMode] = createSignal<'FULL' | 'AUDIT'>("AUDIT");
  
  const handleAddScan = async () => {
    if (!newScanConn() || !newScanRoot() || !newScanCron()) return alert("Fill all fields");
    try {
      await api.post("/scans", {
        connectionProfileId: Number(newScanConn()), 
        rootPath: newScanRoot(), 
        schedulerCron: newScanCron(),
        scanMode: newScanMode()
      });
      setNewScanConn(""); setNewScanRoot("");
      refetch();
    } catch (e: any) { alert(e.message); }
  };

  const handleDeleteScan = async (id: number) => {
    if (confirm("Delete scan profile?")) {
      try { await api.delete(`/scans/${id}`); refetch(); } catch (e: any) { alert(e.message); }
    }
  };

  const handleStartScan = async (id: number) => {
    try {
      await api.post(`/jobs/${id}/start`, {});
      alert("Scan started contextually.");
    } catch (e: any) { alert("Failed to start scan: " + e.message); }
  };

  const handleStopScan = async (id: number) => {
    try {
      await api.post(`/jobs/${id}/stop`, {});
      alert("Stop signal sent.");
    } catch (e: any) { alert("Failed to stop scan: " + e.message); }
  };

  const [editingId, setEditingId] = createSignal<number | null>(null);
  const [editFormData, setEditFormData] = createSignal<Partial<ScanProfile>>({});

  const handleEditClick = (scan: ScanProfile) => {
    setEditingId(scan.id);
    setEditFormData({ ...scan });
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditFormData({});
  };

  const handleSaveEdit = async () => {
    const id = editingId();
    if (!id) return;
    try {
      await api.put(`/scans/${id}`, editFormData());
      setEditingId(null);
      refetch();
    } catch (e: any) { alert(e.message); }
  };

  // Pagination
  const [page, setPage] = createSignal(1);
  const rowsPerPage = 10;
  const paginatedData = () => {
    const data = scans();
    if (!data) return [];
    return data.slice((page() - 1) * rowsPerPage, page() * rowsPerPage);
  };
  const totalPages = () => Math.ceil((scans()?.length || 0) / rowsPerPage);

  return (
    <AppLayout title="Scan Profiles">
      <div class="bg-[#111111] border border-zinc-800 rounded-lg shadow-sm overflow-hidden flex flex-col min-h-[500px]">
        <div class="p-4 border-b border-zinc-800 bg-[#161616]">
          <h2 class="text-sm font-semibold text-zinc-100 flex items-center gap-2">
            <FaSolidSatelliteDish class="text-emerald-500" /> Define Crawler Targets
          </h2>
        </div>
        <div class="p-4 border-b border-zinc-800/80 bg-[#111111] grid grid-cols-1 md:grid-cols-5 gap-3">
          <select value={newScanConn()} onChange={e => setNewScanConn(Number(e.currentTarget.value))} class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm">
            <option value="">Connection Link...</option>
            <Show when={connections()}><For each={connections()}>{(c) => <option value={c.id}>Profile #{c.id} - {c.host}</option>}</For></Show>
          </select>
          <input type="text" placeholder="Root Directory (\Shared)" value={newScanRoot()} onInput={e => setNewScanRoot(e.currentTarget.value)} class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm" />
          <input type="text" placeholder="Schedule CRON" value={newScanCron()} onInput={e => setNewScanCron(e.currentTarget.value)} class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm font-mono" />
          <select value={newScanMode()} onChange={e => setNewScanMode(e.currentTarget.value as 'FULL' | 'AUDIT')} class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm">
            <option value="AUDIT">Audit Mode (Lean)</option>
            <option value="FULL">Full Scan (Heavy)</option>
          </select>
          <button class="bg-emerald-600 hover:bg-emerald-500 text-white rounded text-sm font-medium flex items-center justify-center gap-2 transition-colors" onClick={handleAddScan}><FaSolidPlus /> Add Profile</button>
        </div>
        
        <div class="overflow-x-auto flex-1 bg-[#111111]">
          <table class="w-full text-left text-sm whitespace-nowrap">
            <thead class="text-xs text-zinc-500 uppercase bg-[#18181A] border-b border-zinc-800">
              <tr>
                <th class="px-5 py-3 font-medium">Link Profile ID</th>
                <th class="px-5 py-3 font-medium">Share Root Path</th>
                <th class="px-5 py-3 font-medium">Schedule (Cron)</th>
                <th class="px-5 py-3 font-medium">Mode</th>
                <th class="px-5 py-3 font-medium text-right">Execution Controls</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-zinc-800/80">
              <Show when={scans()} fallback={<tr><td colspan="5" class="px-5 py-8 text-center text-zinc-500"><FaSolidSpinner class="animate-spin text-emerald-500 mx-auto" /></td></tr>}>
                <For each={paginatedData()}>
                  {(s) => (
                    <tr class="hover:bg-[#1a1a1c] transition-colors">
                      <Show when={editingId() === s.id} fallback={
                        <>
                          <td class="px-5 py-3 text-emerald-400 font-mono text-xs font-semibold">#{s.connectionProfileId}</td>
                          <td class="px-5 py-3 text-zinc-200 font-mono text-[13px]">{s.rootPath}</td>
                          <td class="px-5 py-3 text-zinc-400 font-mono text-xs">{s.schedulerCron}</td>
                          <td class="px-5 py-3 text-zinc-500 font-mono text-[10px] uppercase tracking-tighter">
                             <span class={s.scanMode === 'AUDIT' ? "text-emerald-500/80" : "text-amber-500/80"}>{s.scanMode || 'FULL'}</span>
                          </td>
                          <td class="px-5 py-3 text-right">
                            <button class="text-zinc-500 hover:text-blue-400 p-2 border border-transparent hover:bg-zinc-800 rounded transition-colors" onClick={() => handleEditClick(s)} title="Edit Configuration"><FaSolidPen /></button>
                            <span class="border-l border-zinc-700 mx-1"></span>
                            <button class="text-emerald-400 hover:text-emerald-300 px-3 py-1.5 mx-1 bg-emerald-500/10 hover:bg-emerald-500/20 rounded transition-colors text-xs inline-flex items-center gap-2 border border-emerald-500/20" onClick={() => handleStartScan(s.id)}><FaSolidPlay /> Invoke</button>
                            <button class="text-amber-400 hover:text-amber-300 px-3 py-1.5 mx-1 bg-amber-500/10 hover:bg-amber-500/20 rounded transition-colors text-xs inline-flex items-center gap-2 border border-amber-500/20" onClick={() => handleStopScan(s.id)}><FaSolidStop /> Halt</button>
                            <span class="border-l border-zinc-700 mx-1"></span>
                            <button class="text-zinc-500 hover:text-red-400 p-2 border border-transparent hover:bg-zinc-800 rounded transition-colors ml-1" onClick={() => handleDeleteScan(s.id)} title="Delete"><FaSolidTrash /></button>
                          </td>
                        </>
                      }>
                        <td class="px-5 py-3">
                          <select value={editFormData().connectionProfileId || ""} onChange={e => setEditFormData({...editFormData(), connectionProfileId: Number(e.currentTarget.value)})} class="bg-[#161616] border border-zinc-700 rounded px-2 py-1 text-zinc-200 text-xs w-full outline-none focus:border-emerald-500">
                            <Show when={connections()}><For each={connections()}>{(c) => <option value={c.id}>Profile #{c.id}</option>}</For></Show>
                          </select>
                        </td>
                        <td class="px-5 py-3">
                            <input type="text" value={editFormData().rootPath || ""} onInput={e => setEditFormData({...editFormData(), rootPath: e.currentTarget.value})} class="bg-[#161616] border border-zinc-700 rounded px-2 py-1 text-zinc-200 text-xs w-full outline-none focus:border-emerald-500" />
                        </td>
                        <td class="px-5 py-3">
                            <input type="text" value={editFormData().schedulerCron || ""} onInput={e => setEditFormData({...editFormData(), schedulerCron: e.currentTarget.value})} class="bg-[#161616] border border-zinc-700 rounded px-2 py-1 text-zinc-200 text-xs w-full font-mono outline-none focus:border-emerald-500" />
                        </td>
                        <td class="px-5 py-3">
                          <select value={editFormData().scanMode || "AUDIT"} onChange={e => setEditFormData({...editFormData(), scanMode: e.currentTarget.value as 'FULL' | 'AUDIT'})} class="bg-[#161616] border border-zinc-700 rounded px-2 py-1 text-zinc-200 text-xs w-full outline-none focus:border-emerald-500">
                             <option value="AUDIT">AUDIT</option>
                             <option value="FULL">FULL</option>
                          </select>
                        </td>
                        <td class="px-5 py-3 text-right">
                            <button class="text-emerald-500 hover:text-emerald-400 p-2 border border-emerald-500/20 bg-emerald-500/10 hover:bg-emerald-500/20 rounded transition-colors" onClick={handleSaveEdit} title="Save Changes"><FaSolidCheck /></button>
                            <button class="text-zinc-500 hover:text-red-400 p-2 border border-transparent hover:bg-zinc-800 rounded transition-colors ml-1" onClick={cancelEdit} title="Cancel"><FaSolidXmark /></button>
                        </td>
                      </Show>
                    </tr>
                  )}
                </For>
                {paginatedData().length === 0 && <tr><td colspan="5" class="py-10 text-center text-zinc-600">No scan templates configured.</td></tr>}
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
