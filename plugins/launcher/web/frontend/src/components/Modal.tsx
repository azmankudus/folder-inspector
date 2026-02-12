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
  const bgClass = () => isDark() ? 'bg-slate-800' : 'bg-white';
  const borderClass = () => isDark() ? 'border-slate-700' : 'border-slate-200';
  const textClass = () => isDark() ? 'text-white' : 'text-slate-800';

  return (
    <Show when={props.isOpen}>
      <div
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm animate-in fade-in duration-200 p-4"
        onClick={(e) => {
          if (e.target === e.currentTarget) props.onClose();
        }}
      >
        <div
          class={`w-full ${props.maxWidth || 'max-w-md'} rounded-xl shadow-2xl scale-100 animate-in zoom-in-95 duration-200 ${bgClass()} ${borderClass()} border flex flex-col max-h-[90vh]`}
          onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div class={`flex justify-between items-center p-4 border-b shrink-0 ${isDark() ? 'border-slate-700 bg-slate-900' : 'border-slate-100 bg-slate-100'} rounded-t-xl`}>
            <div class="flex items-center min-w-0">
              <Show when={props.icon}>
                <div class="mr-3 shrink-0">{props.icon}</div>
              </Show>
              <h3 class={`text-lg md:text-xl font-bold ${textClass()} truncate pr-4`}>{props.title}</h3>
            </div>
            <button
              onClick={props.onClose}
              class={`p-2 rounded-full transition-colors ${isDark() ? 'hover:bg-slate-700 text-slate-400 hover:text-red-400' : 'hover:bg-slate-200 text-slate-500 hover:text-red-600'}`}
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
