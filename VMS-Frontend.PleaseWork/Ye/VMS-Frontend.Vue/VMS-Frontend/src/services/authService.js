import { http } from "../lib/http.js";

const STORAGE_KEY = "user";

export async function login({ username, password }) {
  const { data } = await http.post("/api/auth/login", { username, password });
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
  return data;
}

export async function register(payload) {
  const { data } = await http.post("/api/auth/register", payload);
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
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