import { Component, createSignal, createResource, For, Show } from 'solid-js';
import { Title } from '@solidjs/meta';

interface ScheduleInfo {
  name: string;
  cron: string;
  path: string;
  snapshotName: string;
}

const fetchSchedules = async () => {
  const response = await fetch('/api/v1/schedules');
  if (!response.ok) throw new Error('Failed to fetch schedules');
  return await response.json() as ScheduleInfo[];
};

const Service: Component = () => {
  const [schedules, { refetch }] = createResource(fetchSchedules);
  const [name, setName] = createSignal('');
  const [cron, setCron] = createSignal('0 0 * * * ?'); // Default: every hour
  const [path, setPath] = createSignal('');
  const [snapshotName, setSnapshotName] = createSignal('');
  const [loading, setLoading] = createSignal(false);
  const [error, setError] = createSignal('');

  const handleSchedule = async (e: Event) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const response = await fetch('/api/v1/schedules', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: name(), cron: cron(), path: path(), snapshotName: snapshotName() }),
      });
      if (response.ok) {
        setName('');
        setPath('');
        setSnapshotName('');
        refetch();
      } else {
        const msg = await response.text();
        setError(msg || 'Failed to schedule scan');
      }
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (scheduleName: string) => {
    if (!confirm(`Are you sure you want to delete the schedule "${scheduleName}"?`)) return;
    try {
      const response = await fetch(`/api/v1/schedules/${scheduleName}`, { method: 'DELETE' });
      if (response.ok) refetch();
    } catch (err: any) {
      alert(err.message);
    }
  };

  return (
    <div class="animate-in fade-in slide-in-from-bottom-4 duration-700">
      <Title>Service - Folder Inspector</Title>

      <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
        <div>
          <h1 class="text-3xl font-black tracking-tight text-slate-900 dark:text-white">Background Services</h1>
          <p class="text-slate-500 dark:text-slate-400 mt-1">Manage scheduled folder scans and automation.</p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Schedule Form */}
        <div class="lg:col-span-1">
          <div class="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm">
            <h2 class="text-lg font-bold mb-4 flex items-center gap-2">
              <i class="fa-solid fa-calendar-plus text-blue-600"></i>
              New Scheduled Scan
            </h2>
            <form onSubmit={handleSchedule} class="space-y-4">
              <div>
                <label class="block text-sm font-medium mb-1.5">Schedule Name</label>
                <input
                  type="text"
                  value={name()}
                  onInput={e => setName(e.currentTarget.value)}
                  class="w-full px-4 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                  placeholder="Daily Report"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium mb-1.5">Cron Expression</label>
                <input
                  type="text"
                  value={cron()}
                  onInput={e => setCron(e.currentTarget.value)}
                  class="w-full px-4 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                  placeholder="0 0 * * * ?"
                  required
                />
                <p class="text-xs text-slate-500 mt-1">Format: sec min hour day month dow</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1.5">Folder Path</label>
                <input
                  type="text"
                  value={path()}
                  onInput={e => setPath(e.currentTarget.value)}
                  class="w-full px-4 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                  placeholder="/var/log"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium mb-1.5">Snapshot Name</label>
                <input
                  type="text"
                  value={snapshotName()}
                  onInput={e => setSnapshotName(e.currentTarget.value)}
                  class="w-full px-4 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                  placeholder="daily-log-scan"
                  required
                />
              </div>
              <Show when={error()}>
                <div class="p-3 rounded-lg bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 text-sm">
                  {error()}
                </div>
              </Show>
              <button
                type="submit"
                disabled={loading()}
                class="w-full py-3 px-4 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-bold rounded-xl transition-all shadow-lg shadow-blue-500/20"
              >
                {loading() ? 'Scheduling...' : 'Schedule Scan'}
              </button>
            </form>
          </div>
        </div>

        {/* Schedule List */}
        <div class="lg:col-span-2">
          <div class="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 overflow-hidden shadow-sm">
            <div class="p-6 border-b border-slate-200 dark:border-slate-800 flex justify-between items-center">
              <h2 class="text-lg font-bold flex items-center gap-2">
                <i class="fa-solid fa-list-check text-indigo-600"></i>
                Active Schedules
              </h2>
              <button onClick={refetch} class="p-2 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition-colors">
                <i class="fa-solid fa-rotate text-slate-500"></i>
              </button>
            </div>

            <Show when={schedules.loading}>
              <div class="p-12 text-center text-slate-500">Loading schedules...</div>
            </Show>

            <Show when={!schedules.loading && schedules()}>
              <div class="overflow-x-auto">
                <table class="w-full">
                  <thead class="bg-slate-50 dark:bg-slate-950 text-left text-xs font-bold uppercase tracking-wider text-slate-500">
                    <tr>
                      <th class="px-6 py-4">Name</th>
                      <th class="px-6 py-4">Cron / Schedule</th>
                      <th class="px-6 py-4">Path</th>
                      <th class="px-6 py-4">Snapshot</th>
                      <th class="px-6 py-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100 dark:divide-slate-800">
                    <For each={schedules()} fallback={
                      <tr>
                        <td colspan="5" class="px-6 py-12 text-center text-slate-500">No scheduled scans found.</td>
                      </tr>
                    }>
                      {(s) => (
                        <tr class="hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition-colors">
                          <td class="px-6 py-4 font-semibold">{s.name}</td>
                          <td class="px-6 py-4">
                            <span class="px-2 py-1 rounded-md bg-indigo-50 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 font-mono text-xs">
                              {s.cron}
                            </span>
                          </td>
                          <td class="px-6 py-4 text-sm text-slate-500 truncate max-w-[150px]">{s.path}</td>
                          <td class="px-6 py-4 text-sm">{s.snapshotName}</td>
                          <td class="px-6 py-4 text-right">
                            <button
                              onClick={() => handleDelete(s.name)}
                              class="p-2 text-slate-400 hover:text-red-600 dark:hover:text-red-400 transition-colors"
                            >
                              <i class="fa-solid fa-trash-can"></i>
                            </button>
                          </td>
                        </tr>
                      )}
                    </For>
                  </tbody>
                </table>
              </div>
            </Show>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Service;
