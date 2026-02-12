import { createMemo, createSignal, Show, For } from 'solid-js';
import { FileEntry, TargetConfig, Theme } from '../types';
import { username } from '../store';

interface DashboardProps {
  files: () => FileEntry[];
  configs: () => TargetConfig[];
  theme: () => Theme;
}

const COLORS = ['#DC2626', '#1E40AF', '#059669', '#D97706', '#7C3AED'];
const PASTEL_COLORS = [
  { bg: '#FEE2E2', text: '#991B1B', accent: '#FECACA' },
  { bg: '#DBEAFE', text: '#1E3A8A', accent: '#BFDBFE' },
  { bg: '#D1FAE5', text: '#065F46', accent: '#A7F3D0' },
  { bg: '#FEF3C7', text: '#92400E', accent: '#FDE68A' },
  { bg: '#EDE9FE', text: '#5B21B6', accent: '#DDD6FE' },
  { bg: '#FCE7F3', text: '#9D174D', accent: '#FBCFE8' },
  { bg: '#FFEDD5', text: '#9A3412', accent: '#FED7AA' }, // Orange
  { bg: '#E0F2FE', text: '#075985', accent: '#BAE6FD' }, // Sky
  { bg: '#F3E8FF', text: '#6B21A8', accent: '#D8B4FE' }, // Purple
  { bg: '#ECFCCB', text: '#365314', accent: '#D9F99D' }, // Lime
];

