import { createSignal, createResource, Show, For } from "solid-js";
import { useNavigate } from "@solidjs/router";
import { FaSolidClockRotateLeft, FaSolidSpinner, FaSolidTerminal, FaSolidTimes } from "solid-icons/fa";
import { api, getToken } from "../lib/api";
import AppLayout from "../components/AppLayout";

type ScanHistory = { id: number, scanProfileId: number, status: string, progress: number, startTime: string, finishTime?: string };

const fetchHistories = async () => api.get("/jobs/history");

export default function ScanHistoryView() {
  const navigate = useNavigate();
  if (globalThis.window !== undefined && !getToken()) {
    setTimeout(() => navigate("/login", { replace: true }), 0);
  }

  const [histories] = createResource(
    () => (globalThis.window !== undefined && getToken() ? "fetch" : null),
    fetchHistories
  );
  const [logModalOpen, setLogModalOpen] = createSignal<ScanHistory | null>(null);
  const [logs] = createResource(logModalOpen, async (job) => {
    try {
      const res = await fetch(`http://localhost:8080/api/jobs/${job.id}/logs`, {
        headers: { "Authorization": `Bearer ${getToken()}` }
      });
      if (!res.ok) return "logs deleted";
      return await res.text();
    } catch (e) {
      return "logs deleted";
    }
  });

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
    <AppLayout title="Audit Execution Logs">
      <div class="bg-[#111111] border border-zinc-800 rounded-lg shadow-sm overflow-hidden flex flex-col min-h-[500px]">
        <div class="p-4 border-b border-zinc-800 bg-[#161616]">
          <h2 class="text-sm font-semibold text-zinc-100 flex items-center gap-2">
            <FaSolidClockRotateLeft class="text-amber-500" /> Historical Job Trace
          </h2>
        </div>
        
        <div class="overflow-x-auto flex-1 bg-[#111111]">
          <table class="w-full text-left text-sm whitespace-nowrap">
            <thead class="text-xs text-zinc-500 uppercase bg-[#18181A] border-b border-zinc-800">
              <tr>
                <th class="px-5 py-3 font-medium">Job ID</th>
                <th class="px-5 py-3 font-medium">Scan Profile</th>
                <th class="px-5 py-3 font-medium">Status</th>
                <th class="px-5 py-3 font-medium text-center">Progress (%)</th>
                <th class="px-5 py-3 font-medium">Timeline (Start / End)</th>
                <th class="px-5 py-3 font-medium text-right">Trace Output</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-zinc-800/80">
              <Show when={histories()} fallback={<tr><td colspan="6" class="px-5 py-8 text-center text-zinc-500"><FaSolidSpinner class="animate-spin text-amber-500 mx-auto" /></td></tr>}>
                <For each={paginatedData()}>
                  {(h) => (
                    <tr class="hover:bg-[#1a1a1c] transition-colors">
                      <td class="px-5 py-3 text-zinc-300 font-mono text-xs font-semibold">#{h.id}</td>
                      <td class="px-5 py-3 text-blue-400 font-mono text-[13px]">Scan Profile {h.scanProfileId}</td>
                      <td class="px-5 py-3 font-medium">
                        <span class={`text-[10px] px-2 py-0.5 rounded tracking-wider uppercase ${h.status === 'COMPLETED' ? 'bg-emerald-500/10 text-emerald-400' : h.status === 'FAILED' ? 'bg-red-500/10 text-red-500' : 'bg-amber-500/10 text-amber-500'}`}>
                          {h.status}
                        </span>
                      </td>
                      <td class="px-5 py-3 text-zinc-400 font-mono text-xs">{h.progress}%</td>
                      <td class="px-5 py-3 flex flex-col gap-0.5 mt-1">
                        <div class="text-[11px] text-zinc-500"><span class="text-zinc-600">Start:</span> {new Date(h.startTime).toLocaleString()}</div>
                        <div class="text-[11px] text-zinc-500"><span class="text-zinc-600">Stop:</span> {h.finishTime ? new Date(h.finishTime!).toLocaleString() : '...'}</div>
                      </td>
                      <td class="px-5 py-3 text-right">
                        <button 
                          class="text-xs px-3 py-1.5 rounded transition-all bg-zinc-800 hover:bg-amber-500/20 text-zinc-300 hover:text-amber-400 inline-flex items-center gap-2 border border-transparent hover:border-amber-500/40"
                          onClick={() => setLogModalOpen(h)}
                        >
                           <FaSolidTerminal /> View Logs
                        </button>
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
          <div class="bg-[#111111] border border-zinc-800 rounded-lg w-full max-w-4xl shadow-2xl overflow-hidden flex flex-col max-h-[85vh]">
            <div class="px-6 py-4 border-b border-zinc-800 bg-[#161616] flex items-center justify-between">
              <h3 class="text-lg font-medium text-zinc-100 flex items-center gap-2"><FaSolidTerminal class="text-amber-500 text-sm" /> Job StdOut Trace: Execution #{logModalOpen()?.id}</h3>
              <button class="text-zinc-500 hover:text-white transition-colors" onClick={() => setLogModalOpen(null)}><FaSolidTimes /></button>
            </div>
            
            <div class="p-6 overflow-y-auto flex-1 bg-[#0a0a0b] text-sm">
                <pre class="font-mono text-[12px] text-emerald-400 bg-black p-4 rounded border border-zinc-800 whitespace-pre-wrap leading-relaxed">
{logs.loading ? "Loading telemetry..." : logs()}
                </pre>
            </div>
          </div>
        </div>
      </Show>

    </AppLayout>
  );
}
