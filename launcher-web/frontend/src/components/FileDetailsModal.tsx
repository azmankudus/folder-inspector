import { Component, For, Show } from 'solid-js';
import { FileEntry } from '../types';
import { isDark } from '../store';
import Modal from './Modal';

interface FileDetailsModalProps {
  file: FileEntry;
  onClose: () => void;
}

const FileDetailsModal: Component<FileDetailsModalProps> = (props) => {
  const textClass = 'text-slate-800 dark:text-slate-200';
  const labelClass = 'text-slate-500 dark:text-slate-400';
  const borderClass = 'border-slate-200 dark:border-slate-700';

  const icon = () => (
    <i class={`fa-solid ${props.file.type === 'Directory' ? 'fa-folder text-amber-500' : 'fa-file text-slate-400'}`}></i>
  );

  return (
    <Modal
      isOpen={true}
      onClose={props.onClose}
      title={props.file.name}
      maxWidth="max-w-4xl"
      icon={icon()}
    >
      <div class="mb-6">
        <p class="text-sm text-slate-500 font-mono break-all"><span class="font-bold mr-2">Parent:</span>{props.file.parent}</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div class="space-y-4">
          <h4 class={`font-semibold text-lg ${textClass} border-b ${borderClass} pb-2`}>Properties</h4>
          <div class="grid grid-cols-2 gap-y-4 text-sm">
            <div><span class={labelClass}>Type</span><div class={textClass}>{props.file.type}</div></div>
            <div><span class={labelClass}>Size</span><div class={`font-mono ${textClass}`}>{props.file.size.toLocaleString()} bytes</div></div>
            <div><span class={labelClass}>Created</span><div class={textClass}>{new Date(props.file.created).toLocaleString(undefined, { timeZoneName: 'short' })}</div></div>
            <div><span class={labelClass}>Modified</span><div class={textClass}>{new Date(props.file.modified).toLocaleString(undefined, { timeZoneName: 'short' })}</div></div>
            <div><span class={labelClass}>Owner</span><div class={textClass}>{props.file.owner}</div></div>
            <div><span class={labelClass}>Group</span><div class={textClass}>{props.file.group}</div></div>
          </div>
        </div>

        <div class="space-y-4">
          <h4 class={`font-semibold text-lg ${textClass} border-b ${borderClass} pb-2`}>Access Control List (ACL)</h4>
          <div class={`rounded-lg border overflow-hidden ${borderClass}`}>
            <table class={`w-full text-sm text-left ${textClass}`}>
              <thead class="text-xs uppercase font-bold bg-slate-100 text-slate-500 dark:bg-slate-900/50 dark:text-slate-400">
                <tr>
                  <th class="px-3 py-2">Name</th>
                  <th class="px-3 py-2">Inheritance</th>
                  <th class="px-3 py-2">Access Mask</th>
                </tr>
              </thead>
              <tbody class={`divide-y ${borderClass}`}>
                <Show when={props.file.acl && props.file.acl.length > 0} fallback={<tr><td colspan="3" class="px-3 py-4 text-center text-slate-500 italic">No ACL entries found.</td></tr>}>
                  <For each={props.file.acl}>{acl => (
                    <tr class="hover:bg-slate-50 dark:hover:bg-slate-700/50">
                      <td class="px-3 py-2 font-medium">{acl.name}</td>
                      <td class="px-3 py-2 text-xs opacity-80">{acl.inheritanceFlags}</td>
                      <td class="px-3 py-2 font-mono text-xs opacity-80">{acl.accessMasks}</td>
                    </tr>
                  )}</For>
                </Show>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Close Button at Bottom Left */}
      <div class={`flex justify-end mt-6 pt-4 border-t ${borderClass}`}>
        <button onClick={props.onClose} class="px-4 py-2 text-sm font-medium text-white bg-gradient-to-br from-red-600 to-slate-900 rounded-lg shadow-sm hover:shadow-md transition-colors">Close</button>
      </div>
    </Modal>
  );
};

export default FileDetailsModal;
