import { Component, JSX, createSignal, Show, onMount } from 'solid-js';
import { A, useLocation, useIsRouting } from '@solidjs/router';
import { isDark, toggleTheme, theme, username, setMockFiles, mockFiles } from '../store';
import { generateMockFiles } from '../constants';

interface LayoutProps {
  children?: JSX.Element;
}

const Layout: Component<LayoutProps> = (props) => {
  const location = useLocation();
  const isRouting = useIsRouting();
  const [isMenuOpen, setIsMenuOpen] = createSignal(false);

  // Improved highlighting logic with distinct colors for active state
  const activeClass = (path: string) => {
    const isActive = location.pathname === path; // Exact match for these top-level routes
    if (isActive) {
      return isDark()
        ? 'bg-slate-800 text-red-400 font-bold'
        : 'bg-white text-red-600 shadow-sm font-bold border border-slate-100';
    }
    return isDark()
      ? 'text-slate-400 hover:text-white hover:bg-slate-800'
      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100';
  };

  onMount(() => {
    if (mockFiles().length === 0) {
      setMockFiles(generateMockFiles(250));
    }
  });

  return (
    <div class={`min-h-screen transition-colors duration-200 ${isDark() ? 'bg-slate-950 text-slate-200' : 'bg-slate-50 text-slate-800'}`}>
      <Show when={isRouting()}>
        <div class="fixed inset-0 z-[100] flex items-center justify-center bg-white/50 dark:bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
          <div class="relative w-16 h-16">
            <div class="absolute inset-0 border-4 border-slate-200 dark:border-slate-700 rounded-full"></div>
            <div class="absolute inset-0 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
          </div>
        </div>
      </Show>

      {/* Navigation Bar */}
      <nav class={`sticky top-0 z-40 backdrop-blur-md border-b transition-colors duration-200 ${isDark() ? 'bg-slate-900/80 border-slate-800' : 'bg-white/80 border-slate-200'}`}>
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex items-center justify-between h-16">

            {/* Left Side: Logo & Main Navigation */}
            <div class="flex items-center gap-6 md:gap-8">
              {/* Logo */}
              <div class="flex items-center gap-3 flex-shrink-0">
                <div class="relative w-10 h-10 md:w-11 md:h-11 flex-shrink-0 flex items-center justify-center">
                  {/* FA Folder Icon with Gradient */}
                  <i class="fa-solid fa-folder text-4xl md:text-5xl bg-gradient-to-br from-red-600 to-slate-900 bg-clip-text text-transparent drop-shadow-sm"></i>
                  {/* Magnifying Glass Overlay */}
                  <div class="absolute inset-0 flex items-center justify-center pt-1.5 pl-0.5 pointer-events-none">
                    <i class="fa-solid fa-magnifying-glass text-white text-sm md:text-base drop-shadow-md"></i>
                  </div>
                </div>
                <A href="/" class={`text-lg md:text-xl font-black tracking-tight truncate ${isDark() ? 'text-white' : 'text-slate-900'}`}>Folder Inspector</A>
              </div>

              {/* Desktop Navigation Links */}
              <div class="hidden lg:flex items-center gap-1">
                <A href="/" class={`px-3 py-2 rounded-lg text-sm transition-all duration-200 ${activeClass('/')}`}>Dashboard</A>
                <A href="/search" class={`px-3 py-2 rounded-lg text-sm transition-all duration-200 ${activeClass('/search')}`}>Search</A>
                <A href="/history" class={`px-3 py-2 rounded-lg text-sm transition-all duration-200 ${activeClass('/history')}`}>History</A>
                <A href="/configuration" class={`px-3 py-2 rounded-lg text-sm transition-all duration-200 ${activeClass('/configuration')}`}>Configuration</A>
                <a href="#" class={`px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${isDark() ? 'text-slate-400 hover:text-white hover:bg-slate-800' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'}`}>Documentation</a>
                <a href="#" class={`px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${isDark() ? 'text-slate-400 hover:text-white hover:bg-slate-800' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'}`}>Help</a>
              </div>
            </div>

            {/* Right Side: Actions & Mobile Menu Toggle */}
            <div class="flex items-center gap-2 md:gap-4">

              {/* Theme Toggle */}
              <button
                onClick={toggleTheme}
                class={`p-2 rounded-lg transition-colors ${isDark() ? 'text-yellow-400 hover:bg-slate-800' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'}`}
                title="Toggle Theme"
              >
                <Show when={isDark()} fallback={<i class="fa-solid fa-moon text-lg"></i>}>
                  <i class="fa-solid fa-sun text-lg"></i>
                </Show>
              </button>

              {/* User & Logout */}
              <div class={`flex items-center gap-3 pl-3 md:pl-4 border-l ${isDark() ? 'border-slate-800' : 'border-slate-200'}`}>
                <span class={`text-sm font-semibold ${isDark() ? 'text-white' : 'text-slate-800'}`}>{username()}</span>
                <button class={`p-1.5 rounded-full transition-colors ${isDark() ? 'text-slate-400 hover:bg-slate-800 hover:text-red-400' : 'text-slate-500 hover:bg-red-50 hover:text-red-600'}`} title="Logout">
                  <i class="fa-solid fa-right-from-bracket text-lg"></i>
                </button>
              </div>

              {/* Mobile Menu Toggle */}
              <div class="lg:hidden flex items-center ml-2">
                <button
                  onClick={() => setIsMenuOpen(!isMenuOpen())}
                  class={`p-2 rounded-lg transition-colors ${isDark() ? 'text-slate-200 hover:bg-slate-800' : 'text-slate-700 hover:bg-slate-100'}`}
                >
                  <i class={`fa-solid ${isMenuOpen() ? 'fa-xmark' : 'fa-bars'} text-xl`}></i>
                </button>
              </div>

            </div>
          </div>
        </div>

        {/* Mobile Navigation Menu */}
        <Show when={isMenuOpen()}>
          <div class={`lg:hidden border-t ${isDark() ? 'border-slate-800 bg-slate-900' : 'border-slate-100 bg-white'}`}>
            <div class="px-4 pt-2 pb-4 space-y-1">
              <A href="/" onClick={() => setIsMenuOpen(false)} class={`block px-3 py-2 rounded-lg text-base ${activeClass('/')}`}>Dashboard</A>
              <A href="/search" onClick={() => setIsMenuOpen(false)} class={`block px-3 py-2 rounded-lg text-base ${activeClass('/search')}`}>Search</A>
              <A href="/history" onClick={() => setIsMenuOpen(false)} class={`block px-3 py-2 rounded-lg text-base ${activeClass('/history')}`}>History</A>
              <A href="/configuration" onClick={() => setIsMenuOpen(false)} class={`block px-3 py-2 rounded-lg text-base ${activeClass('/configuration')}`}>Configuration</A>
              <a href="#" onClick={() => setIsMenuOpen(false)} class={`block px-3 py-2 rounded-lg text-base font-medium ${isDark() ? 'text-slate-400 hover:text-white hover:bg-slate-800' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'}`}>Documentation</a>
              <a href="#" onClick={() => setIsMenuOpen(false)} class={`block px-3 py-2 rounded-lg text-base font-medium ${isDark() ? 'text-slate-400 hover:text-white hover:bg-slate-800' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'}`}>Help</a>
            </div>
          </div>
        </Show>
      </nav>

      <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {props.children}
      </main>
    </div>
  );
};

export default Layout;
