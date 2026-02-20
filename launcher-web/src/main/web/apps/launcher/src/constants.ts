
import { FileEntry, ACL, TargetConfig, ScanHistoryEntry } from './types';

export const MOCK_OWNERS = ['SYSTEM', 'Administrator', 'user_john', 'dev_team', 'guest'];
export const MOCK_GROUPS = ['Administrators', 'Users', 'Devs', 'Guests'];
export const ACL_NAMES = ['Full Control', 'Modify', 'Read & Execute', 'Read', 'Write'];
export const INHERITANCE_FLAGS = ['None', 'Container Inherit', 'Object Inherit', 'Inherit Only'];
export const ACCESS_MASKS = ['0x1F01FF', '0x120089', '0x12019F', '0x1301BF'];

export const generateMockACL = (): ACL[] => {
  const count = Math.floor(Math.random() * 3) + 1;
  return Array.from({ length: count }).map(() => ({
    name: ACL_NAMES[Math.floor(Math.random() * ACL_NAMES.length)],
    inheritanceFlags: INHERITANCE_FLAGS[Math.floor(Math.random() * INHERITANCE_FLAGS.length)],
    accessMasks: ACCESS_MASKS[Math.floor(Math.random() * ACCESS_MASKS.length)],
  }));
};

export const formatBytes = (bytes: number, decimals = 2) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
};

// Generate 100 MOCK CONFIGS
export const INITIAL_CONFIGS: TargetConfig[] = Array.from({ length: 100 }, (_, i) => ({
  id: `cfg-${i + 1}`,
  name: `Server Node ${i + 1}`,
  type: ['SMB', 'NFS', 'Local', 'S3'][Math.floor(Math.random() * 4)] as any,
  server: `192.168.1.${100 + i}`,
  rootPath: `/mnt/data/node_${i + 1}`,
  recursive: Math.random() > 0.5 ? 'Yes' : 'No',
  schedule: ['Daily 00:00', 'Weekly', 'Hourly', 'Manual'][Math.floor(Math.random() * 4)],
  created: new Date().toISOString(),
  updated: new Date().toISOString()
}));

export const INITIAL_HISTORY: ScanHistoryEntry[] = Array.from({ length: 100 }, (_, i) => {
  const config = INITIAL_CONFIGS[Math.floor(Math.random() * INITIAL_CONFIGS.length)];
  return {
    id: `h${i + 1}`,
    configId: config.id,
    configName: config.name,
    timestamp: new Date(Date.now() - Math.floor(Math.random() * 10000000000)).toISOString(),
    status: ['Completed', 'Failed', 'In Progress'][Math.floor(Math.random() * 3)] as any
  };
});

export const generateMockFiles = (count = 100): FileEntry[] => {
  const files: FileEntry[] = [];

  for (let i = 0; i < count; i++) {
    const isDir = Math.random() > 0.7;
    // Randomly assign a config ID
    const config = INITIAL_CONFIGS[Math.floor(Math.random() * INITIAL_CONFIGS.length)];

    files.push({
      id: crypto.randomUUID(),
      configId: config.id,
      name: i % 7 === 0 ? `Quarterly_Report_${i}.pdf` : i % 5 === 0 ? `Backup_${i}.zip` : `Document_${i}.docx`,
      parent: config.rootPath + (i % 3 === 0 ? '/reports' : '/data'),
      type: isDir ? 'Directory' : 'File',
      size: Math.floor(Math.random() * 10000000),
      created: new Date(Date.now() - Math.random() * 10000000000).toISOString(),
      modified: new Date(Date.now() - Math.random() * 1000000000).toISOString(),
      owner: MOCK_OWNERS[i % MOCK_OWNERS.length],
      group: MOCK_GROUPS[i % MOCK_GROUPS.length],
      acl: generateMockACL(),
    });
  }
  return files;
};
