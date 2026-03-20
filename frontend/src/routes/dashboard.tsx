import { createSignal, createResource, Show, For, createEffect, onCleanup } from "solid-js";
import { A } from "@solidjs/router";
import * as echarts from "echarts";
import AppLayout from "~/components/AppLayout";
import { FaBrandsWindows, FaSolidShieldHalved, FaSolidNetworkWired, FaSolidFolderOpen, FaSolidUser, FaSolidFile, FaSolidFolderTree, FaSolidFileInvoice, FaSolidDesktop, FaSolidLock, FaSolidPenToSquare, FaSolidPlay, FaSolidEye, FaSolidSpinner } from "solid-icons/fa";
import { api, getToken } from "~/lib/api";
import { store } from "~/lib/store";

type DashboardData = {
  userSummary: {
    totalFilesOwned: number;
    totalFilesRead: number;
    totalFilesWrite: number;
    totalFilesExecute: number;
  };
  profileCards: {
    scanConfigId: number;
    title: string;
    totalFiles: number;
    totalFolders: number;
    totalRead: number;
    totalWrite: number;
    totalExecute: number;
  }[];
};

export default function Dashboard() {
  const [targetUser, setTargetUser] = createSignal("");
  const [data] = createResource<DashboardData, { user: string }>(
    () => ({ user: targetUser() }),
    ({ user }) => api.get(`/dashboard${user ? `?targetUser=${encodeURIComponent(user)}` : ''}`) as Promise<DashboardData>
  );

  return (
    <AppLayout title="Dashboard">
      <div class="space-y-8 animate-in fade-in duration-500">
        <header>
          <h1 class="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-zinc-100 to-zinc-500">
            Dashboard
          </h1>
          <p class="text-zinc-400 mt-2">
            Overview of your access rights across all scanned profiles.
          </p>
        </header>

        <Show when={store.state.user?.roles?.includes('API_REPORT_ALL') || store.state.user?.roles?.includes('API_LIST_ALL')}>
          <div class="mb-8 flex items-center gap-4 bg-[#111111] p-4 rounded-xl border border-zinc-800 shadow-sm animate-in fade-in duration-300">
            <div class="bg-amber-500/10 p-2 rounded-lg"><FaSolidUser class="text-amber-500" /></div>
            <div class="flex-1">
              <span class="block text-[10px] uppercase tracking-wider font-bold text-zinc-500 mb-1">Impersonation Mode</span>
              <input 
                type="text" 
                placeholder="Type username to view as (e.g. admin)..."
                value={targetUser()} 
                onChange={e => setTargetUser(e.currentTarget.value)}
                class="bg-transparent text-zinc-200 text-sm font-medium focus:outline-none w-full border-b border-transparent focus:border-amber-500 transition-colors" 
              />
            </div>
            <Show when={data.loading}>
              <FaSolidSpinner class="animate-spin text-zinc-500 mr-2" />
            </Show>
          </div>
        </Show>

        <Show when={data()} fallback={<div class="text-zinc-500 flex items-center gap-2"><FaSolidSpinner class="animate-spin"/> Loading dashboard data...</div>}>
          {(dashboard) => (
            <>
              {/* User Summary Section */}
              <section class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <SummaryCard
                  title="Owned Items"
                  value={dashboard().userSummary.totalFilesOwned}
                  icon={<FaSolidUser class="text-indigo-500" />}
                  colorClass="bg-indigo-500/10 border-indigo-500/20"
                />
                <SummaryCard
                  title="Read Access"
                  value={dashboard().userSummary.totalFilesRead}
                  icon={<FaSolidEye class="text-emerald-500" />}
                  colorClass="bg-emerald-500/10 border-emerald-500/20"
                />
                <SummaryCard
                  title="Write Access"
                  value={dashboard().userSummary.totalFilesWrite}
                  icon={<FaSolidPenToSquare class="text-amber-500" />}
                  colorClass="bg-amber-500/10 border-amber-500/20"
                />
                <SummaryCard
                  title="Execute/Traverse"
                  value={dashboard().userSummary.totalFilesExecute}
                  icon={<FaSolidPlay class="text-blue-500" />}
                  colorClass="bg-blue-500/10 border-blue-500/20"
                />
              </section>

              {/* Profile Cards Section */}
              <section>
                <h2 class="text-xl font-bold text-zinc-200 mb-6 flex items-center gap-2">
                  <FaSolidNetworkWired class="text-zinc-500 text-sm" />
                  Profile Breakdown
                </h2>
                
                <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
                  <For each={dashboard().profileCards}>
                    {(card) => (
                      <div class="bg-zinc-900/50 border border-zinc-800 rounded-xl p-6 shadow-xl hover:border-zinc-700 transition-colors group">
                        <div class="flex items-start gap-4 mb-6">
                          <div class="bg-zinc-800 p-3 rounded-lg group-hover:bg-amber-500/20 transition-colors">
                            <FaSolidDesktop class="text-zinc-400 group-hover:text-amber-500 text-xl transition-colors" />
                          </div>
                          <div>
                            <h3 class="text-lg font-bold text-zinc-100 truncate w-[300px] sm:w-[400px]" title={card.title}>
                              {card.title}
                            </h3>
                            <p class="text-zinc-500 text-sm flex items-center gap-4 mt-1">
                              <span class="flex items-center gap-1"><FaSolidFile class="text-xs" /> {card.totalFiles.toLocaleString()} files</span>
                              <span class="flex items-center gap-1"><FaSolidFolderTree class="text-xs" /> {card.totalFolders.toLocaleString()} folders</span>
                            </p>
                          </div>
                        </div>

                        <div class="grid grid-cols-3 gap-4 border-t border-zinc-800 pt-6">
                          <div class="text-center">
                            <span class="block text-zinc-500 text-xs uppercase tracking-wider font-semibold mb-1">Read</span>
                            <span class="text-2xl font-light text-emerald-400">{card.totalRead.toLocaleString()}</span>
                          </div>
                          <div class="text-center border-l border-r border-zinc-800">
                            <span class="block text-zinc-500 text-xs uppercase tracking-wider font-semibold mb-1">Write</span>
                            <span class="text-2xl font-light text-amber-400">{card.totalWrite.toLocaleString()}</span>
                          </div>
                          <div class="text-center">
                            <span class="block text-zinc-500 text-xs uppercase tracking-wider font-semibold mb-1">Execute</span>
                            <span class="text-2xl font-light text-blue-400">{card.totalExecute.toLocaleString()}</span>
                          </div>
                        </div>

                        <ProfileHistoryChart scanConfigId={card.scanConfigId} targetUser={targetUser()} />
                      </div>
                    )}
                  </For>
                  
                  <Show when={dashboard().profileCards?.length === 0}>
                    <div class="col-span-full border border-dashed border-zinc-800 rounded-xl p-12 text-center">
                      <FaSolidFolderOpen class="text-4xl text-zinc-700 mx-auto mb-4" />
                      <p class="text-zinc-400">No profile data available yet.</p>
                    </div>
                  </Show>
                </div>
              </section>
            </>
          )}
        </Show>
      </div>
    </AppLayout>
  );
}

