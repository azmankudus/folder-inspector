const API_URL = "http://localhost:8080/api";

export function getToken() {
  if (typeof window !== "undefined") {
    const local = localStorage.getItem("token");
    if (local) return local;

    const match = document.cookie.match(/(^|;)\s*token\s*=\s*([^;]+)/);
    return match ? match[2] : null;
  }
  return null;
}

export function setToken(token: string) {
  if (typeof window !== "undefined") {
    localStorage.setItem("token", token);
  }
}

export function getUserInfo() {
  const token = getToken();
  if (!token) return null;
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    const payload = JSON.parse(jsonPayload);
    return {
      username: payload.sub,
      roles: payload.roles || []
    };
  } catch (e) {
    console.error("JWT Parse Error:", e);
    return null;
  }
}

export function removeToken() {
  if (typeof window !== "undefined") {
    localStorage.removeItem("token");
    document.cookie = "token=; Max-Age=0; path=/; SameSite=Lax";
  }
}

async function request(endpoint: string, options: RequestInit = {}) {
  const token = getToken();
  const headers = new Headers(options.headers);
  headers.set("Content-Type", "application/json");
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const res = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!res.ok) {
    if (res.status === 401) {
      removeToken();
      if (typeof window !== "undefined" && window.location.pathname !== "/access/login") {
        window.location.href = "/access/login";
      }
    }
    const err = await res.text().catch(() => res.statusText);
    const errorMessage = err || `Request failed with status ${res.status}`;
    console.error(`[API Error] ${options.method || 'GET'} ${endpoint}:`, errorMessage);
    throw new Error(errorMessage);
  }

  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  get: (endpoint: string) => request(endpoint),
  post: (endpoint: string, body: any) => request(endpoint, { method: "POST", body: JSON.stringify(body) }),
  put: (endpoint: string, body: any) => request(endpoint, { method: "PUT", body: JSON.stringify(body) }),
  delete: (endpoint: string) => request(endpoint, { method: "DELETE" }),
  download: async (endpoint: string) => {
    const token = getToken();
    const headers = new Headers();
    if (token) headers.set("Authorization", `Bearer ${token}`);
    const res = await fetch(`${API_URL}${endpoint}`, { headers });
    if (!res.ok) throw new Error("Download failed");
    return res.blob();
  }
};
