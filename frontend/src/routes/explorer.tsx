import { createSignal, createResource, Show, For } from "solid-js";
import { useNavigate } from "@solidjs/router";
import { FaSolidFolder, FaSolidFile, FaSolidLock, FaSolidTimes, FaSolidSpinner, FaSolidInfoCircle, FaSolidChevronDown } from "solid-icons/fa";
import { api, getToken } from "~/lib/api";
import { store } from "~/lib/store";
import AppLayout from "~/components/AppLayout";

type ScanHistory = { id: number, scanConfigId: number, status: string, startTime: string, hostname: string, rootPath: string };
type FileNode = { id: number, name: string, path: string, nodeType: string, sizeBytes: number, lastModified: string, ownerSid: string, groupSid: string, ownerName: string, groupName: string };
type Page<T> = { content: T[], totalSize: number, totalPages: number, numberOfElements: number, size: number, pageNumber: number };
type FileAcl = { id: number, itemId: number, principal: string, inheritanceType: string, canView: boolean, canAdd: boolean, canEdit: boolean, canRemove: boolean };

const fetchHistories = async () => api.get("/scan/job");

export default function FilesView() {
  const navigate = useNavigate();
  if (globalThis.window !== undefined && !getToken()) {
    setTimeout(() => navigate("/access/login", { replace: true }), 0);
  }

  const [histories] = createResource(
    () => (globalThis.window !== undefined && getToken() ? "fetch" : null),
    fetchHistories
  );

  const [selectedHistory, setSelectedHistory] = createSignal<number | null>(null);
  const selectedHistoryData = () => histories()?.find((h: ScanHistory) => h.id === selectedHistory());

  const [page, setPage] = createSignal(0);
  const [rowsPerPage] = createSignal(20);
  const [showAll, setShowAll] = createSignal(false);

  const [files] = createResource(
    () => {
      const h = selectedHistory();
      if (h === null) return null;
      return { historyId: h, page: page(), size: rowsPerPage(), all: showAll() };
    },
    async ({ historyId, page, size, all }) => {
      return api.get(`/explorer/${historyId}?page=${page}&size=${size}&showAll=${all}`) as Promise<Page<FileNode>>;
    }
  );

  const [selectedFileForAcl, setSelectedFileForAcl] = createSignal<number | null>(null);
  const [acls] = createResource(() => selectedFileForAcl(), async (fileId) => {
    return api.get(`/explorer/${fileId}/acl`);
  });

  const totalPages = () => files()?.totalPages || 0;
  const totalElements = () => files()?.totalSize || 0;

  const handleExport = async (format: 'csv' | 'xlsx', scope: 'all' | 'page') => {
    const historyId = selectedHistory();
    if (!historyId) return alert("Select a scan first");

    try {
      const endpoint = `/explorer/${historyId}/export/${format}?scope=${scope}&page=${page()}&size=${rowsPerPage()}&showAll=${showAll()}`;
      const blob = await api.download(endpoint);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `scan_results_${historyId}.${format}`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (e: any) {
      alert("Export failed: " + e.message);
    }
  };

  const overrideHistorySelect = (id: number) => {
    setPage(0);
    setSelectedHistory(id);
  }

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return "-";
    return new Date(dateStr).toLocaleString();
  };

  return (
    <AppLayout title="Explorer">
      <div class="flex flex-col gap-6">

        {/* Top Context Selector */}
        <div class="flex flex-col gap-4">
          <div class="flex items-center gap-4 w-full">
            <span class="text-sm font-semibold text-zinc-300 whitespace-nowrap">Select Scan:</span>
            <select
              onChange={e => overrideHistorySelect(Number(e.currentTarget.value))}
              class="bg-[#161616] border border-zinc-700 rounded-lg px-4 py-2 text-zinc-200 text-sm w-full focus:border-amber-500 outline-none transition-all"
            >
              <option value="">-- Choose a past scan --</option>
              <Show when={histories()}>
                <For each={[...histories()].sort((a, b) => b.id - a.id)}>
                  {h => <option value={h.id}>{h.id} - {h.hostname} - {h.rootPath}</option>}
                </For>
              </Show>
            </select>
          </div>

          <Show when={selectedHistoryData()}>
            <div class="flex items-center gap-6 mt-2 p-3 bg-zinc-900/50 rounded-lg border border-zinc-800 animate-in fade-in slide-in-from-top-2 duration-300">
              <div class="flex items-center gap-2">
                <FaSolidInfoCircle class="text-amber-500 text-sm" />
                <span class="text-xs font-bold text-zinc-500 uppercase tracking-widest leading-none">Scan Details:</span>
              </div>
              <div class="flex gap-8">
                <div class="flex flex-col gap-0.5">
                  <span class="text-[9px] text-zinc-500 font-bold uppercase tracking-wider">Storage Host</span>
                  <span class="text-sm text-zinc-200 font-medium">{selectedHistoryData()?.hostname || 'N/A'}</span>
                </div>
                <div class="flex flex-col gap-0.5">
                  <span class="text-[9px] text-zinc-500 font-bold uppercase tracking-wider">Target Path</span>
                  <span class="text-sm text-amber-500 font-mono font-medium">{selectedHistoryData()?.rootPath || 'N/A'}</span>
                </div>
              </div>
            </div>
          </Show>
        </div>

        {/* Files Data Table */}
        <div class="bg-[#111111] border border-zinc-800 rounded-lg shadow-sm overflow-hidden flex flex-col min-h-[500px]">
          <div class="p-4 border-b border-zinc-800 flex items-center justify-between bg-[#161616]">
            <h2 class="text-sm font-semibold text-zinc-100 flex items-center gap-2">
              <FaSolidFolder class="text-zinc-400" /> Discovered Files & Folders
            </h2>
            <div class="flex items-center gap-4">
              <Show when={store.state.user?.roles.includes('UI_EXPLORER_TOGGLE')}>
                <div class="flex items-center gap-3 bg-zinc-900/40 border border-zinc-800 px-3 py-1.5 rounded-lg">
                  <label class="relative inline-flex items-center cursor-pointer scale-90">
                    <input
                      type="checkbox"
                      class="sr-only peer"
                      checked={showAll()}
                      onChange={e => { setShowAll(e.currentTarget.checked); setPage(0); }}
                    />
                    <div class="w-9 h-5 bg-zinc-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-amber-500"></div>
                    <span class="ms-3 text-xs font-medium text-zinc-400 whitespace-nowrap">View All Items</span>
                  </label>
                </div>
              </Show>
              <div class="relative group/export">
                <button class="text-xs px-3 py-1.5 rounded bg-zinc-800 text-zinc-300 hover:bg-zinc-700 transition-colors border border-zinc-700 flex items-center gap-2 pr-2.5">
                  <FaSolidFile class="text-zinc-500" /> Export Data <FaSolidChevronDown class="text-[10px] text-zinc-500 ml-1 group-hover/export:rotate-180 transition-transform" />
                </button>
                <div class="absolute right-0 top-full mt-1 w-48 bg-[#161616] border border-zinc-700 rounded shadow-xl hidden group-hover/export:block z-50">
                  <div class="p-1">
                    <div class="text-[10px] text-zinc-500 px-2 py-1 uppercase font-bold tracking-wider">Current Page</div>
                    <button onClick={() => handleExport('csv', 'page')} class="w-full text-left px-3 py-1.5 text-xs text-zinc-300 hover:bg-zinc-800 hover:text-white rounded">CSV Format</button>
                    <button onClick={() => handleExport('xlsx', 'page')} class="w-full text-left px-3 py-1.5 text-xs text-zinc-300 hover:bg-zinc-800 hover:text-white rounded">Excel (XLSX)</button>
                    <div class="h-px bg-zinc-800 my-1"></div>
                    <div class="text-[10px] text-zinc-500 px-2 py-1 uppercase font-bold tracking-wider">Full Dataset</div>
                    <button onClick={() => handleExport('csv', 'all')} class="w-full text-left px-3 py-1.5 text-xs text-zinc-300 hover:bg-zinc-800 hover:text-white rounded">CSV Format</button>
                    <button onClick={() => handleExport('xlsx', 'all')} class="w-full text-left px-3 py-1.5 text-xs text-zinc-300 hover:bg-zinc-800 hover:text-white rounded">Excel (XLSX)</button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="overflow-x-auto flex-1 bg-[#111111]">
            <table class="w-full text-left text-sm whitespace-nowrap">
              <thead class="text-xs text-zinc-500 uppercase bg-[#18181A] border-b border-zinc-800">
                <tr>
                  <th class="px-5 py-3 font-medium">Name / Path</th>
                  <th class="px-5 py-3 font-medium text-center">Type</th>
                  <th class="px-5 py-3 font-medium">Size</th>
                  <th class="px-5 py-3 font-medium">Last Modified</th>
                  <th class="px-5 py-3 font-medium">Owner (User)</th>
                  <th class="px-5 py-3 font-medium">Group</th>
                  <th class="px-5 py-3 font-medium text-right">Access</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-zinc-800/80">
                <Show when={selectedHistory()} fallback={<tr><td colspan="7" class="py-24 text-center text-zinc-600 font-medium">Please select a scan history from the dropdown above to view its files.</td></tr>}>
                  <Show when={files()} fallback={<tr><td colspan="7" class="py-16 text-center"><FaSolidSpinner class="animate-spin text-blue-500 mx-auto" /></td></tr>}>
                    <For each={files()?.content || []}>
                      {(f) => (
                        <tr class="hover:bg-[#1a1a1c] transition-colors group">
                          <td class="px-5 py-3 font-medium text-zinc-200 flex items-center gap-3">
                            {f.nodeType === "FILE" ? <FaSolidFile class="text-zinc-500" /> : <FaSolidFolder class="text-amber-500" />}
                            <div class="flex flex-col min-w-0">
                              <span class="truncate" title={f.name}>{f.name}</span>
                              <span class="text-[10px] text-zinc-500 truncate max-w-[400px]" title={f.path}>{f.path}</span>
                            </div>
                          </td>
                          <td class="px-5 py-3 text-zinc-500 font-mono text-[11px] uppercase tracking-widest text-center">{f.nodeType}</td>
                          <td class="px-5 py-3 text-zinc-400 font-mono text-xs">
                            {f.sizeBytes !== null && f.sizeBytes !== undefined ? `${(f.sizeBytes / 1024).toFixed(2)} KB` : '-'}
                          </td>
                          <td class="px-5 py-3 text-zinc-500 text-[11px] whitespace-pre">
                            {f.lastModified ? formatDate(f.lastModified) : '-'}
                          </td>
                          <td class="px-5 py-3 text-blue-400/80 font-mono text-xs">
                            <div class="text-zinc-300 font-sans font-bold">{f.ownerName || '<Unknown>'}</div>
                          </td>
                          <td class="px-5 py-3 text-blue-400/80 font-mono text-xs">
                            <div class="text-zinc-300 font-sans font-bold">{f.groupName || '<Unknown>'}</div>
                          </td>
                          <td class="px-5 py-3 text-right">
                            <button
                              class="text-xs px-3 py-1.5 rounded transition-all bg-zinc-800 text-zinc-300 hover:bg-emerald-500/20 hover:text-emerald-400 border border-transparent hover:border-emerald-500/40 font-medium flex items-center gap-2 ml-auto"
                              onClick={() => setSelectedFileForAcl(f.id)}
                            >
                              <FaSolidLock /> View Access
                            </button>
                          </td>
                        </tr>
                      )}
                    </For>
                    {(files()?.content || []).length === 0 && <tr><td colspan="7" class="py-16 text-center text-zinc-600">No objects mapped during this scope.</td></tr>}
                  </Show>
                </Show>
              </tbody>
            </table>
          </div>

          <Show when={selectedHistory() !== null && files()}>
            {/* Pagination Footer */}
            <div class="p-3 border-t border-zinc-800 bg-[#161616] flex items-center justify-between text-xs text-zinc-500">
              <span>Showing Results Page {page() + 1} of {totalPages() || 1} &nbsp;&nbsp;&nbsp; (Total Map Hits: {totalElements()})</span>
              <div class="flex items-center gap-1">
                <button class="px-3 py-1 rounded border border-zinc-700 hover:bg-zinc-800 hover:text-zinc-300 transition-colors disabled:opacity-50" onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page() === 0}>Prev</button>
                <button class="px-3 py-1 rounded border border-zinc-700 hover:bg-zinc-800 hover:text-zinc-300 transition-colors disabled:opacity-50" onClick={() => setPage(p => Math.min(totalPages() - 1, p + 1))} disabled={page() >= totalPages() - 1}>Next</button>
              </div>
            </div>
          </Show>
        </div>

      </div>

      {/* ACL Modal Overlay */}
      <Show when={selectedFileForAcl() !== null}>
        <div class="fixed inset-0 bg-black/60 flex items-center justify-center p-4 z-[100] backdrop-blur-[2px]">
          <div class="bg-[#111111] border border-zinc-800 rounded-lg w-full max-w-3xl shadow-2xl overflow-hidden flex flex-col max-h-[85vh]">
            <div class="px-6 py-4 border-b border-zinc-800 bg-[#161616] flex items-center justify-between">
              <h3 class="text-lg font-medium text-zinc-100 flex items-center gap-2"><FaSolidLock class="text-emerald-500 text-sm" /> File/Folder Permissions (ACL)</h3>
              <button class="text-zinc-500 hover:text-white transition-colors" onClick={() => setSelectedFileForAcl(null)}><FaSolidTimes /></button>
            </div>

            <div class="p-6 overflow-y-auto flex-1 bg-[#0a0a0b] text-sm">
              <Show when={acls()} fallback={<div class="text-zinc-500 text-center py-12"><FaSolidSpinner class="animate-spin text-3xl mb-4 mx-auto text-zinc-700" /> Resolving Access Maps against DCs...</div>}>
                <div class="bg-[#111111] border border-zinc-800 rounded overflow-hidden">
                  <table class="w-full text-left">
                    <thead class="bg-[#18181A] text-zinc-500 text-xs uppercase border-b border-zinc-800">
                      <tr>
                        <th class="px-4 py-3 font-medium">User or Group</th>
                        <th class="px-4 py-3 font-medium text-center">Inherited</th>
                        <th class="px-4 py-3 font-medium text-center">Read</th>
                        <th class="px-4 py-3 font-medium text-center">Create</th>
                        <th class="px-4 py-3 font-medium text-center">Modify</th>
                        <th class="px-4 py-3 font-medium text-center">Delete</th>
                      </tr>
                    </thead>
                    <tbody class="divide-y divide-zinc-800">
                      <For each={acls()}>
                        {(acl: FileAcl) => (
                          <tr class="hover:bg-[#161616] transition-colors">
                            <td class="px-4 py-3 border-l-4 border-l-transparent hover:border-l-emerald-500">
                              <div class="font-mono text-zinc-200 font-semibold tracking-wide">
                                {(!acl.principal || acl.principal.startsWith('S-1-')) ? '<Unknown>' : acl.principal}
                              </div>
                            </td>
                            <td class="px-4 py-3 text-center"><span class="text-[10px] bg-zinc-800 text-zinc-400 px-2 py-1 rounded border border-zinc-700">{acl.inheritanceType}</span></td>
                            <td class="px-4 py-3 text-center">{acl.canView ? <span class="w-3 h-3 rounded-full bg-emerald-500/20 border border-emerald-500/50 inline-block shadow-[0_0_8px_rgba(16,185,129,0.3)]"></span> : <span class="w-2.5 h-2.5 rounded-full bg-zinc-800 border border-zinc-700 inline-block"></span>}</td>
                            <td class="px-4 py-3 text-center">{acl.canAdd ? <span class="w-3 h-3 rounded-full bg-emerald-500/20 border border-emerald-500/50 inline-block shadow-[0_0_8px_rgba(16,185,129,0.3)]"></span> : <span class="w-2.5 h-2.5 rounded-full bg-zinc-800 border border-zinc-700 inline-block"></span>}</td>
                            <td class="px-4 py-3 text-center">{acl.canEdit ? <span class="w-3 h-3 rounded-full bg-emerald-500/20 border border-emerald-500/50 inline-block shadow-[0_0_8px_rgba(16,185,129,0.3)]"></span> : <span class="w-2.5 h-2.5 rounded-full bg-zinc-800 border border-zinc-700 inline-block"></span>}</td>
                            <td class="px-4 py-3 text-center">{acl.canRemove ? <span class="w-3 h-3 rounded-full bg-emerald-500/20 border border-emerald-500/50 inline-block shadow-[0_0_8px_rgba(16,185,129,0.3)]"></span> : <span class="w-2.5 h-2.5 rounded-full bg-zinc-800 border border-zinc-700 inline-block"></span>}</td>
                          </tr>
                        )}
                      </For>
                    </tbody>
                  </table>
                  {acls()?.length === 0 && <div class="text-zinc-600 text-center py-6">Orphaned Access Context.</div>}
                </div>
              </Show>
            </div>

            <div class="px-6 py-4 border-t border-zinc-800 bg-[#161616] flex justify-end">
              <button class="bg-zinc-800 hover:bg-zinc-700 text-white px-5 py-2 rounded text-sm font-medium transition-colors" onClick={() => setSelectedFileForAcl(null)}>Close</button>
            </div>
          </div>
        </div>
      </Show>

    </AppLayout>
  );
}
