<template>
    <div class="login-page">
      <!-- Background -->
      <div class="bg"></div>
      <div class="bg-overlay"></div>
  
      <!-- Content -->
      <div class="content">
        <div class="left">
          <div class="brand">
            <div class="logo">VMS</div>
            <div class="title">Vendor Management System</div>
            <div class="subtitle">
              Coordinate vendors like a control tower: track, approve, and deliver with clarity.
            </div>
          </div>
  
          <ul class="bullets">
            <li>Vendor onboarding & profiles</li>
            <li>Contracts, SLAs, and approvals</li>
            <li>Project tasks & status tracking</li>
            <li>Role-based access & audit trails</li>
          </ul>
        </div>
  
        <div class="right">
          <form class="card" @submit.prevent="onSubmit">
            <div class="card-header">
              <h2>Sign in</h2>
              <p>Use your account to access the VMS portal.</p>
            </div>
  
            <div class="field">
              <label for="username">Username</label>
              <input
                id="username"
                v-model.trim="form.username"
                type="text"
                autocomplete="username"
                placeholder="Enter your username"
                required
              />
            </div>
  
            <div class="field">
              <label for="password">Password</label>
              <input
                id="password"
                v-model="form.password"
                type="password"
                autocomplete="current-password"
                placeholder="••••••••"
                required
              />
            </div>
  
            <div class="row">
              <label class="checkbox">
                <input v-model="form.remember" type="checkbox" />
                <span>Remember me</span>
              </label>
              <button class="link" type="button" @click="onForgotPassword">
                Forgot password?
              </button>
            </div>
  
            <button class="primary" type="submit" :disabled="loading">
              {{ loading ? "Signing in..." : "Sign in" }}
            </button>
  
            <div v-if="error" class="error">{{ error }}</div>
  
            <div class="divider"><span>or</span></div>
  
            <button class="secondary" type="button" @click="onRegister">
              Create an account
            </button>
          </form>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { reactive, ref } from "vue";
  import { useRouter } from "vue-router";
  import { login } from "../services/authService.js";

  const loading = ref(false);
  const error = ref<string | null>(null);
  const router = useRouter();

  const form = reactive({
    username: "",
    password: "",
    remember: true,
    rememberMe: true,
  });

