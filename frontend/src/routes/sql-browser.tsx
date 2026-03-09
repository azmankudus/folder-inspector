import { createSignal, createResource, Show, For } from "solid-js";
import { useNavigate } from "@solidjs/router";
import { FaSolidDatabase, FaSolidPlay, FaSolidSpinner } from "solid-icons/fa";
import { api, getToken } from "../lib/api";
import AppLayout from "../components/AppLayout";

type QueryResult = {
  columns?: string[];
  rows?: Record<string, any>[];
  error?: string;
};

const fetchTables = async () => api.get("/sql/tables");

export default function SqlBrowser() {
  const navigate = useNavigate();
  if (globalThis.window !== undefined && !getToken()) {
    setTimeout(() => navigate("/login", { replace: true }), 0);
  }

  const [tables] = createResource(
    () => (globalThis.window !== undefined && getToken() ? "fetch" : null),
    fetchTables
  );
  const [query, setQuery] = createSignal("");
  const [result, setResult] = createSignal<QueryResult | null>(null);
  const [loading, setLoading] = createSignal(false);

  // Pagination for Results
  const [page, setPage] = createSignal(1);
  const rowsPerPage = 15;
  const paginatedRows = () => {
    const res = result();
    if (!res || !res.rows) return [];
    return res.rows.slice((page() - 1) * rowsPerPage, page() * rowsPerPage);
  };
  const totalPages = () => {
    const res = result();
    return Math.max(1, Math.ceil((res?.rows?.length || 0) / rowsPerPage));
  };

  const executeQuery = async (customQuery?: string) => {
    const q = customQuery ?? query();
    if (!q) return;
    
    setLoading(true);
    setResult(null);
    setPage(1);

    try {
      const res = await api.post("/sql/execute", { query: q });
      setResult(res);
    } catch (e: any) {
      setResult({ error: e.message || "Failed to execute query" });
    } finally {
      setLoading(false);
    }
  };

  const handleTableSelect = (tableName: string) => {
    if (!tableName) return;
    const q = `SELECT * FROM ${tableName} LIMIT 1000`; // safeguard limit
    setQuery(q);
    executeQuery(q);
  };

  return (
    <AppLayout title="SQL Database Browser">
      <div class="flex flex-col gap-6">

        {/* Control Panel */}
        <div class="bg-[#111111] border border-zinc-800 rounded-lg p-4 shadow-sm flex flex-col gap-4">
          <div class="flex items-center gap-4">
            <span class="text-sm font-semibold text-zinc-300 flex items-center gap-2">
               <FaSolidDatabase class="text-blue-500" /> Quick View Table:
            </span>
            <select 
              onChange={e => handleTableSelect(e.currentTarget.value)}
              class="bg-[#161616] border border-zinc-700 rounded px-3 py-1.5 text-zinc-200 text-sm min-w-[200px]"
            >
                <option value="">-- Select Table --</option>
                <Show when={tables()}>
                  <For each={tables()}>
                    {t => <option value={t}>{t}</option>}
                  </For>
                </Show>
            </select>
          </div>
          
          <div class="border-t border-zinc-800/80 pt-4">
            <label class="block text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-2">Custom SQL Query</label>
            <div class="flex gap-3 items-end">
               <textarea 
                  value={query()}
                  onInput={e => setQuery(e.currentTarget.value)}
                  class="bg-[#0a0a0b] border border-zinc-700 rounded p-3 text-zinc-200 text-sm font-mono flex-1 min-h-[100px] resize-y focus:outline-none focus:border-blue-500"
                  placeholder="SELECT * FROM connection_profile WHERE id = 1"
               />
               <button 
                 onClick={() => executeQuery()}
                 disabled={loading() || !query()}
                 class="bg-blue-600 hover:bg-blue-500 disabled:opacity-50 disabled:cursor-not-allowed text-white px-6 py-3 rounded text-sm font-medium flex items-center gap-2 transition-colors h-[46px]"
               >
                 <Show when={loading()} fallback={<FaSolidPlay />}>
                    <FaSolidSpinner class="animate-spin" />
                 </Show>
                 Execute
               </button>
            </div>
          </div>
        </div>

        {/* Results Panel */}
        <div class="bg-[#111111] border border-zinc-800 rounded-lg shadow-sm overflow-hidden flex flex-col min-h-[400px]">
          <div class="p-4 border-b border-zinc-800 bg-[#161616]">
            <h2 class="text-sm font-semibold text-zinc-100">Execution Results</h2>
          </div>
          
          <div class="overflow-x-auto flex-1 bg-[#111111]">
            <Show when={result()} fallback={
               <div class="text-zinc-600 flex items-center justify-center p-20 font-medium">Input a query above to view tabular results.</div>
            }>
               {(res) => (
                  <Show when={!res().error} fallback={
                      <div class="text-red-400 font-mono text-sm p-6 bg-[#1a1111]">
                         <strong>SQL Error: </strong><br/><br/>
                         {res().error}
                      </div>
                  }>
                      <table class="w-full text-left text-sm whitespace-nowrap">
                        <thead class="text-xs text-zinc-400 uppercase bg-[#18181A] border-b border-zinc-800">
                          <tr>
                            <For each={res().columns || []}>
                              {col => <th class="px-5 py-3 font-medium border-r border-zinc-800/50 last:border-0">{col}</th>}
                            </For>
                          </tr>
                        </thead>
                        <tbody class="divide-y divide-zinc-800/80">
                          <For each={paginatedRows()}>
                            {(row) => (
                              <tr class="hover:bg-[#1a1a1c] transition-colors">
                                 <For each={res().columns || []}>
                                    {col => (
                                       <td class="px-5 py-2.5 text-zinc-300 font-mono text-[13px] border-r border-zinc-800/50 last:border-0">
                                          {row[col] === null ? <span class="text-zinc-600 italic">NULL</span> : String(row[col])}
                                       </td>
                                    )}
                                 </For>
                              </tr>
                            )}
                          </For>
                          <Show when={(res().rows?.length || 0) === 0}>
                            <tr><td colspan={(res().columns?.length || 1)} class="py-10 text-center text-zinc-500">Query returned 0 rows.</td></tr>
                          </Show>
                        </tbody>
                      </table>
                  </Show>
               )}
            </Show>
          </div>

          <Show when={result() && !result()!.error && (result()!.rows?.length || 0) > 0}>
              {/* Pagination Footer */}
              <div class="p-3 border-t border-zinc-800 bg-[#161616] flex items-center justify-between text-xs text-zinc-500">
                <span>Showing Page {page()} of {totalPages()} (Total Records: {result()!.rows?.length})</span>
                <div class="flex items-center gap-1">
                  <button class="px-3 py-1 rounded border border-zinc-700 hover:bg-zinc-800 hover:text-zinc-300 transition-colors disabled:opacity-50" onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page() === 1}>Prev</button>
                  <button class="px-3 py-1 rounded border border-zinc-700 hover:bg-zinc-800 hover:text-zinc-300 transition-colors disabled:opacity-50" onClick={() => setPage(p => Math.min(totalPages(), p + 1))} disabled={page() >= totalPages()}>Next</button>
                </div>
              </div>
          </Show>
        </div>

      </div>
    </AppLayout>
  );
}
