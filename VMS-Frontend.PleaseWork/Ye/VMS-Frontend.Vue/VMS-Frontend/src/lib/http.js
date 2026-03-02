import axios from "axios";
import { getCurrentUser } from "../services/authService.js";

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8081",
  withCredentials: false,
});

http.interceptors.request.use((config) => {
  const user = getCurrentUser();
  if (user?.id) {
    config.headers["X-User"] = user.username;
  }
  return config;
});