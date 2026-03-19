import { onMount } from "solid-js";
import { useNavigate } from "@solidjs/router";
import { removeToken } from "~/lib/api";
import { store } from "~/lib/store";

export default function Logout() {
  const navigate = useNavigate();

  onMount(() => {
    removeToken();
    store.setUser(null);
    // Force a small delay to ensure state propagates if needed, then redirect
    setTimeout(() => {
        navigate("/", { replace: true });
    }, 100);
  });

  return (
    <div class="min-h-screen bg-[#050505] flex items-center justify-center text-zinc-400 font-sans font-inter">
      <div class="flex flex-col items-center gap-4">
        <div class="w-12 h-12 border-4 border-amber-500/20 border-t-amber-500 rounded-full animate-spin"></div>
        <p class="text-sm font-medium tracking-wide">Ending session reliably...</p>
      </div>
    </div>
  );
}
