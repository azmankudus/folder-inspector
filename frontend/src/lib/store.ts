import { createStore } from "solid-js/store";
import { getUserInfo } from "./api";

interface AppState {
  user: { username: string; roles: string[] } | null;
  error: string | null;
  scanProgress: Record<number, number>; // historyId -> percentage
}

const [state, setState] = createStore<AppState>({
  user: getUserInfo(),
  error: null,
  scanProgress: {},
});

export const store = {
  get state() { return state; },
  
  setUser: (user: AppState["user"]) => setState("user", user),
  setError: (error: string | null) => setState("error", error),
  
  updateScanProgress: (historyId: number, progress: number) => 
    setState("scanProgress", (prev) => ({ ...prev, [historyId]: progress })),
    
  clearError: () => setState("error", null),
};
