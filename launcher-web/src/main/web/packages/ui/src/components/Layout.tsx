import { Component, JSX, createSignal, Show, For } from 'solid-js';
import { A, useLocation, useIsRouting } from '@solidjs/router';

export interface NavItem {
  label: string;
  href: string;
}

export interface LayoutProps {
  children?: JSX.Element;
  theme: 'Light' | 'Dark';
  toggleTheme: () => void;
  username: string;
  navItems: NavItem[];
  onLogout?: () => void;
}

export const Layout: Component<LayoutProps> = (props) => {
  const location = useLocation();
  const isRouting = useIsRouting();
  const [isMenuOpen, setIsMenuOpen] = createSignal(false);
  const isDark = () => props.theme === 'Dark';

  const activeClass = (path: string) => {
    const isActive = location.pathname === path;
    if (isActive) {
      return 'bg-gradient-to-br from-red-600 to-slate-900 text-white font-bold shadow-md';
    }
    return 'text-slate-600 hover:text-slate-900 hover:bg-slate-100 dark:text-slate-400 dark:hover:text-white dark:hover:bg-slate-800';
  };

  return (
    <div class="min-h-screen bg-slate-50 text-slate-800 dark:bg-slate-950 dark:text-slate-200">
      <Show when={isRouting()}>
        <div class="fixed inset-0 z-[100] flex items-center justify-center bg-white/50 dark:bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
          {/* Spinner */}
          <div class="relative w-16 h-16">
            <div class="absolute inset-0 border-4 border-slate-200 dark:border-slate-700 rounded-full"></div>
            <div class="absolute inset-0 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
          </div>
        </div>
      </Show>

      {/* Navigation Bar */}
      <nav class="sticky top-0 z-40 backdrop-blur-md border-b transition-colors duration-200 bg-white/80 border-slate-200 dark:bg-slate-900/80 dark:border-slate-800">
        <div class="w-full px-4 sm:px-6 lg:px-8">
          <div class="flex items-center justify-between h-16">

            {/* Left Side: Logo & Main Navigation */}
            <div class="flex items-center gap-6 md:gap-8">
              {/* Logo */}
              <div class="flex items-center gap-3 flex-shrink-0">
                <div class="relative w-10 h-10 md:w-11 md:h-11 flex-shrink-0 flex items-center justify-center">
                  <i class="fa-solid fa-folder text-4xl md:text-5xl bg-gradient-to-br from-red-600 to-slate-900 bg-clip-text text-transparent drop-shadow-sm"></i>
                  <div class="absolute inset-0 flex items-center justify-center pt-1.5 pl-0.5 pointer-events-none">
                    <i class="fa-solid fa-magnifying-glass text-white text-sm md:text-base drop-shadow-md"></i>
                  </div>
                </div>
                <A href="/" class="text-lg md:text-xl font-black tracking-tight truncate text-slate-900 dark:text-white">Folder Inspector</A>
              </div>

              {/* Desktop Navigation Links */}
              <div class="hidden lg:flex items-center gap-1">
                <For each={props.navItems}>
                  {(item) => (
                    <A href={item.href} class={`px-4 py-2 rounded-lg text-sm transition-all duration-200 ${activeClass(item.href)}`}>
                      {item.label}
                    </A>
                  )}
                </For>
              </div>
            </div>

            {/* Right Side: Actions & Mobile Menu Toggle */}
            <div class="flex items-center gap-2 md:gap-4">

              {/* Theme Toggle */}
              <button
                onClick={props.toggleTheme}
                class="p-3 rounded-lg transition-colors text-slate-600 hover:bg-slate-100 hover:text-slate-900 dark:text-yellow-400 dark:hover:bg-slate-800"
                title="Toggle Theme"
              >
                <Show when={isDark()} fallback={<i class="fa-solid fa-moon text-lg p-1"></i>}>
                  <i class="fa-solid fa-sun text-lg p-1"></i>
                </Show>
              </button>

              {/* User & Logout */}
              <div class="flex items-center gap-3 pl-3 md:pl-4 border-l border-slate-200 dark:border-slate-800">
                <span class="text-sm font-semibold text-slate-800 dark:text-white">{props.username}</span>
                <button
                  onClick={props.onLogout}
                  class="p-2 rounded-full transition-colors text-slate-500 hover:bg-red-50 hover:text-red-600 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-red-400"
                  title="Logout"
                >
                  <i class="fa-solid fa-right-from-bracket text-lg p-1"></i>
                </button>
              </div>

              {/* Mobile Menu Toggle */}
              <div class="lg:hidden flex items-center ml-2">
                <button
                  onClick={() => setIsMenuOpen(!isMenuOpen())}
                  class="p-3 rounded-lg transition-colors text-slate-700 hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-800"
                >
                  <i class={`fa-solid ${isMenuOpen() ? 'fa-xmark' : 'fa-bars'} text-xl p-1`}></i>
                </button>
              </div>

            </div>
          </div>
        </div>

        {/* Mobile Navigation Menu */}
        <Show when={isMenuOpen()}>
          <div class="lg:hidden border-t border-slate-100 bg-white dark:border-slate-800 dark:bg-slate-900">
            <div class="px-4 pt-2 pb-4 space-y-1">
              <For each={props.navItems}>
                {(item) => (
                  <A
                    href={item.href}
                    onClick={() => setIsMenuOpen(false)}
                    class={`block px-4 py-3 rounded-lg text-base ${activeClass(item.href)}`}
                  >
                    {item.label}
                  </A>
                )}
              </For>
            </div>
          </div>
        </Show>
      </nav>

      <div class="w-full px-4 sm:px-6 lg:px-8 py-6">
        {props.children}
      </div>
    </div>
  );
};
