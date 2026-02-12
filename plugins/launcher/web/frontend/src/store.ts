import { createSignal, createEffect } from 'solid-js';
import { TargetConfig, ScanHistoryEntry, FileEntry, Theme } from './types';
import { INITIAL_CONFIGS, INITIAL_HISTORY, generateMockFiles } from './constants';

export const [theme, setTheme] = createSignal<Theme>('Light');
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
