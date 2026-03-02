import { http } from "../lib/http.js";

const STORAGE_KEY = "user";

// Which storage to use — set by login()
function getStorage() {
  // If we stored with "remember me", localStorage has the key; otherwise sessionStorage
  return localStorage.getItem(STORAGE_KEY)
    ? localStorage
    : sessionStorage;
}

export async function login({ username, password, rememberMe = false }) {
  const { data } = await http.post("/api/auth/login", { username, password });

  // Remember me → persist across browser restarts; otherwise session-only
  const storage = rememberMe ? localStorage : sessionStorage;
  storage.setItem(STORAGE_KEY, JSON.stringify(data));

  return data;
}

export function logout() {
  localStorage.removeItem(STORAGE_KEY);
  sessionStorage.removeItem(STORAGE_KEY);
}

export function isAuthenticated() {
  return !!(localStorage.getItem(STORAGE_KEY) || sessionStorage.getItem(STORAGE_KEY));
}

export function getCurrentUser() {
  const raw = localStorage.getItem(STORAGE_KEY) || sessionStorage.getItem(STORAGE_KEY);
  return raw ? JSON.parse(raw) : null;
}