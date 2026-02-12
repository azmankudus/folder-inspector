import { Title } from "@solidjs/meta";
import { createSignal, createEffect, onMount, For, Show, createMemo } from "solid-js";
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

interface UserInfo {
  username: string;
  role: "viewer" | "auditor" | "admin";
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
  return new Date(timestamp * 1000).toLocaleString();
};

// Simple glob to regex converter
const globToRegex = (glob: string): RegExp => {
  const escaped = glob
    .replace(/[.+^${}()|[\]\\]/g, "\\$&")
    .replace(/\*/g, ".*")
    .replace(/\?/g, ".");
  return new RegExp(`^${escaped}$`, "i");
};

export default function Viewer() {
  const navigate = useNavigate();
  const [user, setUser] = createSignal<UserInfo | null>(null);
  const [snapshots, setSnapshots] = createSignal<Snapshot[]>([]);
  const [selectedSnapshot, setSelectedSnapshot] = createSignal<string>("");
  const [items, setItems] = createSignal<Item[]>([]);
  const [loading, setLoading] = createSignal(false);

  // Pagination
  const [page, setPage] = createSignal(1);
  const pageSize = 50;

  // Column filters (glob patterns)
  const [filters, setFilters] = createSignal({
    name: "",
    type: "",
    owner: "",
  });

  onMount(async () => {
    if (isServer) return;

    try {
      const res = await fetch("/api/v1/auth/me");
      const data = await res.json();
      if (!data.authenticated) {
        navigate("/");
        return;
      }
      setUser({ username: data.username, role: data.role || "viewer" });
      await fetchSnapshots();
    } catch (err) {
      navigate("/");
    }
  });

  const fetchSnapshots = async () => {
    try {
      const res = await fetch("/api/v1/snapshots");
      if (res.ok) {
        const data = await res.json();
        setSnapshots(data);
        // Default to latest (first) snapshot
        if (data.length > 0 && !selectedSnapshot()) {
          setSelectedSnapshot(data[0].name);
        }
      }
    } catch (e) {
      console.error("Failed to fetch snapshots", e);
    }
  };

  const fetchItems = async (name: string) => {
    setLoading(true);
    try {
      const res = await fetch(`/api/v1/snapshots/${name}?limit=1000`);
      if (res.ok) {
        setItems(await res.json());
        setPage(1);
      }
    } finally {
      setLoading(false);
    }
  };

  createEffect(() => {
    const name = selectedSnapshot();
    if (name && !isServer) {
      fetchItems(name);
    }
  });

  const handleLogout = async () => {
    await fetch("/api/v1/auth/logout", { method: "POST" });
    navigate("/");
  };

  // Filtered and paginated items
  const filteredItems = createMemo(() => {
    let result = items();
    const f = filters();

    if (f.name) {
      const regex = globToRegex(f.name);
      result = result.filter(i => regex.test(i.relativePath));
    }
    if (f.type) {
      const regex = globToRegex(f.type);
      result = result.filter(i => regex.test(i.type));
    }
    if (f.owner) {
      const regex = globToRegex(f.owner);
      result = result.filter(i => regex.test(i.owner));
    }

    return result;
  });

  const paginatedItems = createMemo(() => {
    const start = (page() - 1) * pageSize;
    return filteredItems().slice(start, start + pageSize);
  });

  const totalPages = createMemo(() => Math.ceil(filteredItems().length / pageSize) || 1);

  const currentSnapshotInfo = createMemo(() =>
    snapshots().find(s => s.name === selectedSnapshot())
  );

  const canAccess = (minRole: "viewer" | "auditor" | "admin") => {
    const roles = ["viewer", "auditor", "admin"];
    const userRole = user()?.role || "viewer";
    return roles.indexOf(userRole) >= roles.indexOf(minRole);
  };

  return (
    <div style="min-height: 100vh;">
      <Title>Viewer - Folder Inspector</Title>
      <div class="accent-strip"></div>

      {/* Navigation */}
      <nav class="nav">
        <span class="nav-brand">Folder Inspector</span>
        <div class="nav-links">
          <a href="/viewer" class="nav-link active">Viewer</a>
          <Show when={canAccess("auditor")}>
            <a href="/scanner" class="nav-link">Scanner</a>
          </Show>
          <Show when={canAccess("admin")}>
            <a href="/admin" class="nav-link">Admin</a>
          </Show>
        </div>
        <div class="flex items-center gap-4">
          <span style="color: var(--text-muted); font-size: 12px; text-transform: uppercase; letter-spacing: 0.1em;">
            {user()?.username} <span style="color: var(--ge-red);">({user()?.role})</span>
          </span>
          <button class="btn btn-secondary" onClick={handleLogout}>Logout</button>
        </div>
      </nav>

      {/* Content */}
      <div class="container" style="padding: 40px;">
        {/* Snapshot Selector & Info */}
        <div class="card" style="padding: 24px; margin-bottom: 24px;">
          <div class="flex items-center gap-4" style="flex-wrap: wrap;">
            <div>
              <label class="label">Snapshot</label>
              <select
                class="input"
                style="width: 300px;"
                value={selectedSnapshot()}
                onChange={(e) => setSelectedSnapshot(e.currentTarget.value)}
              >
                <For each={snapshots()}>
                  {(snap) => <option value={snap.name}>{snap.name}</option>}
                </For>
              </select>
            </div>

            <Show when={currentSnapshotInfo()}>
              <div style="flex: 1; display: flex; gap: 40px; padding-left: 40px; border-left: 1px solid var(--border);">
                <div>
                  <div class="label">Items</div>
                  <div style="font-size: 20px; font-weight: 700;">{currentSnapshotInfo()?.itemCount.toLocaleString()}</div>
                </div>
                <div>
                  <div class="label">Size</div>
                  <div style="font-size: 20px; font-weight: 700;">{formatSize(currentSnapshotInfo()?.fileSize || 0)}</div>
                </div>
                <div>
                  <div class="label">Created</div>
                  <div style="font-size: 14px; font-weight: 500;">{formatDate(currentSnapshotInfo()?.created || 0)}</div>
                </div>
              </div>
            </Show>
          </div>
        </div>

        {/* Items Table */}
        <div class="card" style="padding: 24px;">
          <div class="flex items-center" style="justify-content: space-between; margin-bottom: 24px;">
            <h2 class="heading" style="font-size: 20px;">Files & Directories</h2>
            <div style="color: var(--text-muted); font-size: 14px;">
              Showing {paginatedItems().length} of {filteredItems().length} items
            </div>
          </div>

          {/* Column Filters */}
          <div class="flex gap-4" style="margin-bottom: 20px;">
            <div style="flex: 2;">
              <input
                type="text"
                class="input"
                placeholder="🔍 Filter name (glob: *.java, src/**)"
                value={filters().name}
                onInput={(e) => setFilters({ ...filters(), name: e.currentTarget.value })}
              />
            </div>
            <div style="flex: 1;">
              <input
                type="text"
                class="input"
                placeholder="Type filter"
                value={filters().type}
                onInput={(e) => setFilters({ ...filters(), type: e.currentTarget.value })}
              />
            </div>
            <div style="flex: 1;">
              <input
                type="text"
                class="input"
                placeholder="Owner filter"
                value={filters().owner}
                onInput={(e) => setFilters({ ...filters(), owner: e.currentTarget.value })}
              />
            </div>
            <button
              class="btn btn-secondary"
              onClick={() => setFilters({ name: "", type: "", owner: "" })}
            >
              Clear
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
                    <th>Modified</th>
                    <th>Owner</th>
                    <th>Permissions</th>
                  </tr>
                </thead>
                <tbody>
                  <For each={paginatedItems()}>
                    {(item) => (
                      <tr>
                        <td class="mono" style="font-size: 13px;">
                          <span style={`font-size: 18px; margin-right: 10px; ${item.type === "Directory" ? "color: #fbbf24" : "color: var(--text-muted)"}`}>
                            {item.type === "Directory" ? "📁" : "📄"}
                          </span>
                          {item.relativePath}
                        </td>
                        <td><span class={`badge ${item.type === "Directory" ? "badge-folder" : "badge-file"}`}>{item.type}</span></td>
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

            {/* Pagination */}
            <div class="flex items-center justify-center gap-4" style="margin-top: 24px;">
              <button
                class="btn btn-secondary"
                disabled={page() === 1}
                onClick={() => setPage(p => Math.max(1, p - 1))}
              >
                ← Previous
              </button>
              <span style="font-weight: 600; font-size: 14px;">
                Page {page()} of {totalPages()}
              </span>
              <button
                class="btn btn-secondary"
                disabled={page() >= totalPages()}
                onClick={() => setPage(p => Math.min(totalPages(), p + 1))}
              >
                Next →
              </button>
            </div>
          </Show>
        </div>
      </div>
    </div>
  );
}
