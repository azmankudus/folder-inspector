import { createSignal, createMemo, createEffect, Show, For } from 'solid-js';
import { TargetConfig, FileEntry, Theme } from '../types';
import { formatBytes } from '../constants';
import { wildcardMatch } from '../utils';
import { DataTable, Column } from './DataTable';
import FileDetailsModal from './FileDetailsModal';
import Modal from './Modal';

interface SearchProps {
  configs: () => TargetConfig[];
  files: () => FileEntry[];
  theme: () => Theme;
}

const Search = (props: SearchProps) => {
  const isDark = () => props.theme() === 'Dark';
  const [selectedConfigId, setSelectedConfigId] = createSignal<string>(props.configs()[0]?.id || '');
  const [selectedFile, setSelectedFile] = createSignal<FileEntry | null>(null);

  const textClass = () => isDark() ? 'text-slate-200' : 'text-slate-800';
  const bgClass = () => isDark() ? 'bg-slate-800' : 'bg-white';
  const borderClass = () => isDark() ? 'border-slate-700' : 'border-slate-200';
  const inputClass = () => isDark() ? 'bg-slate-700 border-slate-600 text-white placeholder-slate-400 focus:ring-red-500' : 'bg-white border-slate-300 text-slate-900 focus:ring-red-500';

  const selectedConfig = () => props.configs().find(c => c.id === selectedConfigId());
  const closeDetails = () => setSelectedFile(null);

  // State for Config Details Modal
  const [showConfigDetails, setShowConfigDetails] = createSignal(false);

  const columns: Column<FileEntry>[] = [
    {
      key: 'name',
      label: 'Name',
      sortable: true,
      filterType: 'text',
      width: 'min-w-[150px]',
      render: (row) => (
        <span class="font-medium truncate max-w-[200px] block" title={row.name}>
          <span class={row.type === 'Directory' ? 'text-amber-500 mr-2 no-underline' : 'text-slate-400 mr-2 no-underline'}>
            <i class={`fa-solid ${row.type === 'Directory' ? 'fa-folder' : 'fa-file'}`}></i>
          </span>
          {row.name}
        </span>
      )
    },
    { key: 'parent', label: 'Parent', sortable: true, filterType: 'text', width: 'min-w-[250px] md:min-w-[400px]', render: (row) => <span class="truncate max-w-[400px] text-xs font-mono opacity-80 block" title={row.parent}>{row.parent}</span> },
    { key: 'type', label: 'Type', sortable: true, filterType: 'select', filterOptions: ['File', 'Directory'], render: (row) => <span class={`px-2 py-0.5 rounded text-[10px] font-bold border ${row.type === 'Directory' ? 'bg-amber-100 text-amber-700 border-amber-200' : 'bg-slate-100 text-slate-600 border-slate-200'}`}>{row.type}</span> },
    { key: 'size', label: 'Size', sortable: true, filterType: 'number', width: 'w-[140px]', render: (row) => <span class="font-mono text-xs text-slate-600 dark:text-slate-400">{formatBytes(row.size)}</span> },
    { key: 'modified', label: 'Modified', sortable: true, filterType: 'date', render: (row) => <span class="text-xs opacity-80 whitespace-nowrap">{new Date(row.modified).toLocaleString(undefined, { timeZoneName: 'short' })}</span> },
    {
      key: 'owner', // using 'owner' as key for sorting but label is Access
      label: 'Access',
      sortable: true,
      filterType: 'text',
      render: (row) => {
        const parts = [];
        if (row.owner) parts.push('Owner');
        if (row.group) parts.push('Group');
        if (row.acl && row.acl.length > 0) parts.push('ACL');
        return <span class="text-xs text-slate-500">{parts.join(', ')}</span>;
      }
    },
    {
      key: 'id', // Dummy key for action column
      label: 'Action',
      width: 'w-[60px]',
      render: (row) => (
        <button
          onClick={() => setSelectedFile(row)}
          class="p-1.5 rounded-full bg-blue-100 hover:bg-blue-200 text-blue-700 transition-colors"
          title="View Details"
        >
          <i class="fa-solid fa-circle-info text-sm"></i>
        </button>
      )
    }
  ];

  return (
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <h2 class={`text-2xl font-bold ${isDark() ? 'text-white' : 'text-slate-800'}`}>Search</h2>

        <div class="flex items-center gap-2 w-full md:w-auto">
          <div class="relative w-full md:w-64">
            <select value={selectedConfigId()} onChange={(e) => setSelectedConfigId(e.currentTarget.value)} class={`w-full p-2 pr-8 rounded-lg border outline-none focus:ring-2 appearance-none ${inputClass()}`}>
              <option value="">-- Select Configuration --</option>
              <For each={props.configs()}>{c => <option value={c.id}>{c.name}</option>}</For>
            </select>
            <div class="absolute inset-y-0 right-0 flex items-center px-3 pointer-events-none text-slate-500">
              <i class="fa-solid fa-chevron-down text-xs"></i>
            </div>
          </div>
          <Show when={selectedConfig()}>
            <button
              onClick={() => setShowConfigDetails(true)}
              class="p-2 rounded-lg transition-colors shrink-0 bg-blue-100 hover:bg-blue-200 text-blue-700"
              title="Configuration Details"
            >
              <i class="fa-solid fa-file-lines text-lg"></i>
            </button>
          </Show>
        </div>
      </div>

      <Show when={selectedConfig()}>
        <div class="flex flex-col gap-4">
          <DataTable
            data={props.files()}
            columns={columns}
            emptyMessage="No files found."
            defaultPageSize={10}
          />
        </div>
      </Show>

      {/* Config Details Modal */}
      <Modal
        isOpen={showConfigDetails() && !!selectedConfig()}
        onClose={() => setShowConfigDetails(false)}
        title="Configuration Details"
        icon={<div class="p-2 rounded-full bg-blue-100 text-blue-700"><i class="fa-solid fa-file-lines text-lg"></i></div>}
      >
        <Show when={selectedConfig()}>
          <div class="space-y-4">
            <div><span class="text-xs font-bold uppercase text-slate-500">Name</span><div class={`text-lg font-medium ${textClass()}`}>{selectedConfig()?.name}</div></div>
            <div><span class="text-xs font-bold uppercase text-slate-500">Type</span><div class={`font-mono ${textClass()}`}>{selectedConfig()?.type}</div></div>
            <div><span class="text-xs font-bold uppercase text-slate-500">Host / IP</span><div class={`font-mono ${textClass()}`}>{selectedConfig()?.server}</div></div>
            <div><span class="text-xs font-bold uppercase text-slate-500">Port</span><div class={`font-mono ${textClass()}`}>{selectedConfig()?.port || '-'}</div></div>
            <div><span class="text-xs font-bold uppercase text-slate-500">Username</span><div class={`font-mono ${textClass()}`}>{selectedConfig()?.username || '-'}</div></div>
            <div><span class="text-xs font-bold uppercase text-slate-500">Root Path</span><div class={`font-mono ${textClass()} break-all`}>{selectedConfig()?.rootPath}</div></div>

            {/* Close Button at Bottom Left */}
            <div class="flex justify-start pt-4 mt-2 border-t border-slate-200 dark:border-slate-700">
              <button onClick={() => setShowConfigDetails(false)} class="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 hover:bg-slate-50 rounded-lg shadow-sm transition-colors dark:bg-slate-800 dark:border-slate-600 dark:text-slate-300 dark:hover:bg-slate-700">Close</button>
            </div>
          </div>
        </Show>
      </Modal>

      <Show when={selectedFile()}>
        <FileDetailsModal file={selectedFile()!} onClose={closeDetails} />
      </Show>
    </div>
  );
};

export default Search;
