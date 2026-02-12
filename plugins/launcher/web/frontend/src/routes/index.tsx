import { Title } from "@solidjs/meta";
import { createSignal } from "solid-js";
import { useNavigate } from "@solidjs/router";

export default function Login() {
  const [username, setUsername] = createSignal("");
  const [password, setPassword] = createSignal("");
  const [error, setError] = createSignal("");
  const [loading, setLoading] = createSignal(false);
  const navigate = useNavigate();

  const handleLogin = async (e: Event) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const res = await fetch("/api/v1/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: username(), password: password() }),
      });

      const data = await res.json();

      if (data.success) {
        navigate("/viewer");
      } else {
        setError(data.error || "Login failed");
      }
    } catch (err) {
      setError("Connection error. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main style="min-height: 100vh; display: flex; flex-direction: column;">
      <Title>Login - Folder Inspector</Title>

      {/* Animated accent strip */}
      <div class="accent-strip"></div>

      <div style="flex: 1; display: flex; align-items: center; justify-content: center; padding: 40px;">
        <div class="card animate-fadeIn" style="width: 100%; max-width: 480px; padding: 60px 50px;">
          <div class="text-center mb-4">
            <h1 class="heading" style="font-size: 36px; margin-bottom: 16px; color: var(--ge-red); letter-spacing: -0.02em;">
              Folder Inspector
            </h1>
            <p style="color: var(--text-secondary); font-size: 15px; font-weight: 500;">
              Sign in to explore your file system
            </p>
          </div>

          <form onSubmit={handleLogin} class="flex flex-col gap-4" style="margin-top: 40px;">
            <div>
              <label class="label">Username</label>
              <input
                type="text"
                class="input"
                placeholder="Enter your username"
                value={username()}
                onInput={(e) => setUsername(e.currentTarget.value)}
                required
              />
            </div>

            <div>
              <label class="label">Password</label>
              <input
                type="password"
                class="input"
                placeholder="Enter your password"
                value={password()}
                onInput={(e) => setPassword(e.currentTarget.value)}
                required
              />
            </div>

            {error() && (
              <div class="text-error text-center" style="font-size: 14px; padding: 12px; background: rgba(239, 68, 68, 0.1); border-radius: 8px;">
                {error()}
              </div>
            )}

            <button
              type="submit"
              class="btn btn-primary"
              style="margin-top: 12px; width: 100%; font-size: 15px;"
              disabled={loading()}
            >
              {loading() ? <div class="spinner"></div> : "Sign In"}
            </button>
          </form>

          <p style="margin-top: 32px; text-align: center; font-size: 13px; color: var(--text-muted);">
            Demo credentials: <span style="color: var(--text-secondary); font-weight: 600;">admin / admin123</span>
          </p>
        </div>
      </div>
    </main>
  );
}
