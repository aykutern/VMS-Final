import { http } from "../lib/http.js";

export async function login({ username, password }) {
  const { data } = await http.post("/api/auth/login", { username, password });

  // Store the user session info returned by the backend
  localStorage.setItem("user", JSON.stringify(data));

  return data;
}

export function logout() {
  localStorage.removeItem("user");
}

export function isAuthenticated() {
  return !!localStorage.getItem("user");
}