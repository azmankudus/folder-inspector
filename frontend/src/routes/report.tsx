import { createSignal, createResource, Show, For, Suspense, createEffect } from "solid-js";
import { useParams, useNavigate, useSearchParams } from "@solidjs/router";
import { 
  FaSolidFileInvoice, 
  FaSolidExclamationTriangle, 
  FaSolidShieldHalved,
  FaSolidDownload,
  FaSolidArrowLeft,
  FaSolidSpinner,
  FaSolidServer,
  FaSolidFolderOpen,
  FaSolidUser,
  FaSolidClock,
  FaSolidHourglassHalf,
  FaSolidCheckCircle,
  FaSolidTimesCircle,
  FaSolidChevronDown
} from "solid-icons/fa";
import { api, getToken } from "~/lib/api";
import AppLayout from "~/components/AppLayout";

type JobReport = {
  id: number;
  scanProfileId: number;
  progress: number;
  startTime: string;
  finishTime?: string;
  status: string;
  totalFiles: number;
  totalFolders: number;
  totalExceptions: number;
  hostname: string;
  rootPath: string;
  username: string;
};

type ScanException = {
  timestamp: string;
  level: string;
  path: string;
  message: string;
  reason: string;
  note?: string;
};

type ScanItem = {
  id: number;
  name: string;
  path: string;
  nodeType: string;
  sizeBytes: number;
  lastModified: string;
  ownerName: string;
  groupName: string;
};

