import { createSignal, createEffect } from 'solid-js';
import { TargetConfig, ScanHistoryEntry, FileEntry, Theme } from './types';
import { INITIAL_CONFIGS, INITIAL_HISTORY, generateMockFiles } from './constants';

const getInitialTheme = (): Theme => {
  if (typeof window !== 'undefined') {
    if (document.documentElement.classList.contains('dark')) return 'Dark'; // Prioritize server-rendered state
    const stored = localStorage.getItem('theme');
    if (stored && stored.toLowerCase() === 'dark') return 'Dark';
    if (stored && stored.toLowerCase() === 'light') return 'Light';
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) return 'Dark';
  }
  return 'Light';
};

export const [theme, setTheme] = createSignal<Theme>(getInitialTheme());
export const [username] = createSignal('JohnDoe');
export const [configs, setConfigs] = createSignal<TargetConfig[]>(INITIAL_CONFIGS);
export const [history] = createSignal<ScanHistoryEntry[]>(INITIAL_HISTORY);
export const [mockFiles, setMockFiles] = createSignal<FileEntry[]>([]);

// Mock data generation moved to component onMount to avoid hydration mismatch

export const handleAddConfig = (config: TargetConfig) => {
  setConfigs(prev => [...prev, config]);
};

export const toggleTheme = () => setTheme(prev => prev === 'Light' ? 'Dark' : 'Light');
export const isDark = () => theme() === 'Dark';

createEffect(() => {
  if (typeof window !== 'undefined') {
    localStorage.setItem('theme', theme());
    if (theme() === 'Dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }
});
