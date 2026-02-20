import { Component } from 'solid-js';
import { A } from '@solidjs/router';
import { isDark } from '../store';

const Documentation: Component = () => {
  return (
    <div class="space-y-8 animate-in fade-in duration-500">
      {/* Header Section */}
      <div class="p-8 rounded-2xl border bg-white border-slate-200 shadow-sm dark:bg-slate-900 dark:border-slate-800">
        <div class="flex items-center gap-4 mb-4">
          <div class="p-3 rounded-xl bg-blue-50 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400">
            <i class="fa-solid fa-book text-2xl"></i>
          </div>
          <div>
            <h1 class="text-2xl font-bold text-slate-900 dark:text-white">Documentation</h1>
            <p class="text-slate-600 dark:text-slate-400">Learn how to use Folder Inspector effectively</p>
          </div>
        </div>
      </div>

      {/* Content Grid */}
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Getting Started */}
        <div class="p-6 rounded-2xl border transition-all duration-200 hover:shadow-md bg-white border-slate-200 hover:border-blue-200 dark:bg-slate-900 dark:border-slate-800 dark:hover:border-slate-700">
          <h2 class="text-xl font-bold mb-4 flex items-center gap-2 text-slate-900 dark:text-white">
            <i class="fa-solid fa-flag-checkered text-green-500 p-1"></i>
            Getting Started
          </h2>
          <p class="mb-4 text-slate-600 dark:text-slate-400">
            Folder Inspector helps you visualize and manage your file system. Start by navigating to the Dashboard to see an overview of your current directory.
          </p>
          <ul class="list-disc list-inside space-y-2 text-slate-600 dark:text-slate-400">
            <li>View file distribution</li>
            <li>Analyze folder sizes</li>
            <li>Monitor stored files</li>
          </ul>
        </div>

        {/* Search Features */}
        <div class="p-6 rounded-2xl border transition-all duration-200 hover:shadow-md bg-white border-slate-200 hover:border-blue-200 dark:bg-slate-900 dark:border-slate-800 dark:hover:border-slate-700">
          <h2 class="text-xl font-bold mb-4 flex items-center gap-2 text-slate-900 dark:text-white">
            <i class="fa-solid fa-magnifying-glass text-purple-500 p-1"></i>
            Advanced Search
          </h2>
          <p class="mb-4 text-slate-600 dark:text-slate-400">
            Use the powerful search engine to find files by name, extension, size, or date modified.
          </p>
          <div class="p-3 rounded-lg text-sm mb-2 bg-slate-50 text-slate-700 dark:bg-slate-950 dark:text-slate-300">
            <code class="font-mono">ext:pdf size:&gt;10MB</code>
          </div>
          <A href="/search" class="text-blue-500 hover:text-blue-400 font-medium text-sm">Try generic search &rarr;</A>
        </div>

        {/* Configuration */}
        <div class="p-6 rounded-2xl border transition-all duration-200 hover:shadow-md bg-white border-slate-200 hover:border-blue-200 dark:bg-slate-900 dark:border-slate-800 dark:hover:border-slate-700">
          <h2 class="text-xl font-bold mb-4 flex items-center gap-2 text-slate-900 dark:text-white">
            <i class="fa-solid fa-gear text-orange-500 p-1"></i>
            Configuration
          </h2>
          <p class="mb-4 text-slate-600 dark:text-slate-400">
            Customize your experience in the Configuration tab. You can set scanning preferences, exclude folders, and manage themes.
          </p>
        </div>

        {/* History Tracking */}
        <div class="p-6 rounded-2xl border transition-all duration-200 hover:shadow-md bg-white border-slate-200 hover:border-blue-200 dark:bg-slate-900 dark:border-slate-800 dark:hover:border-slate-700">
          <h2 class="text-xl font-bold mb-4 flex items-center gap-2 text-slate-900 dark:text-white">
            <i class="fa-solid fa-clock-rotate-left text-blue-500 p-1"></i>
            History Tracking
          </h2>
          <p class="mb-4 text-slate-600 dark:text-slate-400">
            Keep track of your file operations and scan history. The History tab provides a detailed timeline of all activities.
          </p>
        </div>
      </div>
    </div>
  );
};

export default Documentation;
