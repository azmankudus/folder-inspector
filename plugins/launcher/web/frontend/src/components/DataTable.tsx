import { Component, For, createSignal, createMemo, Show, JSX } from 'solid-js';
import { isDark } from '../store';

export interface Column<T> {
  key: keyof T;
  label: string;
  sortable?: boolean;
  filterType?: 'text' | 'select' | 'number' | 'date';
  filterOptions?: string[];
  render?: (row: T) => JSX.Element;
  width?: string;
}

interface DataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  emptyMessage?: string;
  defaultPageSize?: number;
}

export function DataTable<T extends Record<string, any>>(props: DataTableProps<T>) {
  const [currentPage, setCurrentPage] = createSignal(1);
  const [pageSize, setPageSize] = createSignal(props.defaultPageSize || 10);
  const [sortField, setSortField] = createSignal<keyof T | null>(null);
  const [sortOrder, setSortOrder] = createSignal<'asc' | 'desc'>('asc');
  const [filters, setFilters] = createSignal<Record<string, string>>({});
  const [selectedRows, setSelectedRows] = createSignal<Set<string>>(new Set());
  const [showDownloadMenu, setShowDownloadMenu] = createSignal(false);

  // Computed data processing
  const processedData = createMemo(() => {
    let result = [...props.data];

    // Filtering
    Object.entries(filters()).forEach(([key, value]) => {
      if (value) {
        result = result.filter(item => {
          const itemValue = String(item[key]).toLowerCase();
          return itemValue.includes(value.toLowerCase());
        });
      }
    });

    // Sorting
    if (sortField()) {
      result.sort((a, b) => {
        const aVal = a[sortField()!];
        const bVal = b[sortField()!];

        if (aVal < bVal) return sortOrder() === 'asc' ? -1 : 1;
        if (aVal > bVal) return sortOrder() === 'asc' ? 1 : -1;
        return 0;
      });
    }

    return result;
  });

  const totalPages = () => Math.ceil(processedData().length / pageSize());

  const paginatedData = createMemo(() => {
    const start = (currentPage() - 1) * pageSize();
    return processedData().slice(start, start + pageSize());
  });

  const handleSort = (key: keyof T) => {
    if (sortField() === key) {
      setSortOrder(o => o === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(key);
      setSortOrder('asc');
    }
  };

  const toggleSelectAll = () => {
    if (selectedRows().size === paginatedData().length) {
      setSelectedRows(new Set());
    } else {
      setSelectedRows(new Set(paginatedData().map((_, i) => String(i))));
    }
  };

  const toggleSelectRow = (id: string) => {
    const newSet = new Set(selectedRows());
    if (newSet.has(id)) newSet.delete(id);
    else newSet.add(id);
    setSelectedRows(newSet);
  };

  const downloadData = (format: 'csv' | 'json') => {
    const dataToDownload = processedData();
    let content = '';
    let filename = `export.${format}`;
    let mimeType = '';

    if (format === 'json') {
      content = JSON.stringify(dataToDownload, null, 2);
      mimeType = 'application/json';
    } else {
      // Simple CSV export
      const headers = props.columns.map(c => c.label).join(',');
      const rows = dataToDownload.map(row =>
        props.columns.map(c => JSON.stringify(row[c.key])).join(',')
      ).join('\n');
      content = `${headers}\n${rows}`;
      mimeType = 'text/csv';
    }

    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    setShowDownloadMenu(false);
  };

  const textClass = () => isDark() ? 'text-slate-300' : 'text-slate-600';
  const borderClass = () => isDark() ? 'border-slate-700' : 'border-slate-200';
  const headerClass = () => isDark() ? 'bg-slate-800 text-slate-200' : 'bg-slate-50 text-slate-700';
  const rowClass = () => isDark() ? 'hover:bg-slate-800/50 even:bg-slate-900/50' : 'hover:bg-slate-50 even:bg-slate-50/50';

  const btnClass = () => `p-1.5 rounded transition-colors disabled:opacity-30 disabled:cursor-not-allowed ${isDark() ? 'hover:bg-slate-700 text-slate-400' : 'hover:bg-slate-100 text-slate-500'}`;

  return (
    <div class={`flex flex-col h-full rounded-lg border shadow-sm ${isDark() ? 'bg-slate-900 border-slate-700' : 'bg-white border-slate-200'}`}>

      {/* Toolbar */}
      <div class={`flex justify-between items-center p-3 border-b ${borderClass()}`}>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2">
            <span class={`text-xs font-medium uppercase ${textClass()}`}>Rows</span>
            <select
              value={pageSize()}
              onChange={(e) => { setPageSize(parseInt(e.currentTarget.value)); setCurrentPage(1); }}
              class={`text-xs p-1 rounded border outline-none cursor-pointer ${isDark() ? 'bg-slate-800 border-slate-600 text-slate-200' : 'bg-white border-slate-300 text-slate-700'}`}
            >
              <option value="10">10</option>
              <option value="25">25</option>
              <option value="50">50</option>
              <option value="100">100</option>
            </select>
          </div>

          <div class={`text-xs ${textClass()}`}>
            Showing <strong>{Math.min((currentPage() - 1) * pageSize() + 1, processedData().length)}</strong> to <strong>{Math.min(currentPage() * pageSize(), processedData().length)}</strong> of <strong>{processedData().length}</strong>
          </div>
        </div>

        <div class="flex items-center gap-3">
          {/* Pagination */}
          <div class="flex items-center gap-1">
            <button onClick={() => setCurrentPage(1)} disabled={currentPage() === 1} class={btnClass()} title="First">
              <i class="fa-solid fa-angles-left text-xs"></i>
            </button>
            <button onClick={() => setCurrentPage(p => Math.max(1, p - 1))} disabled={currentPage() === 1} class={btnClass()} title="Previous">
              <i class="fa-solid fa-angle-left text-xs"></i>
            </button>

            <div class="flex items-center gap-2 mx-2">
              <span class={`text-[10px] font-medium uppercase ${textClass()}`}>Page</span>
              <input
                type="text"
                value={currentPage()}
                onInput={(e) => {
                  const val = parseInt(e.currentTarget.value);
                  if (!isNaN(val)) setCurrentPage(Math.max(1, Math.min(val, totalPages())));
                }}
                class={`w-10 text-center p-0.5 rounded border-b bg-transparent outline-none text-xs font-bold ${isDark() ? 'border-slate-600 text-slate-200 focus:border-blue-500' : 'border-slate-300 text-slate-800 focus:border-blue-500'}`}
              />
              <span class={`text-[10px] font-medium uppercase ${textClass()}`}>of {totalPages() || 1}</span>
            </div>

            <button onClick={() => setCurrentPage(p => Math.min(totalPages(), p + 1))} disabled={currentPage() === totalPages() || totalPages() === 0} class={btnClass()} title="Next">
              <i class="fa-solid fa-angle-right text-xs"></i>
            </button>
            <button onClick={() => setCurrentPage(totalPages())} disabled={currentPage() === totalPages() || totalPages() === 0} class={btnClass()} title="Last">
              <i class="fa-solid fa-angles-right text-xs"></i>
            </button>
          </div>

          <div class={`h-4 w-px ${isDark() ? 'bg-slate-700' : 'bg-slate-300'}`}></div>

          {/* Download Menu */}
          <div class="relative">
            <button onClick={() => setShowDownloadMenu(!showDownloadMenu())} class={`p-1.5 rounded border transition-colors flex items-center gap-1 text-xs font-medium ${isDark() ? 'border-slate-600 hover:bg-slate-700 hover:text-red-400 text-slate-300' : 'border-slate-300 hover:bg-red-50 hover:text-red-600 text-slate-600'}`} title="Download">
              <i class="fa-solid fa-download text-[10px] mr-1"></i>
              <span>Download</span>
              <i class="fa-solid fa-chevron-down text-[8px] ml-0.5"></i>
            </button>
            <Show when={showDownloadMenu()}>
              <div class="fixed inset-0 z-10" onClick={() => setShowDownloadMenu(false)}></div>
              <div class={`absolute right-0 mt-1 w-32 rounded-lg shadow-lg border z-20 overflow-hidden ${isDark() ? 'bg-slate-800 border-slate-600' : 'bg-white border-slate-200'}`}>
                <button onClick={() => downloadData('csv')} class={`w-full text-left px-4 py-2 text-xs hover:bg-opacity-10 transition-colors ${isDark() ? 'text-slate-200 hover:bg-white' : 'text-slate-700 hover:bg-slate-100'}`}>Export CSV</button>
                <button onClick={() => downloadData('json')} class={`w-full text-left px-4 py-2 text-xs hover:bg-opacity-10 transition-colors ${isDark() ? 'text-slate-200 hover:bg-white' : 'text-slate-700 hover:bg-slate-100'}`}>Export JSON</button>
              </div>
            </Show>
          </div>
        </div>
      </div>

      {/* Table Content */}
      <div class="flex-1 overflow-auto relative">
        <table class="w-full text-left text-sm border-collapse">
          <thead class={`sticky top-0 z-10 uppercase text-xs font-bold tracking-wider shadow-sm ${headerClass()}`}>
            <tr>
              <For each={props.columns}>{col => (
                <th
                  class={`px-4 py-3 whitespace-nowrap cursor-pointer select-none transition-colors ${col.width || ''} ${isDark() ? 'hover:bg-slate-700' : 'hover:bg-slate-100'}`}
                  onClick={() => col.sortable && handleSort(col.key)}
                >
                  <div class="flex items-center">
                    {col.label}
                    <Show when={col.sortable}>
                      <span class={`ml-1 text-[10px] ${sortField() === col.key ? 'text-blue-600' : 'text-slate-300'}`}>
                        <i class={`fa-solid ${sortField() !== col.key ? 'fa-sort' : sortOrder() === 'asc' ? 'fa-sort-up' : 'fa-sort-down'}`}></i>
                      </span>
                    </Show>
                  </div>
                </th>
              )}</For>
            </tr>
            {/* Filter Row */}
            <tr class={`${isDark() ? 'bg-slate-800/50' : 'bg-slate-50/80'}`}>
              <For each={props.columns}>{col => (
                <th class="px-2 py-1">
                  <Show when={col.filterType}>
                    <Show
                      when={col.filterType === 'select'}
                      fallback={
                        <input
                          type={col.filterType === 'number' ? 'number' : 'text'}
                          placeholder={`Filter...`}
                          class={`w-full px-2 py-1 text-xs rounded border outline-none focus:ring-1 focus:ring-blue-500 font-normal ${isDark() ? 'bg-slate-900 border-slate-600 text-slate-200 placeholder-slate-500' : 'bg-white border-slate-300 text-slate-700 placeholder-slate-400'}`}
                          onInput={(e) => setFilters(f => ({ ...f, [col.key as string]: e.currentTarget.value }))}
                        />
                      }
                    >
                      <select
                        class={`w-full px-2 py-1 text-xs rounded border outline-none focus:ring-1 focus:ring-blue-500 font-normal cursor-pointer ${isDark() ? 'bg-slate-900 border-slate-600 text-slate-200' : 'bg-white border-slate-300 text-slate-700'}`}
                        onChange={(e) => setFilters(f => ({ ...f, [col.key as string]: e.currentTarget.value }))}
                      >
                        <option value="">All</option>
                        <For each={col.filterOptions}>{opt => <option value={opt}>{opt}</option>}</For>
                      </select>
                    </Show>
                  </Show>
                </th>
              )}</For>
            </tr>
          </thead>
          <tbody class={`${isDark() ? 'text-slate-300 divide-slate-700' : 'text-slate-600 divide-slate-200'} divide-y`}>
            <Show when={paginatedData().length > 0} fallback={
              <tr><td colspan={props.columns.length} class="p-8 text-center text-slate-400 italic">{props.emptyMessage || 'No data found.'}</td></tr>
            }>
              <For each={paginatedData()}>{row => (
                <tr class={`transition-colors text-sm ${rowClass()}`}>
                  <For each={props.columns}>{col => (
                    <td class="px-4 py-2 border-r last:border-r-0 border-transparent relative group">
                      {col.render ? col.render(row) : String(row[col.key] ?? '')}
                    </td>
                  )}</For>
                </tr>
              )}</For>
            </Show>
          </tbody>
        </table>
      </div>
    </div>
  );
}
