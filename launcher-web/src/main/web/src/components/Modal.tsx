import { ParentComponent, Show, JSX } from 'solid-js';
import { isDark } from '../store';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  maxWidth?: string;
  icon?: JSX.Element;
  children: JSX.Element;
}

const Modal: ParentComponent<ModalProps> = (props) => {
  return (
    <Show when={props.isOpen}>
      <div
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm animate-in fade-in duration-200 p-4"
        onClick={(e) => {
          if (e.target === e.currentTarget) props.onClose();
        }}
      >
        <div
          class={`w-full ${props.maxWidth || 'max-w-md'} rounded-xl shadow-2xl scale-100 animate-in zoom-in-95 duration-200 bg-white border-slate-200 dark:bg-slate-800 dark:border-slate-700 border flex flex-col max-h-[90vh]`}
          onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div class="flex justify-between items-center p-4 border-b shrink-0 border-slate-100 bg-slate-200 dark:border-slate-700 dark:bg-slate-950 rounded-t-xl">
            <div class="flex items-center min-w-0">
              <Show when={props.icon}>
                <div class="mr-3 shrink-0 h-10 w-10 flex items-center justify-center rounded-full bg-slate-100 dark:bg-slate-800 text-slate-500 dark:text-slate-400">{props.icon}</div>
              </Show>
              <h3 class="text-lg md:text-xl font-bold text-slate-800 dark:text-white truncate pr-4">{props.title}</h3>
            </div>
            <button
              onClick={props.onClose}
              class="p-2 rounded-full transition-colors text-slate-500 hover:bg-slate-200 hover:text-red-600 dark:text-slate-400 dark:hover:bg-slate-700 dark:hover:text-red-400"
              title="Close"
            >
              <i class="fa-solid fa-xmark text-lg"></i>
            </button>
          </div>

          {/* Content */}
          <div class="overflow-y-auto p-6">
            {props.children}
          </div>
        </div>
      </div>
    </Show>
  );
};

export default Modal;
