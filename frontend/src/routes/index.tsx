import { A, useNavigate } from "@solidjs/router";
import { FaBrandsWindows, FaSolidShieldHalved, FaSolidNetworkWired, FaSolidFolderOpen } from "solid-icons/fa";
import { getToken } from "~/lib/api";
import { createEffect } from "solid-js";

export default function Index() {
  const navigate = useNavigate();

  createEffect(() => {
    if (getToken()) {
      navigate("/dashboard");
    }
  });

  return (
    <div class="min-h-screen bg-[#050505] text-zinc-300 font-sans flex flex-col font-inter selection:bg-amber-500/30">
      {/* Top Navbar */}
      <header class="h-16 bg-[#050505]/80 backdrop-blur-md border-b border-zinc-800/80 flex items-center justify-between px-6 lg:px-12 fixed top-0 left-0 right-0 z-50">
        <div class="flex items-center gap-6">
          <A href="/" class="flex items-center gap-2 group">
            <div class="bg-amber-500 rounded p-1.5">
              <FaSolidFolderOpen class="text-white text-lg" />
            </div>
            <span class="font-bold text-zinc-100 tracking-tight text-xl group-hover:text-amber-500 transition-colors">Inspector</span>
          </A>
        </div>
        <div class="flex items-center gap-4">
          <A href="/access/login" class="text-sm font-medium bg-amber-600 hover:bg-amber-500 text-white px-5 py-2.5 rounded-lg transition-all shadow-lg shadow-amber-900/20 active:scale-95">
            Login
          </A>
        </div>
      </header>

      {/* Hero Section */}
      <main class="flex-1 mt-16 flex flex-col items-center justify-center text-center px-4 py-16 relative overflow-hidden">
        {/* Background glow */}
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-amber-500/10 rounded-full blur-[120px] -z-10 pointer-events-none"></div>
        <div class="absolute top-0 right-10 w-[400px] h-[400px] bg-blue-500/10 rounded-full blur-[100px] -z-10 pointer-events-none"></div>

        <h1 class="text-5xl md:text-7xl font-bold text-white tracking-tight leading-tight max-w-4xl mb-6">
          Enterprise Visibility Into <br class="hidden md:block" /> <span class="text-transparent bg-clip-text bg-gradient-to-r from-amber-400 to-amber-600">Shared Permissions.</span>
        </h1>

        <p class="text-lg md:text-xl text-zinc-400 max-w-2xl leading-relaxed font-light">
          Folder Inspector acts as your zero-trust sentinel for active directory file shares. Automatically scan, identify, and report on sprawling SMBv3 ACLs.
        </p>
      </main>

      {/* Features Bar */}
      <section class="border-t border-zinc-800/80 bg-[#0a0a0b] py-8 px-6 lg:px-12">
        <div class="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-10">
          <div class="flex flex-col gap-4">
            <div class="w-12 h-12 bg-[#161616] border border-zinc-800 rounded flex items-center justify-center text-blue-500 text-xl shadow-inner">
              <FaSolidNetworkWired />
            </div>
            <h3 class="text-xl font-semibold text-zinc-100">Deep Network Scans</h3>
            <p class="text-zinc-500 leading-relaxed">Agentless sweeps through massive SMBv3 appliances utilizing asynchronous parallel I/O.</p>
          </div>
          <div class="flex flex-col gap-4">
            <div class="w-12 h-12 bg-[#161616] border border-zinc-800 rounded flex items-center justify-center text-emerald-500 text-xl shadow-inner">
              <FaBrandsWindows />
            </div>
            <h3 class="text-xl font-semibold text-zinc-100">AD Resolution</h3>
            <p class="text-zinc-500 leading-relaxed">Translates raw Security Identifiers (SIDs) directly into human-readable Active Directory names.</p>
          </div>
          <div class="flex flex-col gap-4">
            <div class="w-12 h-12 bg-[#161616] border border-zinc-800 rounded flex items-center justify-center text-amber-500 text-xl shadow-inner">
              <FaSolidShieldHalved />
            </div>
            <h3 class="text-xl font-semibold text-zinc-100">Access Auditing</h3>
            <p class="text-zinc-500 leading-relaxed">Instantly expose overpermissive folders, explicit ACL assignments, and broken inheritance chains.</p>
          </div>
        </div>
      </section>

    </div>
  );
}
