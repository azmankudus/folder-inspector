import { Component, createSignal, Show } from 'solid-js';
import { ScanHistoryEntry, Theme } from '../types';
import { isDark } from '../store';
import { DataTable, Column } from './DataTable';
import Modal from './Modal';

interface HistoryProps {
  history: () => ScanHistoryEntry[];
  theme: () => Theme;
}

const History: Component<HistoryProps> = (props) => {
  const [stoppingScanId, setStoppingScanId] = createSignal<string | null>(null);

  const StatusBadge = (status: string) => {
    let cls = 'bg-blue-100 text-blue-800';
    if (status === 'Completed') cls = 'bg-green-100 text-green-800';
    else if (status === 'Failed') cls = 'bg-red-100 text-red-800';
    else if (status === 'Stopped') cls = 'bg-amber-100 text-amber-800';
    return <span class={`px-2 py-1 rounded-full text-[10px] font-bold ${cls}`}>{status}</span>;
  };

  const confirmStopScan = () => {
    if (stoppingScanId()) {
      console.log('Stopping scan:', stoppingScanId());
      // Logic to stop scan would go here
      setStoppingScanId(null);
    }
  };

  // Light/Faded button for "No"
  const secondaryButtonClass = () => isDark()
    ? 'text-slate-400 hover:bg-slate-800 hover:text-slate-200'
    : 'text-slate-500 hover:bg-slate-100 hover:text-slate-700';

  const columns: Column<ScanHistoryEntry>[] = [
    { key: 'timestamp', label: 'Time', sortable: true, filterType: 'date', render: (row) => <span class="text-sm">{new Date(row.timestamp).toLocaleString(undefined, { timeZoneName: 'short' })}</span> },
    { key: 'configName', label: 'Configuration', sortable: true, filterType: 'text', render: (row) => <span class="font-medium">{row.configName}</span> },
    {
      key: 'status',
      label: 'Status',
      sortable: true,
      filterType: 'select',
      filterOptions: ['Completed', 'Failed', 'In Progress', 'Stopped'],
      render: (row) => StatusBadge(row.status)
    },
    {
      key: 'id',
      label: 'Action',
      width: 'w-[60px]',
      render: (row) => (
        <Show when={row.status === 'In Progress'}>
          <button onClick={() => setStoppingScanId(row.id)} class="p-1.5 rounded-full bg-red-100 hover:bg-red-200 text-red-600 transition-colors flex items-center justify-center" title="Stop Scan">
            <i class="fa-solid fa-stop text-sm"></i>
          </button>
        </Show>
      )
    }
  ];

  return (
    <div class="space-y-6">
      <h2 class={`text-2xl font-bold ${isDark() ? 'text-white' : 'text-slate-800'}`}>Scan History</h2>

      <div class="h-[calc(100vh-140px)]">
        <DataTable
          data={props.history()}
          columns={columns}
          emptyMessage="No scan history found."
        />
      </div>

      <Modal
        isOpen={!!stoppingScanId()}
        onClose={() => setStoppingScanId(null)}
        title="Stop Scan"
        icon={<div class="p-2 rounded-full bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400"><i class="fa-solid fa-triangle-exclamation text-lg"></i></div>}
      >
        <div class="text-center p-2">
          <p class="text-sm text-slate-500 dark:text-slate-400 mb-6">
            Are you sure you want to stop this scan?
          </p>
          <div class="flex justify-center gap-3">
            <button onClick={() => setStoppingScanId(null)} class={`px-4 py-2 text-sm font-medium rounded-lg transition-colors ${secondaryButtonClass()}`}>No</button>
            <button onClick={confirmStopScan} class="px-4 py-2 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-lg shadow-sm hover:shadow transition-all">Yes</button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default History;
