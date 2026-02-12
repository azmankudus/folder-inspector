import { createSignal, createMemo, createEffect, Show, For } from 'solid-js';
import { TargetConfig, FileEntry } from '../types';
import { formatBytes } from '../constants';
import { wildcardMatch } from '../utils';
import { isDark } from '../store';
import { DataTable, Column } from './DataTable';

interface FolderTableProps {
  config: TargetConfig;
  files: FileEntry[];
  onBack: () => void;
  isLoading: boolean;
}

const FolderTable = (props: FolderTableProps) => {
  const textClass = () => isDark() ? 'text-slate-200' : 'text-slate-800';

  const columns: Column<FileEntry>[] = [
    {
      key: 'name',
      label: 'Name',
      sortable: true,
      filterType: 'text',
      width: 'min-w-[200px]',
      render: (row) => (
        <span class="font-medium truncate max-w-[200px] block" title={row.name}>
          <span class={row.type === 'Directory' ? 'text-amber-500 mr-2 p-0.5' : 'text-slate-400 mr-2 p-0.5'}>{row.type === 'Directory' ? '📁' : '📄'}</span>
          {row.name}
        </span>
      )
    },
    {
      key: 'parent',
      label: 'Parent',
      sortable: true,
      filterType: 'text',
      render: (row) => <span class="truncate max-w-[200px] text-xs font-mono block" title={row.parent}>{row.parent}</span>
    },
    {
      key: 'type',
      label: 'Type',
      sortable: true,
      filterType: 'select',
      filterOptions: ['File', 'Directory'],
      render: (row) => <span class={`px-2 py-0.5 rounded text-[10px] font-bold border ${row.type === 'Directory' ? 'bg-amber-50 text-amber-700 border-amber-200' : 'bg-slate-50 text-slate-600 border-slate-200'}`}>{row.type}</span>
    },
    {
      key: 'size',
      label: 'Size',
      sortable: true,
      filterType: 'number',
      render: (row) => <span class="font-mono text-xs text-slate-600">{formatBytes(row.size)}</span>
    },
    {
      key: 'created',
      label: 'Created',
      sortable: true,
      filterType: 'date',
      render: (row) => <span class="text-xs whitespace-nowrap text-slate-500">{new Date(row.created).toLocaleString()}</span>
    },
    {
      key: 'modified',
      label: 'Modified',
      sortable: true,
      filterType: 'date',
      render: (row) => <span class="text-xs whitespace-nowrap text-slate-500">{new Date(row.modified).toLocaleString()}</span>
    },
    { key: 'owner', label: 'Owner', sortable: true, filterType: 'text', render: (row) => <span class="text-xs text-slate-600">{row.owner}</span> },
    { key: 'group', label: 'Group', sortable: true, filterType: 'text', render: (row) => <span class="text-xs text-slate-600">{row.group}</span> },
  ];

  return (
    <div class="animate-in fade-in slide-in-from-bottom-2 duration-500">
      <div class="flex flex-col gap-4">
        <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-4 flex flex-col md:flex-row justify-between items-center gap-4 dark:bg-slate-800 dark:border-slate-700">
          <div class="flex items-center gap-4">
            <button onClick={props.onBack} class="p-2 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-full transition-colors text-slate-500" title="Back">
              <svg class="w-5 h-5 p-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
            </button>
            <div>
              <h3 class={`font-bold ${textClass()}`}>{props.config.name}</h3>
              <p class="text-xs text-slate-500 font-mono">{props.config.rootPath}</p>
            </div>
          </div>
        </div>

        <DataTable
          data={props.files}
          columns={columns}
          isLoading={props.isLoading}
          emptyMessage="No files found in this folder."
        />
      </div>
    </div>
  );
};


export default FolderTable;
