import { createSignal, createResource, Show, For } from "solid-js";
import { useNavigate } from "@solidjs/router";
import { FaSolidClockRotateLeft, FaSolidSpinner, FaSolidTerminal, FaSolidTimes, FaSolidFileExport, FaSolidFileInvoice, FaSolidScroll } from "solid-icons/fa";
import { api, getToken } from "~/lib/api";
import AppLayout from "~/components/AppLayout";

type ScanHistory = {
  id: number,
  scanConfigId: number,
  status: string,
  progress: number,
  startTime: string,
  finishTime?: string,
  totalFiles?: number,
  totalFolders?: number,
  totalExceptions?: number,
  logPath?: string
};

const fetchHistories = async () => api.get("/scan/job");

export default function ScanHistoryView() {
  const navigate = useNavigate();
  if (globalThis.window !== undefined && !getToken()) {
    setTimeout(() => navigate("/access/login", { replace: true }), 0);
  }

  const [histories] = createResource(
    () => (globalThis.window !== undefined && getToken() ? "fetch" : null),
    fetchHistories
  );
  const [logModalOpen, setLogModalOpen] = createSignal<ScanHistory | null>(null);
  const [logs] = createResource(logModalOpen, async (job) => {
    try {
      return await api.get(`/scan/job/${job.id}/logs`);
    } catch (e) {
      return [];
    }
  });

  const [exceptionModalOpen, setExceptionModalOpen] = createSignal<ScanHistory | null>(null);

  const [logFileModalOpen, setLogFileModalOpen] = createSignal<ScanHistory | null>(null);
  const [logFileContent] = createResource(logFileModalOpen, async (job) => {
    try {
      return await api.get(`/scan/job/${job.id}/log-file`);
    } catch (e: any) {
      return `Error loading log file: ${e.message}`;
    }
  });
  const [exceptions] = createResource(exceptionModalOpen, async (job) => {
    try {
      return await api.get(`/scan/job/${job.id}/exceptions`);
    } catch (e) {
      return [];
    }
  });

  const getDuration = (start: string, finish?: string) => {
    if (!finish) return "N/A";
    const d = new Date(finish).getTime() - new Date(start).getTime();
    const mins = Math.floor(d / 60000);
    const secs = Math.floor((d % 60000) / 1000);
    return `${mins} min, ${secs} sec`;
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return "N/A";
    return new Date(dateStr).toLocaleString();
  };

  const handleExport = async (historyId: number, includeData: boolean) => {
    try {
      const endpoint = `/explorer/${historyId}/export/xlsx?scope=all&includeData=${includeData}`;
      const blob = await api.download(endpoint);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report_${historyId}_${includeData ? 'full' : 'summary'}_${new Date().getTime()}.xlsx`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (e: any) {
      alert("Export failed: " + e.message);
    }
  };

  // Pagination
  const [page, setPage] = createSignal(1);
  const rowsPerPage = 10;
  const paginatedData = () => {
    const data = histories();
    if (!data) return [];
    // Sort descending by ID to see most recent first
    return [...data].sort((a, b) => b.id - a.id).slice((page() - 1) * rowsPerPage, page() * rowsPerPage);
  };
  const totalPages = () => Math.ceil((histories()?.length || 0) / rowsPerPage);

  return (
    <AppLayout title="History">
      <div class="bg-[#111111] border border-zinc-800 rounded-lg shadow-sm overflow-hidden flex flex-col min-h-[500px]">
        <div class="p-4 border-b border-zinc-800 bg-[#161616]">
          <h2 class="text-sm font-semibold text-zinc-100 flex items-center gap-2">
            <FaSolidClockRotateLeft class="text-amber-500" /> Past Scans
          </h2>
        </div>

        <div class="overflow-x-auto flex-1 bg-[#111111]">
          <table class="w-full text-left text-sm whitespace-nowrap">
            <thead class="text-xs text-zinc-500 uppercase bg-[#18181A] border-b border-zinc-800">
              <tr>
                <th class="px-5 py-3 font-medium">ID</th>
                <th class="px-5 py-3 font-medium">Profile</th>
                <th class="px-5 py-3 font-medium">Status</th>
                <th class="px-5 py-3 font-medium text-center">Progress</th>
                <th class="px-5 py-3 font-medium">Time (Start / End)</th>
                <th class="px-5 py-3 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-zinc-800/80">
              <Show when={histories()} fallback={<tr><td colspan="6" class="px-5 py-8 text-center text-zinc-500"><FaSolidSpinner class="animate-spin text-amber-500 mx-auto" /></td></tr>}>
                <For each={paginatedData()}>
                  {(h) => (
                    <tr class="hover:bg-[#1a1a1c] transition-colors">
                      <td class="px-5 py-3 text-zinc-300 font-mono text-xs font-semibold">#{h.id}</td>
                      <td class="px-5 py-3 text-blue-400 font-mono text-[13px]">Scan Profile {h.scanConfigId}</td>
                      <td class="px-5 py-3 font-medium">
                        <span class={`text-[10px] px-2 py-0.5 rounded tracking-wider uppercase font-bold ${h.status === 'COMPLETED' ? 'bg-emerald-500/10 text-emerald-400' : h.status === 'FAILED' ? 'bg-red-500/10 text-red-500' : 'bg-amber-500/10 text-amber-500'}`}>
                          {h.status === 'COMPLETED' ? 'Done' : h.status === 'FAILED' ? 'Failed' : h.status}
                        </span>
                      </td>
                      <td class="px-5 py-3 text-zinc-400 font-mono text-xs">{h.progress}%</td>
                      <td class="px-5 py-3 flex flex-col gap-0.5 mt-1">
                        <div class="text-[11px] text-zinc-500"><span class="text-zinc-600">Start:</span> {new Date(h.startTime).toLocaleString()}</div>
                        <div class="text-[11px] text-zinc-500"><span class="text-zinc-600">Stop:</span> {h.finishTime ? new Date(h.finishTime!).toLocaleString() : '...'}</div>
                      </td>
                      <td class="px-5 py-3 text-right">
                        <div class="flex items-center justify-end gap-2">
                          <Show when={h.status === 'COMPLETED'}>
                            <button
                              class="text-[10px] px-2 py-1 rounded transition-all bg-zinc-800 hover:bg-emerald-500/20 text-zinc-400 hover:text-emerald-400 border border-transparent hover:border-emerald-500/40 flex items-center gap-1"
                              onClick={() => handleExport(h.id, false)}
                              title="Export Summary Report"
                            >
                              <FaSolidFileInvoice /> Report Only
                            </button>
                            <button
                              class="text-[10px] px-2 py-1 rounded transition-all bg-zinc-800 hover:bg-blue-500/20 text-zinc-400 hover:text-blue-400 border border-transparent hover:border-blue-500/40 flex items-center gap-1"
                              onClick={() => handleExport(h.id, true)}
                              title="Export Full Report with Data"
                            >
                              <FaSolidFileExport /> Report + Data
                            </button>
                          </Show>
                          <Show when={h.logPath}>
                            <button
                              class="text-xs px-3 py-1.5 rounded transition-all bg-zinc-800 hover:bg-violet-500/20 text-zinc-300 hover:text-violet-400 inline-flex items-center gap-2 border border-transparent hover:border-violet-500/40"
                              onClick={() => setLogFileModalOpen(h)}
                            >
                              <FaSolidScroll /> Log File
                            </button>
                          </Show>
                          <button
                            class="text-xs px-3 py-1.5 rounded transition-all bg-zinc-800 hover:bg-amber-500/20 text-zinc-300 hover:text-amber-400 inline-flex items-center gap-2 border border-transparent hover:border-amber-500/40"
                            onClick={() => setLogModalOpen(h)}
                          >
                            <FaSolidTerminal /> Logs
                          </button>
                          <button
                            class="text-xs px-3 py-1.5 rounded transition-all bg-zinc-800 hover:bg-red-500/20 text-zinc-300 hover:text-red-400 inline-flex items-center gap-2 border border-transparent hover:border-red-500/40"
                            onClick={() => setExceptionModalOpen(h)}
                          >
                            <FaSolidTimes /> Issues
                          </button>
                        </div>
                      </td>
                    </tr>
                  )}
                </For>
                {paginatedData().length === 0 && <tr><td colspan="6" class="py-10 text-center text-zinc-600">No telemetry or executions discovered.</td></tr>}
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

      {/* STDOUT Log Modal Overlay */}
      <Show when={logModalOpen() !== null}>
        <div class="fixed inset-0 bg-black/60 flex items-center justify-center p-4 z-[100] backdrop-blur-[2px]">
          <div class="bg-[#111111] border border-zinc-800 rounded-lg w-full max-w-5xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
            <div class="px-6 py-4 border-b border-zinc-800 bg-[#161616] flex items-center justify-between">
              <h3 class="text-lg font-medium text-zinc-100 flex items-center gap-2"><FaSolidTerminal class="text-amber-500 text-sm" /> Scan Logs: Run #{logModalOpen()?.id}</h3>
              <div class="flex items-center gap-3">
                <button
                  class="text-[11px] px-3 py-1.5 rounded bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/20 flex items-center gap-2 transition-all"
                  onClick={() => handleExport(logModalOpen()!.id, false)}
                >
                  <FaSolidFileInvoice /> Export Report
                </button>
                <button
                  class="text-[11px] px-3 py-1.5 rounded bg-blue-500/10 hover:bg-blue-500/20 text-blue-400 border border-blue-500/20 flex items-center gap-2 transition-all"
                  onClick={() => handleExport(logModalOpen()!.id, true)}
                >
                  <FaSolidFileExport /> Export Report + Data
                </button>
                <button class="text-zinc-500 hover:text-white transition-colors ml-2" onClick={() => setLogModalOpen(null)}><FaSolidTimes /></button>
              </div>
            </div>

            <div class="p-6 overflow-y-auto flex-1 bg-[#0a0a0b] text-sm">
              <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
                <div class="p-3 bg-zinc-900/50 border border-zinc-800 rounded-lg">
                  <div class="text-[10px] text-zinc-500 uppercase font-bold tracking-widest mb-1">Status</div>
                  <div class={`text-sm font-bold ${logModalOpen()?.status === 'COMPLETED' ? 'text-emerald-500' : 'text-amber-500'}`}>{logModalOpen()?.status}</div>
                </div>
                <div class="p-3 bg-zinc-900/50 border border-zinc-800 rounded-lg">
                  <div class="text-[10px] text-zinc-500 uppercase font-bold tracking-widest mb-1">Start Time</div>
                  <div class="text-sm text-zinc-300 font-mono">{formatDate(logModalOpen()?.startTime)}</div>
                </div>
                <div class="p-3 bg-zinc-900/50 border border-zinc-800 rounded-lg">
                  <div class="text-[10px] text-zinc-500 uppercase font-bold tracking-widest mb-1">Finish Time</div>
                  <div class="text-sm text-zinc-300 font-mono">{formatDate(logModalOpen()?.finishTime)}</div>
                </div>
                <div class="p-3 bg-zinc-900/50 border border-zinc-800 rounded-lg">
                  <div class="text-[10px] text-zinc-500 uppercase font-bold tracking-widest mb-1">Duration</div>
                  <div class="text-sm text-zinc-300 font-mono">{(() => { const h = logModalOpen(); return h ? getDuration(h.startTime, h.finishTime) : 'N/A'; })()}</div>
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                <div class="p-4 bg-zinc-900/30 border border-emerald-500/10 rounded-lg flex items-center justify-between">
                  <div>
                    <div class="text-[10px] text-zinc-500 uppercase font-bold mb-1">Total Files</div>
                    <div class="text-xl font-mono text-emerald-400">{logModalOpen()?.totalFiles ?? 0}</div>
                  </div>
                </div>
                <div class="p-4 bg-zinc-900/30 border border-blue-500/10 rounded-lg flex items-center justify-between">
                  <div>
                    <div class="text-[10px] text-zinc-500 uppercase font-bold mb-1">Total Folders</div>
                    <div class="text-xl font-mono text-blue-400">{logModalOpen()?.totalFolders ?? 0}</div>
                  </div>
                </div>
                <div class="p-4 bg-zinc-900/30 border border-red-500/10 rounded-lg flex items-center justify-between">
                  <div>
                    <div class="text-[10px] text-zinc-500 uppercase font-bold mb-1">Total Exceptions</div>
                    <div class="text-xl font-mono text-red-500">{logModalOpen()?.totalExceptions ?? 0}</div>
                  </div>
                </div>
              </div>
              <Show when={!logs.loading} fallback={<div class="py-20 text-center text-zinc-500"><FaSolidSpinner class="animate-spin mb-2 mx-auto" /> Loading database traces...</div>}>
                <div class="border border-zinc-800 rounded overflow-hidden">
                  <table class="w-full text-left text-xs bg-[#0f0f10]">
                    <thead class="bg-[#18181A] border-b border-zinc-800 text-zinc-500 uppercase">
                      <tr>
                        <th class="px-4 py-2 font-medium w-40">Timestamp</th>
                        <th class="px-4 py-2 font-medium w-20">Level</th>
                        <th class="px-4 py-2 font-medium">Message</th>
                      </tr>
                    </thead>
                    <tbody class="divide-y divide-zinc-800 font-mono">
                      <For each={logs() || []}>
                        {(log: any) => (
                          <tr class="hover:bg-zinc-900/50">
                            <td class="px-4 py-2 text-zinc-500 whitespace-nowrap">{formatDate(log.timestamp)}</td>
                            <td class="px-4 py-2">
                              <span class={`px-1 rounded text-[9px] font-bold ${log.logLevel === 'ERROR' ? 'bg-red-500/20 text-red-500' : log.logLevel === 'WARN' ? 'bg-amber-500/20 text-amber-500' : 'bg-blue-500/20 text-blue-400'}`}>
                                {log.logLevel}
                              </span>
                            </td>
                            <td class={`px-4 py-2 ${log.logLevel === 'ERROR' ? 'text-red-400' : log.logLevel === 'WARN' ? 'text-amber-400/80' : 'text-zinc-300'}`}>
                              {log.message}
                            </td>
                          </tr>
                        )}
                      </For>
                      {(logs() || []).length === 0 && <tr><td colspan="3" class="px-4 py-8 text-center text-zinc-600">No structured logs found for this execution.</td></tr>}
                    </tbody>
                  </table>
                </div>
              </Show>
            </div>
          </div>
        </div>
      </Show>

      {/* Log File Modal */}
      <Show when={logFileModalOpen() !== null}>
        <div class="fixed inset-0 bg-black/60 flex items-center justify-center p-4 z-[100] backdrop-blur-[2px]">
          <div class="bg-[#0d0d0f] border border-zinc-800 rounded-lg w-full max-w-5xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
            <div class="px-6 py-4 border-b border-zinc-800 bg-[#161616] flex items-center justify-between">
              <h3 class="text-lg font-medium text-zinc-100 flex items-center gap-2">
                <FaSolidScroll class="text-violet-400 text-sm" /> Log File: Job #{logFileModalOpen()?.id}
              </h3>
              <button class="text-zinc-500 hover:text-white transition-colors" onClick={() => setLogFileModalOpen(null)}><FaSolidTimes /></button>
            </div>
            <div class="flex-1 overflow-y-auto bg-[#080809] p-4">
              <Show when={!logFileContent.loading} fallback={
                <div class="py-20 text-center text-zinc-500"><FaSolidSpinner class="animate-spin mb-2 mx-auto" /> Reading log file...</div>
              }>
                <pre class="text-xs text-zinc-300 font-mono whitespace-pre-wrap break-all leading-5">{logFileContent()}</pre>
              </Show>
            </div>
          </div>
        </div>
      </Show>

      {/* Exception Modal Overlay */}
      <Show when={exceptionModalOpen() !== null}>
        <div class="fixed inset-0 bg-black/60 flex items-center justify-center p-4 z-[100] backdrop-blur-[2px]">
          <div class="bg-[#111111] border border-zinc-800 rounded-lg w-full max-w-6xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
            <div class="px-6 py-4 border-b border-zinc-800 bg-[#161616] flex items-center justify-between">
              <h3 class="text-lg font-medium text-zinc-100 flex items-center gap-2">
                <FaSolidTimes class="text-red-500 text-sm" /> Recorded Issues: Run #{exceptionModalOpen()?.id}
              </h3>
              <button class="text-zinc-500 hover:text-white transition-colors" onClick={() => setExceptionModalOpen(null)}><FaSolidTimes /></button>
            </div>

            <div class="p-6 overflow-y-auto overflow-x-hidden flex-1 bg-[#0a0a0b]">
              <Show when={!exceptions.loading} fallback={<div class="py-20 text-center text-zinc-500"><FaSolidSpinner class="animate-spin mb-2 mx-auto" /> Fetching exception trace...</div>}>
                <div class="border border-zinc-800 rounded overflow-x-auto">
                  <table class="w-full min-w-max text-left text-xs bg-[#0f0f10]">
                    <thead class="bg-[#18181A] border-b border-zinc-800 text-zinc-500 uppercase">
                      <tr>
                        <th class="px-4 py-3 font-medium w-40">Timestamp</th>
                        <th class="px-4 py-3 font-medium w-24">Level</th>
                        <th class="px-4 py-3 font-medium w-72">Path / Resource</th>
                        <th class="px-4 py-3 font-medium">Issue</th>
                        <th class="px-4 py-3 font-medium">Reason</th>
                        <th class="px-4 py-3 font-medium">Note</th>
                      </tr>
                    </thead>
                    <tbody class="divide-y divide-zinc-800 font-mono">
                      <For each={exceptions() || []}>
                        {(ex: any) => (
                          <tr class="hover:bg-zinc-900/50">
                            <td class="px-4 py-3 text-zinc-500 whitespace-nowrap">{formatDate(ex.timestamp)}</td>
                            <td class="px-4 py-3">
                              <span class={`px-2 py-0.5 rounded text-[10px] font-bold ${ex.level === 'ERROR' ? 'bg-red-500/20 text-red-500' : 'bg-amber-500/20 text-amber-500'}`}>
                                {ex.level}
                              </span>
                            </td>
                            <td class="px-4 py-3 text-zinc-300 break-all max-w-[18rem]">{ex.path}</td>
                            <td class="px-4 py-3 text-zinc-100 font-semibold">{ex.message}</td>
                            <td class={`px-4 py-3 ${ex.level === 'ERROR' ? 'text-red-400' : 'text-amber-400/80'}`}>{ex.reason}</td>
                            <td class="px-4 py-3 text-emerald-400/80 italic">{ex.note}</td>
                          </tr>
                        )}
                      </For>
                      {(exceptions() || []).length === 0 && <tr><td colspan="6" class="px-4 py-12 text-center text-zinc-600">No telemetry exceptions recorded for this job.</td></tr>}
                    </tbody>
                  </table>
                </div>
              </Show>
            </div>
          </div>
        </div>
      </Show>
    </AppLayout>
  );
}
