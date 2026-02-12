import { Title } from "@solidjs/meta";
import { createSignal, onMount, For, Show, createMemo } from "solid-js";
import { useNavigate } from "@solidjs/router";
import { isServer } from "solid-js/web";

interface Scan {
  id: number;
  status: "pending" | "running" | "completed" | "failed";
  protocol: string;
  hostname: string;
  rootPath: string;
  startTime: number;
  endTime: number | null;
  snapshotName: string | null;
}

interface ScanDetail extends Scan {
  port: number;
  username: string;
  stdout: string;
  stderr: string;
}

interface UserInfo {
  username: string;
  role: "viewer" | "auditor" | "admin";
}

const formatDate = (timestamp: number): string => {
  if (!timestamp) return "—";
  return new Date(timestamp * 1000).toLocaleString();
};

const PROTOCOLS = [
  { value: "local", label: "Local", fields: ["path"] },
  { value: "ssh", label: "SSH", fields: ["host", "port", "path", "username", "password"] },
  { value: "smb", label: "SMB", fields: ["host", "share", "path", "username", "password"] },
  { value: "nfs", label: "NFS", fields: ["host", "export", "path"] },
  { value: "s3", label: "S3", fields: ["bucket", "prefix", "accessKey", "secretKey", "region"] },
];

// Mock data for demo
const MOCK_SCANS: Scan[] = [
  { id: 1, status: "completed", protocol: "ssh", hostname: "server1.local", rootPath: "/var/log", startTime: 1706900000, endTime: 1706900300, snapshotName: "server1-logs" },
  { id: 2, status: "completed", protocol: "smb", hostname: "nas.local", rootPath: "\\\\nas\\share", startTime: 1706850000, endTime: 1706850600, snapshotName: "nas-share-backup" },
  { id: 3, status: "running", protocol: "s3", hostname: "my-bucket", rootPath: "s3://my-bucket/data", startTime: 1706903600, endTime: null, snapshotName: null },
  { id: 4, status: "failed", protocol: "nfs", hostname: "storage.local", rootPath: "/exports/data", startTime: 1706800000, endTime: 1706800100, snapshotName: null },
];