export default function ReportPage() {
  const navigate = useNavigate();
  const [selectedJobId, setSelectedJobId] = createSignal<number | null>(null);

  if (globalThis.window !== undefined && !getToken()) {
    setTimeout(() => navigate("/access/login", { replace: true }), 0);
  }

  const [histories] = createResource(async () => {
    const data = await api.get("/scan/job");
    return (data || []).filter((h: any) => h.status === 'COMPLETED').sort((a: any, b: any) => b.id - a.id);
  });

  const [job] = createResource(selectedJobId, async (id) => {
    if (!id) return null;
    return await api.get(`/scan/job/${id}`);
  });

  const [exceptions] = createResource(selectedJobId, async (id) => {
    if (!id) return [];
    try {
      return await api.get(`/scan/job/${id}/exceptions`);
    } catch (e) {
      return [];
    }
  });

  const [page, setPage] = createSignal(0);
  const [items] = createResource(() => ({ id: selectedJobId(), page: page() }), async ({ id, page }) => {
    if (!id) return { content: [], totalPages: 0, totalElements: 0 };
    try {
      return await api.get(`/report/${id}/items?page=${page}&size=50&showAll=true`);
    } catch (e) {
      return { content: [], totalPages: 0, totalElements: 0 };
    }
  });

  const getParentPath = (fullPath: string | null, name: string | null) => {
    if (!fullPath || !name || fullPath === name) return "";
    if (fullPath.endsWith(name)) {
      let parent = fullPath.substring(0, fullPath.length - name.length);
      if (parent.length > 1 && (parent.endsWith("\\") || parent.endsWith("/"))) {
        parent = parent.substring(0, parent.length - 1);
      }
      return parent;
    }
    return fullPath;
  };

  const formatAcls = (acls: any[]) => {
    if (!acls || acls.length === 0) return "-";
    const grouped: Record<string, any[]> = {};
    acls.forEach(acl => {
      if (!grouped[acl.principal]) grouped[acl.principal] = [];
      grouped[acl.principal].push(acl);
    });
    return Object.entries(grouped).map(([principal, perms]) => {
      const canView = perms.some(a => a.canView);
      const canAdd = perms.some(a => a.canAdd);
      const canEdit = perms.some(a => a.canEdit);
      const canRemove = perms.some(a => a.canRemove);
      const inheritance = [...new Set(perms.map(p => p.inheritanceType).filter(t => t && t !== 'NONE'))].join(', ');
      const pList = [];
      if (canView) pList.push("READ");
      if (canAdd) pList.push("WRITE");
      if (canEdit) pList.push("EDIT");
      if (canRemove) pList.push("DELETE");
      return `${principal}${inheritance ? ` (${inheritance})` : ''}: ${pList.join(', ')}`;
    }).join('\n');
  };

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

  const handleExport = async (includeData: boolean) => {
    const id = selectedJobId();
    if (!id) return;
    try {
      const endpoint = `/report/${id}/export/xlsx?scope=all&includeData=${includeData}`;
      const blob = await api.download(endpoint);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report_${id}_${includeData ? 'full' : 'summary'}_${new Date().getTime()}.xlsx`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (e: any) {
      alert("Export failed: " + e.message);
    }
  };

  return (
    <AppLayout title="Discovery Reports">
      <div class="flex flex-col gap-6">

        {/* Top Context Selector */}
        <div class="flex flex-col gap-4">
          <div class="flex items-center justify-between gap-6 bg-[#161616] border border-zinc-800 rounded-xl p-4 shadow-xl">
            <div class="flex items-center gap-4 flex-1">
              <span class="text-xs font-black text-zinc-500 uppercase tracking-widest whitespace-nowrap">Execution Run:</span>
              <select
                class="bg-zinc-900 border border-zinc-700 rounded-lg px-4 py-2 text-zinc-200 text-sm w-full max-w-md focus:border-amber-500 outline-none transition-all cursor-pointer"
                value={selectedJobId() || ""}
                onChange={(e) => {
                  setSelectedJobId(parseInt(e.currentTarget.value));
                  setPage(0);
                }}
              >
                <option value="" disabled>-- Select a Successful Scan --</option>
                <For each={histories()}>
                  {(h: any) => (
                    <option value={h.id}>
                      Run #{h.id} - {h.hostname} ({new Date(h.startTime).toLocaleDateString()})
                    </option>
                  )}
                </For>
              </select>
            </div>

            <Show when={selectedJobId()}>
              <div class="flex items-center gap-2 shrink-0">
                <button
                  onClick={() => handleExport(false)}
                  class="px-3 py-2 bg-zinc-800 hover:bg-zinc-700 text-zinc-300 rounded-lg flex items-center gap-2 transition-all border border-zinc-700 text-xs font-bold"
                  title="Export Summary XLSX"
                >
                  <FaSolidDownload class="text-amber-500 text-[10px]" /> SUMMARY
                </button>
                <button
                  onClick={() => handleExport(true)}
                  class="px-3 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-lg flex items-center gap-2 transition-all shadow-lg shadow-blue-500/10 text-xs font-bold"
                  title="Export Full XLSX with Data"
                >
                  <FaSolidDownload class="text-[10px]" /> FULL DATA
                </button>
              </div>
            </Show>
          </div>

          <Show when={job()}>
            {(j) => (
              <div class="flex items-center gap-8 p-4 bg-zinc-900/40 rounded-xl border border-zinc-800/60 animate-in fade-in slide-in-from-top-2 duration-300 relative overflow-hidden group">
                <div class="absolute right-0 top-0 h-full w-32 bg-gradient-to-l from-amber-500/5 to-transparent pointer-events-none"></div>
                <div class="flex items-center gap-2 border-r border-zinc-800 pr-6 mr-2">
                  <FaSolidFileInvoice class="text-amber-500 text-sm" />
                  <span class="text-[10px] font-black text-zinc-500 uppercase tracking-widest leading-none">Discovery Metrics</span>
                </div>

                <div class="flex flex-wrap gap-x-10 gap-y-2 flex-1">
                  <MetricItem label="Storage Point" value={j().hostname} />
                  <MetricItem label="Scan Scope" value={j().rootPath} highlight />
                  <MetricItem label="Credentials" value={j().username} />
                  <MetricItem label="Execution Time" value={formatDate(j().startTime)} />
                  <MetricItem label="Performance" value={getDuration(j().startTime, j().finishTime)} />
                </div>

                <div class="flex items-center gap-3 border-l border-zinc-800 pl-8 ml-auto">
                    <span class={`text-[9px] px-2 py-0.5 rounded-full font-black uppercase tracking-widest ${j().status === 'COMPLETED' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-amber-500/10 text-amber-500 border border-amber-500/20'}`}>
                      {j().status}
                    </span>
                </div>
              </div>
            )}
          </Show>
        </div>

        <Show when={!selectedJobId()}>
          <div class="py-32 text-center space-y-4 bg-[#111111]/50 border-2 border-dashed border-zinc-900 rounded-3xl">
            <div class="w-16 h-16 bg-zinc-900 rounded-full flex items-center justify-center mx-auto border border-zinc-800/50">
              <FaSolidFileInvoice class="text-zinc-800 text-2xl" />
            </div>
            <div>
              <h2 class="text-zinc-200 font-bold text-lg">No Report Selected</h2>
              <p class="text-zinc-500 text-sm max-w-xs mx-auto mt-1">Please select a completed scan job from the dropdown above to visualize its discovery data.</p>
            </div>
          </div>
        </Show>

        <Suspense fallback={<div class="py-20 text-center text-zinc-500"><FaSolidSpinner class="animate-spin text-3xl mx-auto mb-4" /> Loading report data...</div>}>
          <Show when={job()}>
            {(j) => (
              <div class="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
                {/* section 1: SCAN SUMMARY */}
                <div class="bg-[#111111] border border-zinc-800 rounded-2xl overflow-hidden shadow-2xl">
                  <div class="px-6 py-4 bg-[#161616] border-b border-zinc-800 flex items-center gap-3">
                    <FaSolidFileInvoice class="text-amber-500 text-xl" />
                    <h2 class="text-lg font-bold text-white tracking-tight">Scan Summary</h2>
                    <div class="ml-auto">
                      <span class={`text-xs px-2.5 py-1 rounded-full font-bold uppercase tracking-wider ${j().status === 'COMPLETED' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-amber-500/10 text-amber-500'}`}>
                        {j().status}
                      </span>
                    </div>
                  </div>
                  
                  <div class="p-8 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-y-8 gap-x-12">
                    <SummaryItem icon={<FaSolidServer class="text-zinc-500" />} label="Server Hostname / IP" value={j().hostname} />
                    <SummaryItem icon={<FaSolidFolderOpen class="text-zinc-500" />} label="Root Path" value={j().rootPath} />
                    <SummaryItem icon={<FaSolidUser class="text-zinc-500" />} label="Username" value={j().username} />
                    <SummaryItem icon={<FaSolidClock class="text-zinc-500" />} label="Start Time" value={formatDate(j().startTime)} />
                    <SummaryItem icon={<FaSolidClock class="text-zinc-500" />} label="Finish Time" value={formatDate(j().finishTime)} />
                    <SummaryItem icon={<FaSolidHourglassHalf class="text-zinc-500" />} label="Duration" value={getDuration(j().startTime, j().finishTime)} />
                    
                    <div class="col-span-full border-t border-zinc-800/50 pt-8 mt-4 grid grid-cols-1 md:grid-cols-3 gap-6">
                      <StatCard label="Total Files" value={j().totalFiles} color="text-blue-400" icon={<FaSolidCheckCircle class="text-blue-500/20" />} />
                      <StatCard label="Total Folders" value={j().totalFolders} color="text-emerald-400" icon={<FaSolidFolderOpen class="text-emerald-500/20" />} />
                      <StatCard label="Total Exceptions" value={j().totalExceptions} color="text-red-400" icon={<FaSolidTimesCircle class="text-red-500/20" />} />
                    </div>
                  </div>
                </div>

                {/* section 2: SCAN EXCEPTIONS */}
                <Show when={exceptions() && exceptions()!.length > 0}>
                   <div class="bg-[#111111] border border-zinc-800 rounded-2xl overflow-hidden shadow-2xl">
                    <div class="px-6 py-4 bg-[#161616] border-b border-zinc-800 flex items-center gap-3">
                      <FaSolidExclamationTriangle class="text-red-500 text-xl" />
                      <h2 class="text-lg font-bold text-white tracking-tight">Scan Exceptions</h2>
                    </div>
                    <div class="overflow-x-auto">
                      <table class="w-full text-left text-sm whitespace-nowrap">
                        <thead class="text-[10px] text-zinc-500 uppercase font-black tracking-widest bg-[#18181A] border-b border-zinc-800">
                          <tr>
                            <th class="px-6 py-4">Timestamp</th>
                            <th class="px-6 py-4">Level</th>
                            <th class="px-6 py-4">Path</th>
                            <th class="px-6 py-4">Issue</th>
                            <th class="px-6 py-4">Reason</th>
                            <th class="px-6 py-4">Note</th>
                          </tr>
                        </thead>
                        <tbody class="divide-y divide-zinc-800/50 font-mono text-xs">
                          <For each={exceptions()}>
                            {(ex) => (
                              <tr class="hover:bg-zinc-900/40 transition-colors">
                                <td class="px-6 py-4 text-zinc-500">{formatDate(ex.timestamp)}</td>
                                <td class="px-6 py-4">
                                  <span class={`px-2 py-0.5 rounded font-bold ${ex.level === 'ERROR' ? 'bg-red-500/10 text-red-500' : 'bg-amber-500/10 text-amber-500'}`}>
                                    {ex.level}
                                  </span>
                                </td>
                                <td class="px-6 py-4 text-zinc-300 max-w-xs overflow-hidden text-ellipsis" title={ex.path}>{ex.path}</td>
                                <td class="px-6 py-4 text-zinc-100 font-semibold">{ex.message}</td>
                                <td class="px-6 py-4 text-zinc-400">{ex.reason}</td>
                                <td class="px-6 py-4 text-emerald-500/70 italic">{ex.note}</td>
                              </tr>
                            )}
                          </For>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </Show>

                {/* section 3: SECURITY DATA */}
                <div class="bg-[#111111] border border-zinc-800 rounded-2xl overflow-hidden shadow-2xl">
                  <div class="px-6 py-4 bg-[#161616] border-b border-zinc-800 flex items-center gap-3">
                    <FaSolidShieldHalved class="text-blue-500 text-xl" />
                    <h2 class="text-lg font-bold text-white tracking-tight">Security Data</h2>
                    <div class="ml-auto flex items-center gap-4">
                      <span class="text-xs text-zinc-500">
                        Showing {items()?.totalElements ? (page() * 50 + 1) : 0} - {Math.min((page() + 1) * 50, items()?.totalElements || 0)} of {items()?.totalElements || 0}
                      </span>
                    </div>
                  </div>
                  <div class="overflow-x-auto min-h-[400px]">
                    <Show when={!items.loading} fallback={<div class="py-20 text-center"><FaSolidSpinner class="animate-spin text-blue-500 text-2xl mx-auto" /></div>}>
                      <table class="w-full text-left text-[11px] whitespace-nowrap">
                        <thead class="text-[10px] text-zinc-500 uppercase font-black tracking-widest bg-[#18181A] border-b border-zinc-800">
                          <tr>
                            <th class="px-6 py-4">ID</th>
                            <th class="px-6 py-4">Name</th>
                            <th class="px-6 py-4">Parent Path</th>
                            <th class="px-6 py-4">Type</th>
                            <th class="px-6 py-4">Size</th>
                            <th class="px-6 py-4">Last Modified</th>
                            <th class="px-6 py-4">Owner</th>
                            <th class="px-6 py-4">Group</th>
                            <th class="px-6 py-4">Permissions (ACL)</th>
                          </tr>
                        </thead>
                        <tbody class="divide-y divide-zinc-800/50 font-mono text-[10px]">
                          <For each={items()?.content || []}>
                            {(item: ScanItem) => (
                              <tr class="hover:bg-zinc-900/40 transition-colors">
                                <td class="px-6 py-3 text-zinc-600">#{item.id}</td>
                                <td class="px-6 py-3 text-blue-400 font-bold">{item.name}</td>
                                <td class="px-6 py-3 text-zinc-400" title={item.path}>{getParentPath(item.path, item.name)}</td>
                                <td class="px-6 py-3 text-center">
                                  <span class={`px-1.5 py-0.5 rounded text-[9px] font-bold ${item.nodeType === 'FOLDER' ? 'bg-amber-500/10 text-amber-500' : 'bg-blue-500/10 text-blue-400'}`}>
                                    {item.nodeType === 'FOLDER' ? 'DIR' : 'FIL'}
                                  </span>
                                </td>
                                <td class="px-6 py-3 text-zinc-300">{item.sizeBytes ? (item.sizeBytes / 1024).toFixed(2) + ' KB' : '-'}</td>
                                <td class="px-6 py-3 text-zinc-500">{formatDate(item.lastModified)}</td>
                                <td class="px-6 py-3 text-zinc-300">{item.ownerName || 'N/A'}</td>
                                <td class="px-6 py-3 text-zinc-300">{item.groupName || 'N/A'}</td>
                                <td class="px-6 py-3 text-zinc-500 whitespace-pre-wrap max-w-sm">{(item as any).acls ? formatAcls((item as any).acls) : "-"}</td>
                              </tr>
                            )}
                          </For>
                        </tbody>
                      </table>
                    </Show>
                  </div>
                  
                  {/* Pagination */}
                  <div class="px-6 py-4 bg-[#161616] border-t border-zinc-800 flex items-center justify-between">
                    <button 
                      onClick={() => setPage(p => Math.max(0, p - 1))}
                      disabled={page() === 0}
                      class="px-4 py-2 bg-zinc-800 hover:bg-zinc-700 disabled:opacity-30 text-zinc-300 rounded-lg text-xs transition-all border border-zinc-700 font-bold"
                    >
                      Previous
                    </button>
                    <div class="flex items-center gap-2">
                       <span class="text-xs text-zinc-500">Page {page() + 1} of {items()?.totalPages || 1}</span>
                    </div>
                    <button 
                      onClick={() => setPage(p => p + 1)}
                      disabled={page() >= (items()?.totalPages || 1) - 1}
                      class="px-4 py-2 bg-zinc-800 hover:bg-zinc-700 disabled:opacity-30 text-zinc-300 rounded-lg text-xs transition-all border border-zinc-700 font-bold"
                    >
                      Next
                    </button>
                  </div>
                </div>
              </div>
            )}
          </Show>
        </Suspense>
      </div>
    </AppLayout>
  );
}