async function onSubmit() {
  error.value = null;

  if (!form.username || !form.password) {
    error.value = "Please enter your username and password.";
    return;
  }

  loading.value = true;
  try {
    const user = await login({ username: form.username, password: form.password, rememberMe: form.rememberMe });
    if (user.userType === "MANAGER") {
      await router.push("/pm/dashboard");
    } else if (user.userType === "VENDOR_ADMIN") {
      await router.push("/vendor/dashboard");
    } else {
      await router.push("/personnel");
    }
  } catch (e: any) {
    error.value =
      e?.response?.data?.message ??
      e?.message ??
      "Login failed. Check your credentials.";
  } finally {
    loading.value = false;
  }
}

  function onForgotPassword() {
    alert("Forgot password (stub).");
  }

  function onRegister() {
    alert("Register (stub).");
  }
  </script>
  
  <style scoped>
  .login-page {
    min-height: 100vh;
    position: relative;
    overflow: hidden;
    color: #eaf1ff;
    font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial, "Noto Sans", "Liberation Sans";
  }
  
  /* Background image */
  .bg {
    position: absolute;
    inset: 0;
    background-image: url("../assets/VMS-Theme.png");
    background-size: cover;
    background-position: center;
    transform: scale(1.03);
    filter: blur(0px);
  }
  
  /* Dark overlay for readability */
  .bg-overlay {
    position: absolute;
    inset: 0;
    background: radial-gradient(1200px circle at 25% 20%, rgba(12, 28, 56, 0.35), transparent 55%),
      linear-gradient(90deg, rgba(6, 12, 24, 0.85), rgba(6, 12, 24, 0.35));
  }
  
  /* Layout */
  .content {
    position: relative;
    z-index: 1;
    min-height: 100vh;
    display: grid;
    grid-template-columns: 1.1fr 0.9fr;
    gap: 48px;
    padding: 56px clamp(20px, 5vw, 72px);
    align-items: center;
  }
  
  .left .brand .logo {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    display: grid;
    place-items: center;
    font-weight: 800;
    letter-spacing: 0.06em;
    background: rgba(255, 255, 255, 0.10);
    border: 1px solid rgba(255, 255, 255, 0.18);
    backdrop-filter: blur(10px);
    box-shadow: 0 18px 40px rgba(0, 0, 0, 0.35);
  }
  
  .title {
    margin-top: 16px;
    font-size: 40px;
    line-height: 1.1;
    font-weight: 800;
    color: #f3f7ff;
  }
  
  .subtitle {
    margin-top: 14px;
    max-width: 520px;
    color: rgba(234, 241, 255, 0.85);
    font-size: 16px;
    line-height: 1.6;
  }
  
  .bullets {
    margin-top: 22px;
    padding-left: 18px;
    color: rgba(234, 241, 255, 0.78);
  }
  
  .bullets li {
    margin: 10px 0;
  }
  
  /* Card */
  .right {
    display: grid;
    justify-items: end;
  }
  
  .card {
    width: min(420px, 100%);
    border-radius: 20px;
    padding: 26px;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.16);
    backdrop-filter: blur(16px);
    box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
  }
  
  .card-header h2 {
    margin: 0;
    font-size: 22px;
    font-weight: 800;
    color: #f7faff;
  }
  
  .card-header p {
    margin: 6px 0 0;
    color: rgba(234, 241, 255, 0.75);
    font-size: 14px;
  }
  
  .field {
    margin-top: 16px;
  }
  
  label {
    display: block;
    font-size: 13px;
    color: rgba(234, 241, 255, 0.78);
    margin-bottom: 8px;
  }
  
  input {
    width: 100%;
    border-radius: 14px;
    padding: 12px 12px;
    border: 1px solid rgba(255, 255, 255, 0.18);
    background: rgba(5, 10, 20, 0.35);
    color: #f3f7ff;
    outline: none;
  }
  
  input::placeholder {
    color: rgba(234, 241, 255, 0.40);
  }
  
  input:focus {
    border-color: rgba(120, 170, 255, 0.55);
    box-shadow: 0 0 0 3px rgba(80, 140, 255, 0.18);
  }
  
  .row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12px;
  }
  
  .checkbox {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    user-select: none;
  }
  
  .checkbox input {
    width: 16px;
    height: 16px;
    margin: 0;
  }
  
  .link {
    background: transparent;
    border: none;
    color: rgba(170, 205, 255, 0.95);
    cursor: pointer;
    padding: 0;
    font-size: 13px;
  }
  
  .primary {
    margin-top: 16px;
    width: 100%;
    border: none;
    border-radius: 14px;
    padding: 12px 14px;
    font-weight: 700;
    cursor: pointer;
    color: #071326;
    background: rgba(200, 225, 255, 0.95);
  }
  
  .primary:disabled {
    opacity: 0.65;
    cursor: not-allowed;
  }
  
  .secondary {
    width: 100%;
    border-radius: 14px;
    padding: 12px 14px;
    font-weight: 700;
    cursor: pointer;
    background: rgba(255, 255, 255, 0.10);
    border: 1px solid rgba(255, 255, 255, 0.16);
    color: #f3f7ff;
  }
  
  .error {
    margin-top: 12px;
    padding: 10px 12px;
    border-radius: 12px;
    background: rgba(255, 90, 90, 0.12);
    border: 1px solid rgba(255, 90, 90, 0.25);
    color: rgba(255, 200, 200, 0.95);
    font-size: 13px;
  }
  
  .divider {
    margin: 18px 0;
    display: grid;
    place-items: center;
    position: relative;
    color: rgba(234, 241, 255, 0.55);
    font-size: 12px;
  }
  
  .divider::before {
    content: "";
    position: absolute;
    left: 0;
    right: 0;
    height: 1px;
    background: rgba(255, 255, 255, 0.15);
  }
  
  .divider span {
    position: relative;
    padding: 0 10px;
    background: rgba(10, 16, 28, 0.35);
    border-radius: 999px;
  }
  
  /* Responsive */
  @media (max-width: 980px) {
    .content {
      grid-template-columns: 1fr;
      gap: 24px;
      padding: 36px 18px;
    }
    .right {
      justify-items: stretch;
    }
  }
  </style>