export default function Scanner() {
  const navigate = useNavigate();
  const [user, setUser] = createSignal<UserInfo | null>(null);
  const [scans, setScans] = createSignal<Scan[]>(MOCK_SCANS);
  const [selectedScan, setSelectedScan] = createSignal<ScanDetail | null>(null);
  const [showNewScanForm, setShowNewScanForm] = createSignal(false);
  const [loading, setLoading] = createSignal(false);

  // New scan form
  const [newScan, setNewScan] = createSignal({
    protocol: "local",
    host: "",
    port: "22",
    path: "",
    username: "",
    password: "",
    share: "",
    export: "",
    bucket: "",
    prefix: "",
    accessKey: "",
    secretKey: "",
    region: "us-east-1",
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
      const role = data.role || "viewer";
      if (role === "viewer") {
        navigate("/viewer");
        return;
      }
      setUser({ username: data.username, role });
      // fetchScans();
    } catch (err) {
      navigate("/");
    }
  });

  const handleLogout = async () => {
    await fetch("/api/v1/auth/logout", { method: "POST" });
    navigate("/");
  };

  const viewScanDetails = (scan: Scan) => {
    // Mock detail data
    setSelectedScan({
      ...scan,
      port: 22,
      username: "admin",
      stdout: `[INFO] Connecting to ${scan.hostname}...\n[INFO] Scanning ${scan.rootPath}\n[INFO] Found 1,234 items\n[INFO] Scan completed successfully`,
      stderr: scan.status === "failed" ? "[ERROR] Connection refused\n[ERROR] Failed to authenticate" : "",
    });
  };

  const currentProtocol = createMemo(() =>
    PROTOCOLS.find(p => p.value === newScan().protocol)
  );

  const submitNewScan = async () => {
    setLoading(true);
    // Mock submission
    setTimeout(() => {
      const newEntry: Scan = {
        id: scans().length + 1,
        status: "pending",
        protocol: newScan().protocol,
        hostname: newScan().host || "localhost",
        rootPath: newScan().path || "/",
        startTime: Math.floor(Date.now() / 1000),
        endTime: null,
        snapshotName: null,
      };
      setScans([newEntry, ...scans()]);
      setShowNewScanForm(false);
      setLoading(false);
    }, 1000);
  };

  const canAccess = (minRole: "viewer" | "auditor" | "admin") => {
    const roles = ["viewer", "auditor", "admin"];
    const userRole = user()?.role || "viewer";
    return roles.indexOf(userRole) >= roles.indexOf(minRole);
  };

  const getStatusBadge = (status: string) => {
    const colors: Record<string, string> = {
      pending: "background: rgba(251, 191, 36, 0.15); color: #fbbf24",
      running: "background: rgba(59, 130, 246, 0.15); color: #3b82f6",
      completed: "background: rgba(16, 185, 129, 0.15); color: #10b981",
      failed: "background: rgba(239, 68, 68, 0.15); color: #ef4444",
    };
    return colors[status] || "";
  };

  return (
    <div style="min-height: 100vh;">
      <Title>Scanner - Folder Inspector</Title>
      <div class="accent-strip"></div>

      <nav class="nav">
        <span class="nav-brand">Folder Inspector</span>
        <div class="nav-links">
          <a href="/viewer" class="nav-link">Viewer</a>
          <a href="/scanner" class="nav-link active">Scanner</a>
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

      <div class="container" style="padding: 40px;">
        {/* Header */}
        <div class="flex items-center" style="justify-content: space-between; margin-bottom: 24px;">
          <h1 class="heading" style="font-size: 28px;">Scan History</h1>
          <Show when={canAccess("admin")}>
            <button class="btn btn-primary" onClick={() => setShowNewScanForm(true)}>
              + Submit New Scan
            </button>
          </Show>
        </div>

        {/* New Scan Form Modal */}
        <Show when={showNewScanForm()}>
          <div style="position: fixed; inset: 0; background: rgba(0,0,0,0.8); display: flex; align-items: center; justify-content: center; z-index: 100;">
            <div class="card animate-fadeIn" style="width: 600px; max-height: 90vh; overflow-y: auto; padding: 40px;">
              <h2 class="heading" style="font-size: 22px; margin-bottom: 24px;">Submit New Scan</h2>

              <div class="flex flex-col gap-4">
                <div>
                  <label class="label">Protocol</label>
                  <select
                    class="input"
                    value={newScan().protocol}
                    onChange={(e) => setNewScan({ ...newScan(), protocol: e.currentTarget.value })}
                  >
                    <For each={PROTOCOLS}>
                      {(p) => <option value={p.value}>{p.label}</option>}
                    </For>
                  </select>
                </div>

                <Show when={currentProtocol()?.fields.includes("host")}>
                  <div>
                    <label class="label">Host</label>
                    <input
                      type="text"
                      class="input"
                      placeholder="hostname or IP"
                      value={newScan().host}
                      onInput={(e) => setNewScan({ ...newScan(), host: e.currentTarget.value })}
                    />
                  </div>
                </Show>

                <Show when={currentProtocol()?.fields.includes("port")}>
                  <div>
                    <label class="label">Port</label>
                    <input
                      type="text"
                      class="input"
                      value={newScan().port}
                      onInput={(e) => setNewScan({ ...newScan(), port: e.currentTarget.value })}
                    />
                  </div>
                </Show>

                <Show when={currentProtocol()?.fields.includes("path")}>
                  <div>
                    <label class="label">Path</label>
                    <input
                      type="text"
                      class="input"
                      placeholder="/path/to/scan"
                      value={newScan().path}
                      onInput={(e) => setNewScan({ ...newScan(), path: e.currentTarget.value })}
                    />
                  </div>
                </Show>

                <Show when={currentProtocol()?.fields.includes("share")}>
                  <div>
                    <label class="label">Share Name</label>
                    <input
                      type="text"
                      class="input"
                      placeholder="shared_folder"
                      value={newScan().share}
                      onInput={(e) => setNewScan({ ...newScan(), share: e.currentTarget.value })}
                    />
                  </div>
                </Show>

                <Show when={currentProtocol()?.fields.includes("export")}>
                  <div>
                    <label class="label">NFS Export</label>
                    <input
                      type="text"
                      class="input"
                      placeholder="/exports/data"
                      value={newScan().export}
                      onInput={(e) => setNewScan({ ...newScan(), export: e.currentTarget.value })}
                    />
                  </div>
                </Show>

                <Show when={currentProtocol()?.fields.includes("bucket")}>
                  <div>
                    <label class="label">S3 Bucket</label>
                    <input
                      type="text"
                      class="input"
                      placeholder="my-bucket"
                      value={newScan().bucket}
                      onInput={(e) => setNewScan({ ...newScan(), bucket: e.currentTarget.value })}
                    />
                  </div>
                </Show>

                <Show when={currentProtocol()?.fields.includes("prefix")}>
                  <div>
                    <label class="label">S3 Prefix</label>
                    <input
                      type="text"
                      class="input"
                      placeholder="data/"
                      value={newScan().prefix}
                      onInput={(e) => setNewScan({ ...newScan(), prefix: e.currentTarget.value })}
                    />
                  </div>
                </Show>

                <Show when={currentProtocol()?.fields.includes("username")}>
                  <div>
                    <label class="label">Username</label>
                    <input
                      type="text"
                      class="input"
                      value={newScan().username}
                      onInput={(e) => setNewScan({ ...newScan(), username: e.currentTarget.value })}
                    />
                  </div>
                </Show>

                <Show when={currentProtocol()?.fields.includes("password")}>
                  <div>
                    <label class="label">Password</label>
                    <input
                      type="password"
                      class="input"
                      value={newScan().password}
                      onInput={(e) => setNewScan({ ...newScan(), password: e.currentTarget.value })}
                    />
                  </div>
                </Show>

                <Show when={currentProtocol()?.fields.includes("accessKey")}>
                  <div>
                    <label class="label">Access Key</label>
                    <input
                      type="text"
                      class="input"
                      value={newScan().accessKey}
                      onInput={(e) => setNewScan({ ...newScan(), accessKey: e.currentTarget.value })}
                    />
                  </div>
                </Show>

                <Show when={currentProtocol()?.fields.includes("secretKey")}>
                  <div>
                    <label class="label">Secret Key</label>
                    <input
                      type="password"
                      class="input"
                      value={newScan().secretKey}
                      onInput={(e) => setNewScan({ ...newScan(), secretKey: e.currentTarget.value })}
                    />
                  </div>
                </Show>

                <Show when={currentProtocol()?.fields.includes("region")}>
                  <div>
                    <label class="label">Region</label>
                    <input
                      type="text"
                      class="input"
                      value={newScan().region}
                      onInput={(e) => setNewScan({ ...newScan(), region: e.currentTarget.value })}
                    />
                  </div>
                </Show>

                <div class="flex gap-4" style="margin-top: 16px;">
                  <button class="btn btn-primary" style="flex: 1;" onClick={submitNewScan} disabled={loading()}>
                    {loading() ? <div class="spinner"></div> : "Submit Scan"}
                  </button>
                  <button class="btn btn-secondary" onClick={() => setShowNewScanForm(false)}>
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          </div>
        </Show>

        {/* Scan Detail Modal */}
        <Show when={selectedScan()}>
          <div style="position: fixed; inset: 0; background: rgba(0,0,0,0.8); display: flex; align-items: center; justify-content: center; z-index: 100;">
            <div class="card animate-fadeIn" style="width: 800px; max-height: 90vh; overflow-y: auto; padding: 40px;">
              <div class="flex items-center" style="justify-content: space-between; margin-bottom: 24px;">
                <h2 class="heading" style="font-size: 22px;">Scan Details</h2>
                <button class="btn btn-secondary" onClick={() => setSelectedScan(null)}>✕ Close</button>
              </div>

              <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 24px;">
                <div>
                  <div class="label">Protocol</div>
                  <div style="font-size: 16px; font-weight: 600; text-transform: uppercase;">{selectedScan()?.protocol}</div>
                </div>
                <div>
                  <div class="label">Hostname</div>
                  <div style="font-size: 16px; font-weight: 600;">{selectedScan()?.hostname}</div>
                </div>
                <div>
                  <div class="label">Status</div>
                  <span class="badge" style={getStatusBadge(selectedScan()?.status || "")}>{selectedScan()?.status}</span>
                </div>
                <div>
                  <div class="label">Port</div>
                  <div style="font-weight: 500;">{selectedScan()?.port}</div>
                </div>
                <div>
                  <div class="label">Username</div>
                  <div style="font-weight: 500;">{selectedScan()?.username}</div>
                </div>
                <div>
                  <div class="label">Root Path</div>
                  <div class="mono" style="font-size: 13px;">{selectedScan()?.rootPath}</div>
                </div>
              </div>

              <div style="margin-bottom: 20px;">
                <div class="label">Standard Output</div>
                <pre class="mono" style="background: var(--bg-secondary); padding: 16px; border-radius: 8px; font-size: 13px; white-space: pre-wrap; border: 1px solid var(--border);">
                  {selectedScan()?.stdout || "(no output)"}
                </pre>
              </div>

              <Show when={selectedScan()?.stderr}>
                <div>
                  <div class="label" style="color: var(--error);">Error Output</div>
                  <pre class="mono" style="background: rgba(239, 68, 68, 0.1); padding: 16px; border-radius: 8px; font-size: 13px; white-space: pre-wrap; border: 1px solid rgba(239, 68, 68, 0.3); color: var(--error);">
                    {selectedScan()?.stderr}
                  </pre>
                </div>
              </Show>
            </div>
          </div>
        </Show>

        {/* Scan History Table */}
        <div class="card" style="padding: 24px;">
          <div style="overflow-x: auto; border-radius: 12px; border: 1px solid var(--border);">
            <table class="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Protocol</th>
                  <th>Target</th>
                  <th>Status</th>
                  <th>Started</th>
                  <th>Duration</th>
                  <th>Snapshot</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <For each={scans()}>
                  {(scan) => (
                    <tr>
                      <td style="font-weight: 700;">#{scan.id}</td>
                      <td><span class="badge" style="background: rgba(16, 30, 142, 0.2); color: #6b7ceb;">{scan.protocol.toUpperCase()}</span></td>
                      <td>
                        <div style="font-weight: 600;">{scan.hostname}</div>
                        <div class="mono" style="font-size: 12px; color: var(--text-muted);">{scan.rootPath}</div>
                      </td>
                      <td><span class="badge" style={getStatusBadge(scan.status)}>{scan.status}</span></td>
                      <td style="font-size: 13px;">{formatDate(scan.startTime)}</td>
                      <td style="font-weight: 500;">
                        {scan.endTime ? `${Math.round((scan.endTime - scan.startTime) / 60)}m` : "—"}
                      </td>
                      <td>
                        {scan.snapshotName ? (
                          <a href={`/viewer?snapshot=${scan.snapshotName}`} style="color: var(--ge-red); font-weight: 600;">
                            {scan.snapshotName}
                          </a>
                        ) : "—"}
                      </td>
                      <td>
                        <button class="btn btn-secondary" style="padding: 8px 16px; font-size: 12px;" onClick={() => viewScanDetails(scan)}>
                          View
                        </button>
                      </td>
                    </tr>
                  )}
                </For>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