const Dashboard = (props: DashboardProps) => {
  const isDark = () => props.theme() === 'Dark';
  const currentUser = 'user_john';
  const userFiles = () => props.files().filter(f => f.owner === currentUser);
  const totalOwned = () => userFiles().length;
  const filesOwned = () => userFiles().filter(f => f.type === 'File').length;
  const foldersOwned = () => userFiles().filter(f => f.type === 'Directory').length;

  const configStats = createMemo(() => props.configs().map(cfg => {
    const count = props.files().filter(f => f.configId === cfg.id).length;
    return { name: cfg.name, count };
  }).filter(c => c.count > 0));

  const pieData = createMemo(() => {
    const accessMap: Record<string, number> = {};
    props.files().forEach(f => {
      const primaryAccess = f.acl[0]?.name || 'Unknown';
      accessMap[primaryAccess] = (accessMap[primaryAccess] || 0) + 1;
    });
    return Object.entries(accessMap).map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value);
  });

  const cardClass = () => isDark() ? 'bg-slate-800 border-slate-700 text-white' : 'bg-white border-slate-200 text-slate-800';
  const subTextClass = () => isDark() ? 'text-slate-400' : 'text-slate-500';

  const [tooltip, setTooltip] = createSignal<{ name: string; value: number; pct: number; x: number; y: number } | null>(null);

  const conicGradient = () => {
    const data = pieData();
    const total = data.reduce((acc, d) => acc + d.value, 0);
    if (total === 0) return 'conic-gradient(#ccc 0% 100%)';
    let cumPct = 0;
    const stops: string[] = [];
    data.forEach((d, i) => {
      const pct = (d.value / total) * 100;
      stops.push(`${COLORS[i % COLORS.length]} ${cumPct}% ${cumPct + pct}%`);
      cumPct += pct;
    });
    return `conic-gradient(${stops.join(', ')})`;
  };

  const sliceLabels = () => {
    const data = pieData();
    const total = data.reduce((acc, d) => acc + d.value, 0);
    if (total === 0) return [];
    let cumPct = 0;
    return data.map((d, i) => {
      const pct = (d.value / total) * 100;
      const midPct = cumPct + pct / 2;
      cumPct += pct;
      const angle = (midPct / 100) * Math.PI * 2 - Math.PI / 2;
      const r = 35;
      const x = 50 + r * Math.cos(angle);
      const y = 50 + r * Math.sin(angle);
      return { value: d.value, name: d.name, pct: Math.round(pct), x, y, color: COLORS[i % COLORS.length] };
    });
  };

  const handleSliceMouseMove = (lbl: { name: string; value: number; pct: number }, e: MouseEvent) => {
    const target = e.currentTarget as HTMLElement;
    const rect = target.closest('.pie-container')?.getBoundingClientRect();
    if (rect) {
      setTooltip({ name: lbl.name, value: lbl.value, pct: lbl.pct, x: e.clientX - rect.left, y: e.clientY - rect.top - 10 });
    }
  };

  const handleSliceMouseLeave = () => { setTooltip(null); };

  const stats = [
    { label: 'Total Owned', value: totalOwned, color: 'red', icon: <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /> },
    { label: 'Files Owned', value: filesOwned, color: 'blue', icon: <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /> },
    { label: 'Folders Owned', value: foldersOwned, color: 'amber', icon: <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" /> },
  ];

  return (
    <div class="space-y-6">
      <h2 class={`text-2xl font-bold ${isDark() ? 'text-white' : 'text-slate-800'}`}>Dashboard</h2>
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 items-stretch">
        {/* Left Column: Stats */}
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 auto-rows-fr">
          <For each={stats}>
            {stat => {
              const colorMap: Record<string, { text: string; bgL: string; bgD: string }> = {
                red: { text: 'text-red-600', bgL: 'bg-red-50 text-red-600', bgD: 'bg-red-900/30 text-red-400' },
                blue: { text: 'text-blue-600', bgL: 'bg-blue-50 text-blue-600', bgD: 'bg-blue-900/30 text-blue-400' },
                amber: { text: 'text-amber-500', bgL: 'bg-amber-50 text-amber-600', bgD: 'bg-amber-900/30 text-amber-400' },
              };
              const c = colorMap[stat.color];
              return (
                <div class={`p-5 rounded-xl border shadow-sm ${cardClass()} ${stat.label === 'Total Owned' ? 'sm:col-span-2' : ''} flex flex-col justify-between`}>
                  <div class="flex justify-between items-start">
                    <div>
                      <p class={`text-xs font-semibold uppercase tracking-wider ${subTextClass()}`}>{stat.label}</p>
                      <p class={`text-3xl md:text-5xl font-bold ${c.text} mt-2`}>{stat.value()}</p>
                    </div>
                    <div class={`p-3 rounded-lg ${isDark() ? c.bgD : c.bgL}`}>
                      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">{stat.icon}</svg>
                    </div>
                  </div>
                  <div class={`mt-6 w-full h-2 rounded-full overflow-hidden ${isDark() ? 'bg-slate-700' : 'bg-slate-100'}`}>
                    <div class={`${stat.color === 'red' ? 'bg-red-600' : stat.color === 'blue' ? 'bg-blue-600' : 'bg-amber-500'} h-2 rounded-full transition-all`} style={{ width: `${totalOwned() ? (stat.value() / totalOwned()) * 100 : 0}%` }}></div>
                  </div>
                </div>
              );
            }}
          </For>
        </div>

        {/* Right Column: Pie Chart */}
        <div class={`p-5 rounded-xl border shadow-sm ${cardClass()} flex flex-col h-full`}>
          <h3 class={`font-semibold text-lg mb-4 ${isDark() ? 'text-white' : 'text-slate-900'}`}>Access Rights Distribution</h3>
          <div class="flex-1 flex flex-col lg:flex-row items-center justify-center gap-8 pie-container relative" style={{ "min-height": "300px" }}>
            <div class="relative flex-shrink-0" style={{ width: "280px", height: "280px" }}>
              <div style={{ width: "100%", height: "100%", "border-radius": "50%", background: conicGradient(), "box-shadow": "0 4px 20px rgba(0,0,0,0.12)" }}></div>
              <For each={sliceLabels()}>
                {(lbl, i) => {
                  const data = pieData();
                  const total = data.reduce((a, d) => a + d.value, 0);
                  let startPct = 0;
                  for (let j = 0; j < i(); j++) startPct += (data[j].value / total) * 100;
                  const thisPct = (data[i()].value / total) * 100;

                  const getClipPath = () => {
                    const steps: string[] = [];
                    const startAngle = (startPct / 100) * Math.PI * 2 - Math.PI / 2;
                    const endAngle = ((startPct + thisPct) / 100) * Math.PI * 2 - Math.PI / 2;
                    const step = (endAngle - startAngle) / 8;
                    for (let s = 0; s <= 8; s++) {
                      const a = startAngle + step * s;
                      steps.push(`${50 + 50 * Math.cos(a)}% ${50 + 50 * Math.sin(a)}%`);
                    }
                    return `polygon(50% 50%, ${50 + 50 * Math.cos((startPct / 100) * Math.PI * 2 - Math.PI / 2)}% ${50 + 50 * Math.sin((startPct / 100) * Math.PI * 2 - Math.PI / 2)}%, ${steps.join(', ')})`;
                  };

                  return (
                    <>
                      <div class="absolute pointer-events-none flex items-center justify-center"
                        style={{ left: `${lbl.x}%`, top: `${lbl.y}%`, transform: "translate(-50%, -50%)", "z-index": 2 }}>
                        <span class="text-white font-bold text-3xl drop-shadow-lg" style={{ "text-shadow": "0 2px 4px rgba(0,0,0,0.6)" }}>{lbl.value}</span>
                      </div>
                      <div class="absolute inset-0 rounded-full cursor-pointer"
                        style={{ "clip-path": getClipPath(), "z-index": 3 }}
                        onMouseMove={(e) => handleSliceMouseMove(lbl, e)}
                        onMouseLeave={handleSliceMouseLeave}>
                      </div>
                    </>
                  );
                }}
              </For>
            </div>

            <div class="flex flex-col gap-3">
              <For each={pieData()}>
                {(d, i) => {
                  const total = pieData().reduce((a, d) => a + d.value, 0);
                  const pct = total > 0 ? Math.round((d.value / total) * 100) : 0;
                  return (
                    <div class="flex items-center gap-3">
                      <div class="w-4 h-4 rounded flex-shrink-0" style={{ "background-color": COLORS[i() % COLORS.length] }}></div>
                      <div class="flex flex-col">
                        <span class={`text-sm font-medium ${isDark() ? 'text-slate-200' : 'text-slate-700'}`}>{d.name}</span>
                        <span class={`text-xs ${isDark() ? 'text-slate-400' : 'text-slate-500'}`}>{d.value} items · {pct}%</span>
                      </div>
                    </div>
                  );
                }}
              </For>
            </div>

            <Show when={tooltip()}>
              {t => (
                <div class={`absolute z-50 px-4 py-2.5 rounded-lg shadow-xl text-sm pointer-events-none ${isDark() ? 'bg-slate-700 text-white border border-slate-600' : 'bg-white text-slate-800 border border-slate-200'}`}
                  style={{ left: `${t().x}px`, top: `${t().y - 50}px`, transform: "translateX(-50%)" }}>
                  <div class="font-bold">{t().name}</div>
                  <div class={isDark() ? 'text-slate-300' : 'text-slate-500'}>{t().value} items · {t().pct}%</div>
                </div>
              )}
            </Show>
          </div>
        </div>
      </div>

      <div>
        <h3 class={`font-semibold text-lg mb-4 ${isDark() ? 'text-white' : 'text-slate-900'}`}>Files per Configuration</h3>
        <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-4">
          <For each={configStats()}>
            {(item, i) => {
              const pastel = PASTEL_COLORS[i() % PASTEL_COLORS.length];
              return (
                <div class="rounded-xl border shadow-sm flex flex-col items-center justify-center py-8 px-4 transition-transform hover:scale-105"
                  style={isDark()
                    ? `background-color: ${pastel.text}22; border-color: ${pastel.text}44;`
                    : `background-color: ${pastel.bg}; border-color: ${pastel.accent};`}>
                  <span class="text-4xl md:text-5xl font-bold" style={`color: ${pastel.text}`}>{item.count}</span>
                  <span class={`text-sm font-medium mt-3 text-center ${isDark() ? 'text-slate-300' : 'text-slate-600'}`}>{item.name}</span>
                </div>
              );
            }}
          </For>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