function MetricItem(props: { label: string, value?: string, highlight?: boolean }) {
  return (
    <div class="flex flex-col gap-0.5">
      <span class="text-[9px] text-zinc-500 font-black uppercase tracking-widest">{props.label}</span>
      <span class={`text-xs font-bold ${props.highlight ? 'text-amber-500 font-mono' : 'text-zinc-200'}`}>
        {props.value || 'N/A'}
      </span>
    </div>
  );
}

function SummaryItem(props: { icon: any, label: string, value?: string }) {
  return (
    <div class="flex items-start gap-4">
      <div class="mt-1 w-8 h-8 rounded-full bg-zinc-900 flex items-center justify-center border border-zinc-800 shrink-0">
        {props.icon}
      </div>
      <div class="flex flex-col">
        <span class="text-[10px] text-zinc-500 uppercase font-black tracking-widest mb-1">{props.label}</span>
        <span class="text-sm text-zinc-100 font-medium break-all">{props.value || 'N/A'}</span>
      </div>
    </div>
  );
}

function StatCard(props: { label: string, value: number, color: string, icon: any }) {
  return (
    <div class="relative p-6 bg-zinc-900/30 border border-zinc-800 rounded-2xl overflow-hidden group">
      <div class="absolute -right-2 -bottom-2 text-6xl opacity-10 group-hover:scale-110 transition-transform duration-500">
        {props.icon}
      </div>
      <div class="relative z-10">
        <div class="text-[10px] text-zinc-500 uppercase font-black tracking-widest mb-2">{props.label}</div>
        <div class={`text-3xl font-black font-mono ${props.color}`}>{props.value.toLocaleString()}</div>
      </div>
    </div>
  );
}
