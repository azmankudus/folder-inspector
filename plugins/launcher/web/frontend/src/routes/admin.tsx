import { Title } from "@solidjs/meta";
import { createSignal, onMount, For, Show } from "solid-js";
import { useNavigate } from "@solidjs/router";
import { isServer } from "solid-js/web";

interface User {
  id: number;
  username: string;
  role: "viewer" | "auditor" | "admin";
  createdAt: number;
}

interface AuditEntry {
  id: number;
  user: string;
  action: string;
  details: string;
  timestamp: number;
}

interface UserInfo {
  username: string;
  role: "viewer" | "auditor" | "admin";
}

const formatDate = (timestamp: number): string => {
  if (!timestamp) return "—";
  return new Date(timestamp * 1000).toLocaleString();
};

// Mock data
const MOCK_USERS: User[] = [
  { id: 1, username: "admin", role: "admin", createdAt: 1706000000 },
  { id: 2, username: "auditor1", role: "auditor", createdAt: 1706100000 },
  { id: 3, username: "viewer1", role: "viewer", createdAt: 1706200000 },
  { id: 4, username: "viewer2", role: "viewer", createdAt: 1706300000 },
];

const MOCK_AUDIT: AuditEntry[] = [
  { id: 1, user: "admin", action: "LOGIN", details: "Successful login from 192.168.1.100", timestamp: 1706903600 },
  { id: 2, user: "admin", action: "SCAN_SUBMIT", details: "Submitted SSH scan to server1.local", timestamp: 1706903500 },
  { id: 3, user: "auditor1", action: "LOGIN", details: "Successful login from 192.168.1.50", timestamp: 1706902000 },
  { id: 4, user: "auditor1", action: "VIEW_SNAPSHOT", details: "Viewed snapshot: server1-logs", timestamp: 1706901800 },
  { id: 5, user: "viewer1", action: "LOGIN", details: "Successful login from 192.168.1.25", timestamp: 1706900000 },
  { id: 6, user: "viewer2", action: "LOGIN_FAILED", details: "Invalid password from 10.0.0.5", timestamp: 1706899000 },
];

