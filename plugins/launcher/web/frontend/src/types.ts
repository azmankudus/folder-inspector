
export interface ACL {
  name: string;
  inheritanceFlags: string;
  accessMasks: string;
}

export type FileType = 'File' | 'Directory';

export interface FileEntry {
  id: string;
  configId: string; // Added to link file to source config
  name: string;
  parent: string;
  type: FileType;
  size: number;
  created: string;
  modified: string;
  owner: string;
  group: string;
  acl: ACL[];
}

export interface TargetConfig {
  id: string;
  name: string;
  type: 'SMB' | 'NFS' | 'Local' | 'S3';
  server: string; // Hostname or IP
  rootPath: string;
  recursive: string;
  schedule: string;
  port?: number;
  username?: string;
  password?: string;
  created: string;
  updated: string;
}

export interface ScanHistoryEntry {
  id: string;
  configId: string;
  configName: string;
  timestamp: string;
  status: 'Completed' | 'Failed' | 'In Progress';
}

export type AppView = 'Dashboard' | 'Search' | 'Configuration' | 'History';

export type Theme = 'Light' | 'Dark';
