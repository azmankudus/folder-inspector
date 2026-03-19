import "./app.css";
import { MetaProvider, Title } from "@solidjs/meta";
import { Router } from "@solidjs/router";
import { FileRoutes } from "@solidjs/start/router";
import { ErrorBoundary, Suspense, onMount, Show } from "solid-js";
import { store } from "~/lib/store";
import { getUserInfo } from "~/lib/api";

export default function App() {
  onMount(() => {
    store.setUser(getUserInfo());
  });

  return (
    <Router
      root={props => (
        <MetaProvider>
          <Title>SMB Folder Inspector</Title>
          <ErrorBoundary fallback={(err) => (
            <div class="min-h-screen bg-slate-900 text-white flex flex-col items-center justify-center p-8 font-mono">
              <h1 class="text-3xl font-bold mb-4 text-red-500">System Failure</h1>
              <div class="bg-black p-6 rounded-lg border border-zinc-700 max-w-4xl w-full">
                <p class="text-zinc-400 mb-4 border-b border-zinc-800 pb-2">Technical details:</p>
                <pre class="overflow-auto whitespace-pre-wrap text-sm text-red-400">
                  {err instanceof Error ? err.message : String(err)}
                  {err instanceof Error && err.stack && (
                    <div class="mt-4 text-zinc-500 text-xs border-t border-zinc-800 pt-2">
                      {err.stack}
                    </div>
                  )}
                </pre>
              </div>
              <button 
                onClick={() => window.location.reload()} 
                class="mt-8 px-6 py-2 bg-blue-600 text-white rounded-full font-bold hover:bg-blue-500 transition-all shadow-lg hover:shadow-blue-500/20"
              >
                Reload Application
              </button>
            </div>
          )}>
            <Suspense fallback={<div class="flex h-screen items-center justify-center bg-gray-950 text-white font-medium tracking-widest">LOADING CORE ENGINE...</div>}>
              {props.children}
              
              <Show when={store.state.error}>
                 <div class="fixed top-4 right-4 bg-red-600/90 backdrop-blur text-white px-6 py-4 rounded-xl shadow-2xl z-[100] flex items-center gap-4 border border-red-500/50 animate-in fade-in slide-in-from-top-4 duration-300">
                    <span class="font-medium">{store.state.error}</span>
                    <button onClick={() => store.clearError()} class="hover:bg-red-500 p-1 rounded-lg transition-colors">✕</button>
                 </div>
              </Show>

            </Suspense>
          </ErrorBoundary>
        </MetaProvider>
      )}
    >
      <FileRoutes />
    </Router>
  );
}
