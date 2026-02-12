import { Title } from "@solidjs/meta";
import { createSignal, createEffect, onMount, For, Show } from "solid-js";
import { useNavigate } from "@solidjs/router";
import { isServer } from "solid-js/web";

interface Item {
  absolutePath: string;
  relativePath: string;
  type: string;
  size: number;
  lastModified: number;
  owner: string;
  permissions: string;
}

interface Snapshot {
  name: string;
  itemCount: number;
  fileSize: number;
  created: number;
}

const formatSize = (bytes: number): string => {
  if (bytes === 0) return "—";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i];
};

const formatDate = (timestamp: number): string => {
  if (!timestamp) return "—";
  return new Date(timestamp * 1000).toLocaleDateString();
};

export default function Browser() {
  const navigate = useNavigate();
  const [user, setUser] = createSignal<string | null>(null);
  const [activeTab, setActiveTab] = createSignal<"scan" | "snapshots">("scan");
  const [query, setQuery] = createSignal("");
  const [snapshotQuery, setSnapshotQuery] = createSignal("");
  const [selectedSnapshot, setSelectedSnapshot] = createSignal<string | null>(null);
  const [scanResults, setScanResults] = createSignal<Item[]>([]);
  const [snapshots, setSnapshots] = createSignal<Snapshot[]>([]);
  const [snapshotItems, setSnapshotItems] = createSignal<Item[]>([]);
  const [loading, setLoading] = createSignal(false);
  const [snapshotsLoading, setSnapshotsLoading] = createSignal(false);

  onMount(async () => {
    if (isServer) return;

    try {
      const res = await fetch("/api/v1/auth/me");
      const data = await res.json();
      if (!data.authenticated) {
        navigate("/");
        return;
      }
      setUser(data.username);
      await fetchScan();
      await fetchSnapshots();
    } catch (err) {
      navigate("/");
    }
  });

  const fetchScan = async () => {
    setLoading(true);
    try {
      const res = await fetch("/api/v1/scan");
      if (res.ok) {
        setScanResults(await res.json());
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchSnapshots = async () => {
    setSnapshotsLoading(true);
    try {
      const res = await fetch("/api/v1/snapshots");
      if (res.ok) {
        setSnapshots(await res.json());
      }
    } finally {
      setSnapshotsLoading(false);
    }
  };

  const fetchSnapshotItems = async (name: string) => {
    setLoading(true);
    try {
      const q = snapshotQuery() ? `&query=${encodeURIComponent(snapshotQuery())}` : "";
      const res = await fetch(`/api/v1/snapshots/${name}?limit=100${q}`);
      if (res.ok) {
        setSnapshotItems(await res.json());
      }
    } finally {
      setLoading(false);
    }
  };

  createEffect(() => {
    const name = selectedSnapshot();
    if (name && !isServer) {
      fetchSnapshotItems(name);
    }
  });

  const handleLogout = async () => {
    await fetch("/api/v1/auth/logout", { method: "POST" });
    navigate("/");
  };

  const filteredItems = () => {
    const items = scanResults() || [];
    const q = query().toLowerCase();
    if (!q) return items.slice(0, 100);
    return items.filter(i => i.relativePath.toLowerCase().includes(q)).slice(0, 100);
  };

  return (
    <div style="min-height: 100vh;">
      <Title>Browser - Folder Inspector</Title>

      {/* Animated accent strip */}
      <div class="accent-strip"></div>

      {/* Navigation */}
      <nav class="nav">
        <span class="nav-brand">Folder Inspector</span>
        <div class="nav-links">
          <button
            class={`nav-link ${activeTab() === "scan" ? "active" : ""}`}
            onClick={() => setActiveTab("scan")}
          >
            Live Scan
          </button>
          <button
            class={`nav-link ${activeTab() === "snapshots" ? "active" : ""}`}
            onClick={() => setActiveTab("snapshots")}
          >
            Snapshots
          </button>
        </div>
        <div class="flex items-center gap-4">
          <span style="color: var(--text-muted); font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em;">
            {user()}
          </span>
          <button class="btn btn-secondary" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </nav>

      {/* Content */}
      <div class="container" style="padding: 40px;">
        <Show when={activeTab() === "scan"}>
          <div class="card" style="padding: 32px;">
            <div class="flex items-center" style="justify-content: space-between; margin-bottom: 32px;">
              <h2 class="heading" style="font-size: 24px; color: var(--text-primary);">File Browser</h2>
              <div class="flex gap-4">
                <input
                  type="text"
                  class="input"
                  placeholder="🔍 Search files..."
                  style="width: 350px;"
                  value={query()}
                  onInput={(e) => setQuery(e.currentTarget.value)}
                />
                <button class="btn btn-primary" onClick={fetchScan}>
                  Refresh
                </button>
              </div>
            </div>

            <Show when={loading()}>
              <div class="flex items-center justify-center" style="padding: 80px;">
                <div class="spinner"></div>
              </div>
            </Show>

            <Show when={!loading()}>
              <div style="overflow-x: auto; border-radius: 12px; border: 1px solid var(--border);">
                <table class="table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Type</th>
                      <th>Size</th>
                      <th>Modified</th>
                      <th>Owner</th>
                      <th>Permissions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <For each={filteredItems()}>
                      {(item) => (
                        <tr>
                          <td class="mono" style="font-size: 13px;">
                            <span style={`font-size: 18px; margin-right: 10px; ${item.type === "Directory" ? "color: #fbbf24" : "color: var(--text-muted)"}`}>
                              {item.type === "Directory" ? "📁" : "📄"}
                            </span>
                            {item.relativePath}
                          </td>
                          <td>
                            <span class={`badge ${item.type === "Directory" ? "badge-folder" : "badge-file"}`}>
                              {item.type}
                            </span>
                          </td>
                          <td style="font-weight: 600;">{formatSize(item.size)}</td>
                          <td>{formatDate(item.lastModified)}</td>
                          <td style="color: var(--text-secondary);">{item.owner}</td>
                          <td class="mono" style="font-size: 12px; color: var(--text-muted);">{item.permissions}</td>
                        </tr>
                      )}
                    </For>
                  </tbody>
                </table>
              </div>
            </Show>
          </div>
        </Show>

        <Show when={activeTab() === "snapshots"}>
          <div class="flex gap-4">
            {/* Snapshot list */}
            <div class="card" style="width: 360px; padding: 32px;">
              <h2 class="heading" style="font-size: 20px; margin-bottom: 24px;">Snapshots</h2>
              <button class="btn btn-primary" style="width: 100%; margin-bottom: 24px;" onClick={fetchSnapshots}>
                Refresh
              </button>

              <Show when={snapshotsLoading()}>
                <div class="flex items-center justify-center" style="padding: 40px;">
                  <div class="spinner"></div>
                </div>
              </Show>

              <Show when={!snapshotsLoading()}>
                <For each={snapshots()}>
                  {(snap) => (
                    <div
                      onClick={() => setSelectedSnapshot(snap.name)}
                      class={`snapshot-card ${selectedSnapshot() === snap.name ? "active" : ""}`}
                    >
                      <div class="snapshot-name">{snap.name}</div>
                      <div class="snapshot-meta">
                        {snap.itemCount.toLocaleString()} items • {formatSize(snap.fileSize)}
                      </div>
                    </div>
                  )}
                </For>
              </Show>
            </div>

            {/* Snapshot content */}
            <div class="card" style="flex: 1; padding: 32px;">
              <Show when={selectedSnapshot()}>
                <div class="flex items-center gap-4" style="margin-bottom: 32px;">
                  <h2 class="heading" style="font-size: 20px;">
                    {selectedSnapshot()}
                  </h2>
                  <input
                    type="text"
                    class="input"
                    placeholder="SQL WHERE clause (e.g., type='File' AND size > 1000)"
                    style="flex: 1;"
                    value={snapshotQuery()}
                    onInput={(e) => setSnapshotQuery(e.currentTarget.value)}
                  />
                  <button class="btn btn-primary" onClick={() => fetchSnapshotItems(selectedSnapshot()!)}>
                    Query
                  </button>
                </div>

                <Show when={loading()}>
                  <div class="flex items-center justify-center" style="padding: 80px;">
                    <div class="spinner"></div>
                  </div>
                </Show>

                <Show when={!loading()}>
                  <div style="overflow-x: auto; border-radius: 12px; border: 1px solid var(--border);">
                    <table class="table">
                      <thead>
                        <tr>
                          <th>Name</th>
                          <th>Type</th>
                          <th>Size</th>
                          <th>Owner</th>
                        </tr>
                      </thead>
                      <tbody>
                        <For each={snapshotItems()}>
                          {(item) => (
                            <tr>
                              <td class="mono" style="font-size: 13px;">
                                <span style={`font-size: 18px; margin-right: 10px; ${item.type === "Directory" ? "color: #fbbf24" : "color: var(--text-muted)"}`}>
                                  {item.type === "Directory" ? "📁" : "📄"}
                                </span>
                                {item.relativePath}
                              </td>
                              <td>
                                <span class={`badge ${item.type === "Directory" ? "badge-folder" : "badge-file"}`}>
                                  {item.type}
                                </span>
                              </td>
                              <td style="font-weight: 600;">{formatSize(item.size)}</td>
                              <td style="color: var(--text-secondary);">{item.owner}</td>
                            </tr>
                          )}
                        </For>
                      </tbody>
                    </table>
                  </div>
                </Show>
              </Show>

              <Show when={!selectedSnapshot()}>
                <div class="flex items-center justify-center" style="padding: 120px; color: var(--text-muted); font-size: 16px; font-weight: 500;">
                  Select a snapshot to view its contents
                </div>
              </Show>
            </div>
          </div>
        </Show>
      </div>
    </div>
  );
}