export default function Admin() {
  const navigate = useNavigate();
  const [user, setUser] = createSignal<UserInfo | null>(null);
  const [activeTab, setActiveTab] = createSignal<"audit" | "users">("audit");
  const [users, setUsers] = createSignal<User[]>(MOCK_USERS);
  const [auditLog, setAuditLog] = createSignal<AuditEntry[]>(MOCK_AUDIT);
  const [showUserForm, setShowUserForm] = createSignal(false);
  const [editingUser, setEditingUser] = createSignal<User | null>(null);
  const [loading, setLoading] = createSignal(false);

  // User form
  const [formData, setFormData] = createSignal({
    username: "",
    password: "",
    role: "viewer" as "viewer" | "auditor" | "admin",
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
      if (role !== "admin") {
        navigate("/viewer");
        return;
      }
      setUser({ username: data.username, role });
    } catch (err) {
      navigate("/");
    }
  });

  const handleLogout = async () => {
    await fetch("/api/v1/auth/logout", { method: "POST" });
    navigate("/");
  };

  const openUserForm = (existingUser?: User) => {
    if (existingUser) {
      setEditingUser(existingUser);
      setFormData({
        username: existingUser.username,
        password: "",
        role: existingUser.role,
      });
    } else {
      setEditingUser(null);
      setFormData({ username: "", password: "", role: "viewer" });
    }
    setShowUserForm(true);
  };

  const saveUser = async () => {
    setLoading(true);
    setTimeout(() => {
      const form = formData();
      if (editingUser()) {
        // Update
        setUsers(users().map(u =>
          u.id === editingUser()!.id
            ? { ...u, username: form.username, role: form.role }
            : u
        ));
      } else {
        // Create
        const newUser: User = {
          id: Math.max(...users().map(u => u.id)) + 1,
          username: form.username,
          role: form.role,
          createdAt: Math.floor(Date.now() / 1000),
        };
        setUsers([...users(), newUser]);
      }
      setShowUserForm(false);
      setLoading(false);
    }, 500);
  };

  const deleteUser = (id: number) => {
    if (confirm("Are you sure you want to delete this user?")) {
      setUsers(users().filter(u => u.id !== id));
    }
  };

  const getActionBadge = (action: string) => {
    if (action.includes("LOGIN_FAILED")) return "background: rgba(239, 68, 68, 0.15); color: #ef4444";
    if (action.includes("LOGIN")) return "background: rgba(16, 185, 129, 0.15); color: #10b981";
    if (action.includes("SCAN")) return "background: rgba(59, 130, 246, 0.15); color: #3b82f6";
    if (action.includes("VIEW")) return "background: rgba(139, 92, 246, 0.15); color: #8b5cf6";
    return "background: rgba(160, 160, 176, 0.15); color: var(--text-secondary)";
  };

  const getRoleBadge = (role: string) => {
    const colors: Record<string, string> = {
      admin: "background: rgba(210, 59, 68, 0.15); color: var(--ge-red)",
      auditor: "background: rgba(59, 130, 246, 0.15); color: #3b82f6",
      viewer: "background: rgba(160, 160, 176, 0.15); color: var(--text-secondary)",
    };
    return colors[role] || "";
  };

  return (
    <div style="min-height: 100vh;">
      <Title>Admin - Folder Inspector</Title>
      <div class="accent-strip"></div>

      <nav class="nav">
        <span class="nav-brand">Folder Inspector</span>
        <div class="nav-links">
          <a href="/viewer" class="nav-link">Viewer</a>
          <a href="/scanner" class="nav-link">Scanner</a>
          <a href="/admin" class="nav-link active">Admin</a>
        </div>
        <div class="flex items-center gap-4">
          <span style="color: var(--text-muted); font-size: 12px; text-transform: uppercase; letter-spacing: 0.1em;">
            {user()?.username} <span style="color: var(--ge-red);">({user()?.role})</span>
          </span>
          <button class="btn btn-secondary" onClick={handleLogout}>Logout</button>
        </div>
      </nav>

      <div class="container" style="padding: 40px;">
        {/* Tabs */}
        <div class="flex gap-4" style="margin-bottom: 24px;">
          <button
            class={`btn ${activeTab() === "audit" ? "btn-primary" : "btn-secondary"}`}
            onClick={() => setActiveTab("audit")}
          >
            📋 Audit Log
          </button>
          <button
            class={`btn ${activeTab() === "users" ? "btn-primary" : "btn-secondary"}`}
            onClick={() => setActiveTab("users")}
          >
            👥 User Management
          </button>
        </div>

        {/* User Form Modal */}
        <Show when={showUserForm()}>
          <div style="position: fixed; inset: 0; background: rgba(0,0,0,0.8); display: flex; align-items: center; justify-content: center; z-index: 100;">
            <div class="card animate-fadeIn" style="width: 480px; padding: 40px;">
              <h2 class="heading" style="font-size: 22px; margin-bottom: 24px;">
                {editingUser() ? "Edit User" : "Create User"}
              </h2>

              <div class="flex flex-col gap-4">
                <div>
                  <label class="label">Username</label>
                  <input
                    type="text"
                    class="input"
                    value={formData().username}
                    onInput={(e) => setFormData({ ...formData(), username: e.currentTarget.value })}
                    disabled={!!editingUser()}
                  />
                </div>

                <Show when={!editingUser()}>
                  <div>
                    <label class="label">Password</label>
                    <input
                      type="password"
                      class="input"
                      value={formData().password}
                      onInput={(e) => setFormData({ ...formData(), password: e.currentTarget.value })}
                    />
                  </div>
                </Show>

                <div>
                  <label class="label">Role</label>
                  <select
                    class="input"
                    value={formData().role}
                    onChange={(e) => setFormData({ ...formData(), role: e.currentTarget.value as any })}
                  >
                    <option value="viewer">Viewer</option>
                    <option value="auditor">Auditor</option>
                    <option value="admin">Admin</option>
                  </select>
                </div>

                <div class="flex gap-4" style="margin-top: 16px;">
                  <button class="btn btn-primary" style="flex: 1;" onClick={saveUser} disabled={loading()}>
                    {loading() ? <div class="spinner"></div> : "Save"}
                  </button>
                  <button class="btn btn-secondary" onClick={() => setShowUserForm(false)}>
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          </div>
        </Show>

        {/* Audit Log Tab */}
        <Show when={activeTab() === "audit"}>
          <div class="card" style="padding: 24px;">
            <div class="flex items-center" style="justify-content: space-between; margin-bottom: 24px;">
              <h2 class="heading" style="font-size: 20px;">Audit Log</h2>
              <span style="color: var(--text-muted); font-size: 14px;">
                {auditLog().length} entries
              </span>
            </div>

            <div style="overflow-x: auto; border-radius: 12px; border: 1px solid var(--border);">
              <table class="table">
                <thead>
                  <tr>
                    <th>Timestamp</th>
                    <th>User</th>
                    <th>Action</th>
                    <th>Details</th>
                  </tr>
                </thead>
                <tbody>
                  <For each={auditLog()}>
                    {(entry) => (
                      <tr>
                        <td style="font-size: 13px; white-space: nowrap;">{formatDate(entry.timestamp)}</td>
                        <td style="font-weight: 600;">{entry.user}</td>
                        <td><span class="badge" style={getActionBadge(entry.action)}>{entry.action}</span></td>
                        <td style="color: var(--text-secondary); font-size: 13px;">{entry.details}</td>
                      </tr>
                    )}
                  </For>
                </tbody>
              </table>
            </div>
          </div>
        </Show>

        {/* Users Tab */}
        <Show when={activeTab() === "users"}>
          <div class="card" style="padding: 24px;">
            <div class="flex items-center" style="justify-content: space-between; margin-bottom: 24px;">
              <h2 class="heading" style="font-size: 20px;">User Management</h2>
              <button class="btn btn-primary" onClick={() => openUserForm()}>
                + Add User
              </button>
            </div>

            <div style="overflow-x: auto; border-radius: 12px; border: 1px solid var(--border);">
              <table class="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Username</th>
                    <th>Role</th>
                    <th>Created</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <For each={users()}>
                    {(u) => (
                      <tr>
                        <td style="font-weight: 700;">#{u.id}</td>
                        <td style="font-weight: 600;">{u.username}</td>
                        <td><span class="badge" style={getRoleBadge(u.role)}>{u.role.toUpperCase()}</span></td>
                        <td style="font-size: 13px;">{formatDate(u.createdAt)}</td>
                        <td>
                          <div class="flex gap-4">
                            <button class="btn btn-secondary" style="padding: 8px 16px; font-size: 12px;" onClick={() => openUserForm(u)}>
                              Edit
                            </button>
                            <button
                              class="btn btn-secondary"
                              style="padding: 8px 16px; font-size: 12px; color: var(--error);"
                              onClick={() => deleteUser(u.id)}
                              disabled={u.username === "admin"}
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    )}
                  </For>
                </tbody>
              </table>
            </div>
          </div>
        </Show>
      </div>
    </div>
  );
}
