import { createSignal, Show, For } from 'solid-js';
import { TargetConfig, Theme } from '../types';
import { isDark } from '../store';
import { wildcardMatch } from '../utils';
import { DataTable, Column } from './DataTable';
import Modal from './Modal';

interface ConfigurationProps {
  configs: () => TargetConfig[];
  onAdd: (config: TargetConfig) => void;
  theme: () => Theme;
}

const Configuration = (props: ConfigurationProps) => {
  const [showAddForm, setShowAddForm] = createSignal(false);
  const [newConfig, setNewConfig] = createSignal<Partial<TargetConfig>>({ type: 'SMB', recursive: 'Yes', schedule: 'Daily 00:00' });
  const [editingConfig, setEditingConfig] = createSignal<TargetConfig | null>(null);
  const [deletingConfig, setDeletingConfig] = createSignal<TargetConfig | null>(null);
  const [executingConfig, setExecutingConfig] = createSignal<TargetConfig | null>(null);

  const handleSave = (e: Event) => {
    e.preventDefault();
    if (newConfig().name && newConfig().server && newConfig().rootPath) {
      if (editingConfig()) {
        props.onAdd({
          ...editingConfig(),
          ...newConfig(),
          updated: new Date().toISOString()
        } as TargetConfig);
      } else {
        props.onAdd({
          ...newConfig(),
          id: crypto.randomUUID(),
          created: new Date().toISOString(),
          updated: new Date().toISOString()
        } as TargetConfig);
      }
      setShowAddForm(false);
      setEditingConfig(null);
      setNewConfig({ type: 'SMB', recursive: 'Yes', schedule: 'Daily 00:00' });
    }
  };

  const startEdit = (config: TargetConfig) => {
    setEditingConfig(config);
    setNewConfig({ ...config });
    setShowAddForm(true);
  };

  const confirmDelete = () => {
    if (deletingConfig()) {
      console.log('Deleting config:', deletingConfig()?.name);
      setDeletingConfig(null);
    }
  };

  const confirmExecute = () => {
    if (executingConfig()) {
      console.log('Executing config:', executingConfig()?.name);
      setExecutingConfig(null);
    }
  };

  const textClass = 'text-slate-800 dark:text-slate-200';
  const inputClass = 'bg-white border-slate-300 text-slate-900 dark:bg-slate-700 dark:border-slate-600 dark:text-white dark:placeholder-slate-400';

  // Light/Faded button for "No"
  const secondaryButtonClass = 'text-slate-500 hover:bg-slate-200 hover:text-slate-700 dark:text-slate-400 dark:hover:bg-slate-900 dark:hover:text-slate-200';

  const columns: Column<TargetConfig>[] = [
    { key: 'name', label: 'Name', sortable: true, filterType: 'text', render: (row) => <span class="font-medium">{row.name}</span> },
    { key: 'type', label: 'Type', sortable: true, filterType: 'select', filterOptions: ['SMB', 'NFS', 'Local', 'S3'] },
    { key: 'server', label: 'Server', sortable: true, filterType: 'text' },
    { key: 'rootPath', label: 'Root Path', sortable: true, filterType: 'text', width: 'min-w-[300px]', render: (row) => <span class="font-mono text-xs break-all">{row.rootPath}</span> },
    { key: 'recursive', label: 'Recursive', sortable: true, filterType: 'select', filterOptions: ['Yes', 'No'], width: 'w-[100px]', render: (row) => <span>{row.recursive}</span> },
    { key: 'schedule', label: 'Schedule', sortable: true, filterType: 'text', width: 'w-[140px]' },
    {
      key: 'id',
      label: 'Action',
      width: 'w-[140px]',
      render: (row) => (
        <div class="flex items-center gap-3">
          <button onClick={() => setExecutingConfig(row)} class="w-8 h-8 flex items-center justify-center rounded-full bg-blue-100 hover:bg-blue-200 text-blue-700 transition-colors" title="Execute Scan">
            <i class="fa-solid fa-play text-xs pl-0.5"></i>
          </button>
          <button onClick={() => startEdit(row)} class="w-8 h-8 flex items-center justify-center rounded-full bg-amber-100 hover:bg-amber-200 text-amber-700 transition-colors" title="Edit">
            <i class="fa-solid fa-pen text-xs"></i>
          </button>
          <button onClick={() => setDeletingConfig(row)} class="w-8 h-8 flex items-center justify-center rounded-full bg-red-100 hover:bg-red-200 text-red-700 transition-colors" title="Delete">
            <i class="fa-solid fa-trash text-xs"></i>
          </button>
        </div>
      )
    }
  ];

  return (
    <div class="h-[calc(100vh-140px)] flex flex-col gap-6">
      <div class="flex justify-between items-center shrink-0">
        <h2 class={`text-2xl font-bold ${textClass}`}>Configurations</h2>
        <button onClick={() => { setEditingConfig(null); setNewConfig({ type: 'SMB', recursive: 'Yes', schedule: 'Daily 00:00' }); setShowAddForm(true); }} class={`px-4 py-2 rounded-lg font-medium transition-colors bg-gradient-to-br from-red-600 to-slate-900 text-white shadow-sm shadow-red-200 dark:shadow-none hover:shadow-md`}>
          <i class="fa-solid fa-plus mr-2"></i>Add Configuration
        </button>
      </div>

      <Modal
        isOpen={showAddForm()}
        onClose={() => setShowAddForm(false)}
        title={editingConfig() ? 'Edit Configuration' : 'New Configuration'}
        maxWidth="max-w-2xl"
        icon={
          editingConfig() ?
            <div class="p-2 rounded-full bg-amber-100 text-amber-600 dark:bg-amber-900/30 dark:text-amber-400"><i class="fa-solid fa-pen text-lg"></i></div> :
            <div class="p-2 rounded-full bg-emerald-100 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400"><i class="fa-solid fa-plus text-lg"></i></div>
        }
      >
        <form onSubmit={handleSave} class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1"><label class="text-xs uppercase font-bold text-slate-500 dark:text-slate-400">Name</label><input required class={`w-full p-2.5 rounded-lg border ${inputClass}`} value={newConfig().name || ''} onInput={(e) => setNewConfig({ ...newConfig(), name: e.currentTarget.value })} /></div>
          <div class="space-y-1"><label class="text-xs uppercase font-bold text-slate-500 dark:text-slate-400">Type</label><select class={`w-full p-2.5 rounded-lg border ${inputClass}`} value={newConfig().type} onChange={(e) => setNewConfig({ ...newConfig(), type: e.currentTarget.value as any })}><option value="SMB">SMB</option><option value="NFS">NFS</option><option value="Local">Local</option><option value="S3">S3</option></select></div>
          <div class="space-y-1"><label class="text-xs uppercase font-bold text-slate-500 dark:text-slate-400">Server / Host</label><input required class={`w-full p-2.5 rounded-lg border ${inputClass}`} value={newConfig().server || ''} onInput={(e) => setNewConfig({ ...newConfig(), server: e.currentTarget.value })} /></div>

          <div class="space-y-1"><label class="text-xs uppercase font-bold text-slate-500 dark:text-slate-400">Port</label><input type="number" class={`w-full p-2.5 rounded-lg border ${inputClass}`} value={newConfig().port || ''} onInput={(e) => setNewConfig({ ...newConfig(), port: parseInt(e.currentTarget.value) || undefined })} placeholder="Optional" /></div>
          <div class="space-y-1"><label class="text-xs uppercase font-bold text-slate-500 dark:text-slate-400">Username</label><input class={`w-full p-2.5 rounded-lg border ${inputClass}`} value={newConfig().username || ''} onInput={(e) => setNewConfig({ ...newConfig(), username: e.currentTarget.value })} placeholder="Optional" /></div>
          <div class="space-y-1"><label class="text-xs uppercase font-bold text-slate-500 dark:text-slate-400">Password</label><input type="password" class={`w-full p-2.5 rounded-lg border ${inputClass}`} value={newConfig().password || ''} onInput={(e) => setNewConfig({ ...newConfig(), password: e.currentTarget.value })} placeholder="Optional" /></div>

          <div class="space-y-1"><label class="text-xs uppercase font-bold text-slate-500 dark:text-slate-400">Root Path</label><input required class={`w-full p-2.5 rounded-lg border ${inputClass}`} value={newConfig().rootPath || ''} onInput={(e) => setNewConfig({ ...newConfig(), rootPath: e.currentTarget.value })} /></div>
          <div class="space-y-1"><label class="text-xs uppercase font-bold text-slate-500 dark:text-slate-400">Recursive</label><select class={`w-full p-2.5 rounded-lg border ${inputClass}`} value={newConfig().recursive} onChange={(e) => setNewConfig({ ...newConfig(), recursive: e.currentTarget.value })}><option value="Yes">Yes</option><option value="No">No</option></select></div>
          <div class="space-y-1"><label class="text-xs uppercase font-bold text-slate-500 dark:text-slate-400">Schedule</label><select class={`w-full p-2.5 rounded-lg border ${inputClass}`} value={newConfig().schedule} onChange={(e) => setNewConfig({ ...newConfig(), schedule: e.currentTarget.value })}><option>Manual</option><option>Hourly</option><option>Daily 00:00</option><option>Weekly</option></select></div>

          <Show when={editingConfig()}>
            <div class="space-y-1"><label class="text-xs uppercase font-bold text-slate-500 dark:text-slate-400">Created</label><div class="p-2.5 text-slate-600 dark:text-slate-400">{new Date(editingConfig()!.created).toLocaleString()}</div></div>
            <div class="space-y-1"><label class="text-xs uppercase font-bold text-slate-500 dark:text-slate-400">Updated</label><div class="p-2.5 text-slate-600 dark:text-slate-400">{new Date(editingConfig()!.updated).toLocaleString()}</div></div>
          </Show>

          <div class="col-span-1 md:col-span-2 flex justify-end gap-3 mt-4 border-t pt-4 border-slate-200 dark:border-slate-700">
            <button type="button" onClick={() => setShowAddForm(false)} class={`px-4 py-2 rounded-lg font-medium transition-colors ${secondaryButtonClass}`}>Cancel</button>
            <button type="submit" class="px-4 py-2 rounded-lg font-medium transition-colors bg-gradient-to-br from-red-600 to-slate-900 text-white shadow-sm hover:shadow-md">{editingConfig() ? 'Update' : 'Save'}</button>
          </div>
        </form>
      </Modal>

      <Modal
        isOpen={!!deletingConfig()}
        onClose={() => setDeletingConfig(null)}
        title="Confirm Deletion"
        icon={<div class="p-2 rounded-full bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400"><i class="fa-solid fa-triangle-exclamation text-lg"></i></div>}
      >
        <div class="text-center p-2">
          <p class="text-sm text-slate-500 dark:text-slate-400 mb-6">
            Are you sure you want to delete <strong>{deletingConfig()?.name}</strong>? This action cannot be undone.
          </p>
          <div class="flex justify-center gap-3">
            <button onClick={() => setDeletingConfig(null)} class={`px-4 py-2 text-sm font-medium rounded-lg transition-colors ${secondaryButtonClass}`}>No</button>
            <button onClick={confirmDelete} class="px-4 py-2 text-sm font-medium text-white bg-gradient-to-br from-red-600 to-slate-900 rounded-lg shadow-sm hover:shadow-md transition-all">Yes</button>
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={!!executingConfig()}
        onClose={() => setExecutingConfig(null)}
        title="Execute scan now"
        icon={<div class="p-2 rounded-full bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400"><i class="fa-solid fa-play text-lg"></i></div>}
      >
        <div class="text-center p-2">
          <p class="text-sm text-slate-500 dark:text-slate-400 mb-6">
            Are you sure you want to execute scan for <strong>{executingConfig()?.name}</strong>?
          </p>
          <div class="flex justify-center gap-3">
            <button onClick={() => setExecutingConfig(null)} class={`px-4 py-2 text-sm font-medium rounded-lg transition-colors ${secondaryButtonClass}`}>No</button>
            <button onClick={confirmExecute} class="px-4 py-2 text-sm font-medium text-white bg-gradient-to-br from-red-600 to-slate-900 rounded-lg shadow-sm hover:shadow-md transition-all">Yes</button>
          </div>
        </div>
      </Modal>

      <div class="flex-1 min-h-0">
        <DataTable
          data={props.configs()}
          columns={columns}
          emptyMessage="No configurations found."
        />
      </div>
    </div>
  );
};

export default Configuration;
