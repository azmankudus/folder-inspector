import { JSX, Show } from "solid-js";
import { A, useLocation } from "@solidjs/router";
import { FaSolidFolderOpen, FaSolidUser, FaSolidRightFromBracket, FaSolidNetworkWired, FaSolidSatelliteDish, FaSolidFolderTree, FaSolidClockRotateLeft, FaSolidFolder, FaSolidDatabase } from "solid-icons/fa";
import { removeToken } from "../lib/api";
import { store } from "../lib/store";

export default function AppLayout(props: Readonly<{ children: JSX.Element; title: string }>) {
  const location = useLocation();
  const user = () => store.state.user;
  
  const hasPerm = (perm: string) => user()?.roles?.includes(perm) ?? false;
  
  const handleLogout = () => {
    removeToken();
    if (globalThis.window !== undefined) {
      globalThis.window.location.href = "/login";
    }
  };

  return (
    <div class="min-h-screen bg-[#050505] text-zinc-300 font-sans flex flex-col font-inter">
      {/* Top Navbar */}
      <header class="h-16 bg-[#111111] border-b border-zinc-800/80 flex items-center justify-between px-6 lg:px-12 sticky top-0 z-50">
        <div class="flex items-center gap-8">
          <A href="/" class="flex items-center gap-2 group">
            <div class="bg-amber-500 rounded p-1.5 flex items-center justify-center">
              <FaSolidFolderOpen class="text-white text-md" />
            </div>
            <span class="font-bold text-zinc-100 tracking-tight text-xl group-hover:text-amber-500 transition-colors">Inspector</span>
          </A>

          <nav class="hidden md:flex items-center gap-1">
            <Show when={hasPerm('FILE_EXPLORER_VIEW_ALL') || hasPerm('FILE_EXPLORER_VIEW_RESTRICTED')}>
              <TopNavItem href="/files" icon={<FaSolidFolder />} label="Files" isActive={location.pathname === "/files"} />
            </Show>
            <Show when={hasPerm('CONN_READ')}>
              <TopNavItem href="/connections" icon={<FaSolidNetworkWired />} label="Connections" isActive={location.pathname === "/connections"} />
            </Show>
            <Show when={hasPerm('SCAN_READ')}>
              <TopNavItem href="/scan-profiles" icon={<FaSolidSatelliteDish />} label="Profiles" isActive={location.pathname === "/scan-profiles"} />
            </Show>
            <Show when={hasPerm('AD_READ')}>
              <TopNavItem href="/directory-profiles" icon={<FaSolidFolderTree />} label="AD Profiles" isActive={location.pathname === "/directory-profiles"} />
            </Show>
            <Show when={hasPerm('SCAN_HISTORY_READ')}>
              <TopNavItem href="/scan-history" icon={<FaSolidClockRotateLeft />} label="History" isActive={location.pathname === "/scan-history"} />
            </Show>
            <Show when={hasPerm('SQL_ADMIN')}>
              <TopNavItem href="/sql-browser" icon={<FaSolidDatabase />} label="SQL" isActive={location.pathname === "/sql-browser"} />
            </Show>
          </nav>
        </div>
        
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2 text-zinc-400 text-sm bg-zinc-900 border border-zinc-800 px-3 py-1.5 rounded-full">
            <FaSolidUser class="text-xs text-blue-500" />
            <span class="font-medium">{user()?.username || 'Guest'}</span>
          </div>
          
          <button 
            onClick={handleLogout}
            class="p-2 text-zinc-500 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-all"
            title="Logout"
          >
            <FaSolidRightFromBracket class="text-lg" />
          </button>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto bg-[#0a0a0b] relative">
        <div class="max-w-7xl mx-auto p-4 sm:p-6 lg:p-10">
          <div class="mb-8 border-b border-zinc-800 pb-6">
            <h1 class="text-3xl font-bold text-white tracking-tight leading-none">{props.title}</h1>
            <p class="text-zinc-500 mt-2 text-sm uppercase tracking-widest font-semibold font-mono">Control Center / {props.title}</p>
          </div>
          
          <div class="relative z-10 w-full">
            {props.children}
          </div>
        </div>
      </main>
    </div>
  );
}

function TopNavItem(props: Readonly<{ href: string; icon: JSX.Element; label: string; isActive: boolean }>) {
  return (
    <A 
      href={props.href} 
      class={`flex items-center gap-2 px-4 py-2 rounded-lg transition-all text-sm font-medium border border-transparent relative ${
        props.isActive 
          ? "bg-blue-600/10 text-blue-400 border-blue-500/20" 
          : "text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800/50"
      }`}
    >
      <span class={props.isActive ? "text-blue-500" : "text-zinc-500"}>{props.icon}</span>
      <span>{props.label}</span>
      {props.isActive && (
         <div class="absolute -bottom-[1px] left-4 right-4 h-0.5 bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.6)] rounded-t"></div>
      )}
    </A>
  );
}
