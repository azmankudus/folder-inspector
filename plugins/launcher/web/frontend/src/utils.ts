/**
 * Match a string against a wildcard pattern.
 * - `*` matches zero or more characters
 * - `?` matches exactly one character
 * - If no wildcards are present, defaults to *pattern* (contains match)
 * - Case-insensitive
 */
export const wildcardMatch = (value: string, pattern: string): boolean => {
  if (!pattern) return true;
  const hasWildcards = pattern.includes('*') || pattern.includes('?');
  const effectivePattern = hasWildcards ? pattern : `*${pattern}*`;
  const regexStr = effectivePattern
    .replace(/[.+^${}()|[\]\\]/g, '\\$&')
    .replace(/\*/g, '.*')
    .replace(/\?/g, '.');
  return new RegExp(`^${regexStr}$`, 'i').test(value);
};

export const formatBytes = (bytes: number, decimals = 2) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
};
