import { createSignal } from "solid-js";
import { FaSolidFolderOpen } from "solid-icons/fa";
import { setToken } from "../lib/api";
import { A, useNavigate } from "@solidjs/router";

export default function Login() {
  const navigate = useNavigate();
  const [username, setUsername] = createSignal("");
  const [password, setPassword] = createSignal("");
  const [error, setError] = createSignal("");
  const [loading, setLoading] = createSignal(false);

  const handleLogin = async (e: Event) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const res = await fetch("http://localhost:8080/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: username(), password: password() }),
      });

      if (!res.ok) throw new Error("Invalid username or password");

      const data = await res.json();
      setToken(data.access_token);
      navigate("/files");
    } catch (err: any) {
      setError(err.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div class="min-h-screen bg-[#050505] flex flex-col font-sans font-inter text-zinc-300">
      <header class="h-16 flex items-center px-6 lg:px-12 border-b border-zinc-800/80 bg-[#050505]">
        <A href="/" class="flex items-center gap-2 group">
          <div class="bg-amber-500 rounded p-1.5 flex items-center justify-center">
             <FaSolidFolderOpen class="text-white text-lg" />
          </div>
          <span class="font-bold text-zinc-100 tracking-tight text-xl">Inspector</span>
        </A>
      </header>

      <div class="flex-1 flex items-center justify-center p-4 relative overflow-hidden">
        {/* subtle background hue */}
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[600px] bg-blue-500/5 rounded-full blur-[120px] -z-10 pointer-events-none"></div>

        <div class="bg-[#111111] border border-zinc-800 rounded-lg w-full max-w-md p-8 shadow-2xl relative z-10">
          <div class="text-center mb-8">
            <h1 class="text-2xl font-bold text-zinc-100 tracking-tight mb-2">Log in to Inspector</h1>
            <p class="text-zinc-500 text-sm">Enter your credentials to access the console.</p>
          </div>

          {error() && (
            <div class="bg-red-500/10 border border-red-500/50 text-red-500 text-sm p-3 rounded mb-6 flex items-center gap-2">
              <span class="font-semibold">Error:</span> {error()}
            </div>
          )}

          <form onSubmit={handleLogin} class="space-y-5">
            <div>
              <label class="block text-sm font-medium text-zinc-400 mb-1.5">
                <span class="block mb-1.5">Username</span>
                <input
                  id="username"
                  type="text"
                  required
                  value={username()}
                  onInput={e => setUsername(e.currentTarget.value)}
                  class="w-full bg-[#161616] border border-zinc-700 rounded px-3 py-2 text-zinc-200 focus:outline-none focus:border-blue-500/50 transition-all font-mono text-sm"
                  placeholder="e.g. admin"
                />
              </label>
            </div>
            <div>
              <label class="block text-sm font-medium text-zinc-400 mb-1.5">
                <span class="block mb-1.5">Password</span>
                <input
                  id="password"
                  type="password"
                  required
                  value={password()}
                  onInput={e => setPassword(e.currentTarget.value)}
                  class="w-full bg-[#161616] border border-zinc-700 rounded px-3 py-2 text-zinc-200 focus:outline-none focus:border-blue-500/50 transition-all font-mono text-sm"
                  placeholder="••••••••"
                />
              </label>
            </div>
            
            <button
              type="submit"
              disabled={loading()}
              class="w-full bg-blue-600 hover:bg-blue-500 text-white font-medium py-2 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed mt-2"
            >
              {loading() ? "Authenticating..." : "Log in"}
            </button>
          </form>

          <div class="relative my-6">
            <div class="absolute inset-0 flex items-center" aria-hidden="true">
              <div class="w-full border-t border-zinc-800"></div>
            </div>
            <div class="relative flex justify-center text-xs uppercase">
              <span class="bg-[#111111] px-2 text-zinc-500">Or continue with</span>
            </div>
          </div>

          <a
            href="http://localhost:8080/api/oauth/login/azure"
            class="w-full flex items-center justify-center gap-2 bg-zinc-800 hover:bg-zinc-700 text-zinc-200 font-medium py-2 rounded transition-colors text-sm"
          >
            <svg class="w-4 h-4" viewBox="0 0 23 23" fill="currentColor">
              <path d="M0 0h11.4v11.4H0V0zm11.6 0H23v11.4H11.6V0zM0 11.6h11.4V23H0V11.6zm11.6 0H23V23H11.6V11.6z"/>
            </svg>
            Microsoft Entra ID
          </a>
          
          <div class="mt-4">
            <a 
              href="http://localhost:8080/api/saml/login"
              class="flex items-center justify-center gap-3 w-full py-2 px-4 bg-zinc-800 hover:bg-zinc-700 text-zinc-200 rounded transition-colors text-sm"
            >
              <div class="p-1.5 bg-white rounded-lg">
                <svg class="w-4 h-4 text-[#00a4ef]" viewBox="0 0 23 23" fill="currentColor">
                  <path d="M11.4 24l-11.4-11.4 11.4-11.4 11.4 11.4z" />
                </svg>
              </div>
              <span class="font-medium">Microsoft Entra (SAML2)</span>
            </a>
            <p class="text-xs text-center text-zinc-500 mt-2">
              (Recommended for internet-restricted environments)
            </p>
          </div>

          <div class="mt-8 text-center border-t border-zinc-800/80 pt-6">
            <p class="text-xs text-zinc-500">
              By logging in, you agree to the <A href="#" class="text-blue-500 hover:underline">Terms of Service</A> and <A href="#" class="text-blue-500 hover:underline">Privacy Policy</A>.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