function SummaryCard(props: { title: string; value: number; icon: any; colorClass: string }) {
  return (
    <div class={`rounded-xl border p-6 flex flex-col justify-between shadow-lg transition-transform hover:-translate-y-1 ${props.colorClass}`}>
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-medium text-zinc-300">{props.title}</h3>
        <div class="p-2 bg-black/40 rounded-lg shadow-inner">
          {props.icon}
        </div>
      </div>
      <div>
        <span class="text-4xl font-bold text-white tracking-tight">
          {props.value.toLocaleString()}
        </span>
      </div>
    </div>
  );
}

function ProfileHistoryChart(props: { scanConfigId: number, targetUser: string }) {
  let chartRef!: HTMLDivElement;
  const [history] = createResource(
    () => ({ id: props.scanConfigId, u: props.targetUser }),
    ({ id, u }) => api.get(`/dashboard/history/${id}${u ? `?targetUser=${encodeURIComponent(u)}` : ''}`) as Promise<any[]>
  );

  createEffect(() => {
    const histData = history();
    if (chartRef && histData && histData.length > 0) {
      const chart = echarts.init(chartRef);
      chart.setOption({
        tooltip: { trigger: 'axis', backgroundColor: '#18181b', borderColor: '#3f3f46', textStyle: { color: '#e4e4e7' } },
        grid: { left: '2%', right: '2%', bottom: '2%', top: '10%', containLabel: true },
        xAxis: { 
            type: 'category', 
            data: histData.map(d => new Date(d.timestamp).toLocaleDateString()),
            axisLabel: { color: '#71717a' },
            axisLine: { lineStyle: { color: '#3f3f46' } }
        },
        yAxis: { 
            type: 'value', 
            splitLine: { lineStyle: { color: '#27272a', type: 'dashed' } }, 
            axisLabel: { color: '#71717a' } 
        },
        series: [
          { name: 'Read', type: 'line', smooth: true, data: histData.map(d => d.totalRead), itemStyle: { color: '#34d399' } },
          { name: 'Write', type: 'line', smooth: true, data: histData.map(d => d.totalWrite), itemStyle: { color: '#fbbf24' } },
          { name: 'Execute', type: 'line', smooth: true, data: histData.map(d => d.totalExecute), itemStyle: { color: '#60a5fa' } }
        ]
      });
      onCleanup(() => chart.dispose());
    }
  });

  return (
    <div class="mt-6 border-t border-zinc-800 pt-6">
      <Show when={history.loading}>
        <div class="text-zinc-600 text-xs flex items-center justify-center gap-2 py-4"><FaSolidSpinner class="animate-spin" /> Loading historical graph...</div>
      </Show>
      <Show when={!history.loading && history() && history()?.length > 0}>
        <div class="flex items-center justify-between mb-4">
          <h4 class="text-[10px] font-bold text-zinc-500 uppercase tracking-widest">Access Trend Over Time</h4>
        </div>
        <div ref={chartRef} style={{ width: '100%', height: '220px' }}></div>
      </Show>
      <Show when={!history.loading && history() && history()?.length === 0}>
        <div class="text-zinc-600 text-xs text-center py-4">No historical access events recorded yet.</div>
      </Show>
    </div>
  );
}
