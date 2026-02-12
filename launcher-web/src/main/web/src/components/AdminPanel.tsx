import { createSignal, Show, For } from 'solid-js';
import { TargetConfig, ScanHistoryEntry } from '../types';

interface AdminPanelProps {
  configs: TargetConfig[];
  history: ScanHistoryEntry[];
  onAddConfig: (config: TargetConfig) => void;
  onDeleteConfig: (id: string) => void;
  onExecuteScan: (config: TargetConfig) => void;
  onDeleteHistory: (id: string) => void;
}

const AdminPanel = (props: AdminPanelProps) => {
  const [showAddForm, setShowAddForm] = createSignal(false);
  const [newConfig, setNewConfig] = createSignal<Partial<TargetConfig>>({ schedule: 'Manual', type: 'SMB', recursive: 'Yes' });

  const handleSubmit = (e: Event) => {
    e.preventDefault();
    const nc = newConfig();
    if (nc.name && nc.rootPath) {
      props.onAddConfig({
        id: Math.random().toString(36).substr(2, 9),
        name: nc.name,
        rootPath: nc.rootPath,
        type: nc.type || 'SMB',
        server: nc.server || 'localhost',
        recursive: nc.recursive ?? true,
        schedule: nc.schedule || 'Manual',
        created: new Date().toISOString(),
        updated: new Date().toISOString()
      } as TargetConfig);
      setShowAddForm(false);
      setNewConfig({ schedule: 'Manual', type: 'SMB', recursive: 'Yes' });
    }
  };

  return (
    <div class="space-y-8 animate-in fade-in slide-in-from-bottom-2 duration-500">
      <section>
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-xl font-bold text-slate-800">Target Configurations</h3>
          <button
            onClick={() => setShowAddForm(!showAddForm())}
            class="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
          >
            {showAddForm() ? 'Cancel' : 'Add New Target'}
          </button>
        </div>

        <Show when={showAddForm()}>
          <div class="bg-white p-6 rounded-xl border border-blue-200 mb-6 shadow-sm">
            <form onSubmit={handleSubmit} class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="space-y-1">
                <label class="text-xs font-bold text-slate-500 uppercase">Target Name</label>
                <input
                  type="text"
                  required
                  class="w-full p-2 border rounded border-slate-300 focus:ring-2 focus:ring-blue-500 outline-none"
                  value={newConfig().name || ''}
                  onInput={(e) => setNewConfig({ ...newConfig(), name: e.currentTarget.value })}
                />
              </div>
              <div class="space-y-1">
                <label class="text-xs font-bold text-slate-500 uppercase">File Path</label>
                <input
                  type="text"
                  required
                  class="w-full p-2 border rounded border-slate-300 focus:ring-2 focus:ring-blue-500 outline-none"
                  value={newConfig().rootPath || ''}
                  onInput={(e) => setNewConfig({ ...newConfig(), rootPath: e.currentTarget.value })}
                />
              </div>
              <div class="space-y-1">
                <label class="text-xs font-bold text-slate-500 uppercase">Server</label>
                <input
                  type="text"
                  required
                  class="w-full p-2 border rounded border-slate-300 focus:ring-2 focus:ring-blue-500 outline-none"
                  value={newConfig().server || ''}
                  onInput={(e) => setNewConfig({ ...newConfig(), server: e.currentTarget.value })}
                />
              </div>
              <div class="space-y-1">
                <label class="text-xs font-bold text-slate-500 uppercase">Schedule</label>
                <select
                  class="w-full p-2 border rounded border-slate-300 focus:ring-2 focus:ring-blue-500 outline-none"
                  value={newConfig().schedule}
                  onChange={(e) => setNewConfig({ ...newConfig(), schedule: e.currentTarget.value })}
                >
                  <option>Daily</option>
                  <option>Weekly</option>
                  <option>Monthly</option>
                  <option>Manual</option>
                </select>
              </div>
              <div class="md:col-span-2 pt-2">
                <button type="submit" class="w-full bg-blue-600 text-white py-2 rounded font-bold hover:bg-blue-700">
                  Save Configuration
                </button>
              </div>
            </form>
          </div>
        </Show>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <For each={props.configs}>
            {config => (
              <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm group">
                <div class="flex justify-between items-start mb-2">
                  <h4 class="font-bold text-slate-800">{config.name}</h4>
                  <div class="flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button onClick={() => props.onDeleteConfig(config.id)} class="text-slate-400 hover:text-red-500">
                      🗑
                    </button>
                  </div>
                </div>
                <p class="text-xs text-slate-500 font-mono mb-2 truncate" title={config.rootPath}>
                  {config.rootPath}
                </p>
                <div class="flex items-center gap-2 mb-4">
                  <span class="px-2 py-0.5 bg-slate-100 text-slate-600 rounded text-[10px] font-bold">
                    {config.schedule}
                  </span>
                </div>
                <button
                  onClick={() => props.onExecuteScan(config)}
                  class="w-full flex items-center justify-center gap-2 py-2 bg-slate-50 border border-slate-200 rounded text-sm font-semibold text-slate-700 hover:bg-blue-50 hover:border-blue-200 hover:text-blue-600 transition-all"
                >
                  ▶ Execute Scan Now
                </button>
              </div>
            )}
          </For>
        </div>
      </section>

      <section>
        <h3 class="text-xl font-bold text-slate-800 mb-4">Scan History</h3>
        <div class="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm">
          <table class="w-full text-sm text-left">
            <thead class="bg-slate-50 text-slate-500 uppercase text-[10px] font-bold">
              <tr>
                <th class="px-6 py-3">Target</th>
                <th class="px-6 py-3">Timestamp</th>
                <th class="px-6 py-3">Status</th>
                <th class="px-6 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <For each={props.history}>
                {entry => (
                  <tr class="hover:bg-slate-50">
                    <td class="px-6 py-4 font-medium text-slate-800">{entry.configName}</td>
                    <td class="px-6 py-4 text-slate-500">{new Date(entry.timestamp).toLocaleString()}</td>
                    <td class="px-6 py-4">
                      <span class="px-2 py-1 bg-green-100 text-green-700 rounded-full text-[10px] font-bold">
                        {entry.status}
                      </span>
                    </td>
                    <td class="px-6 py-4 text-right">
                      <button onClick={() => props.onDeleteHistory(entry.id)} class="text-slate-400 hover:text-red-600">
                        🗑
                      </button>
                    </td>
                  </tr>
                )}
              </For>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
};

export default AdminPanel;
