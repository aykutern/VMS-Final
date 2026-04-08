<template>
  <div class="login-page">
    <div class="content">
      <!-- Left branding panel -->
      <div class="left">
        <div class="brand">
          <div class="logo">VMS</div>
          <div class="title">Vendor Management System</div>
          <div class="subtitle">
            Centralize your vendor operations: manage onboarding, track performance, and accelerate delivery.
          </div>
        </div>
        <ul class="bullets">
          <li>Seamless vendor onboarding &amp; profiling</li>
          <li>Automated contract &amp; SLA management</li>
          <li>Real-time project &amp; task tracking</li>
          <li>Secure role-based access &amp; detailed audit trails</li>
        </ul>
      </div>

      <!-- Right card — flips between Sign In and Register -->
      <div class="right">
        <div class="card-wrapper" :class="{ flipped: showRegister }">

          <!-- ── Sign In face ── -->
          <form class="card face-front" @submit.prevent="onSubmit">
            <div class="card-header">
              <h2>Sign in</h2>
              <p>Use your account to access the VMS portal.</p>
            </div>

            <div class="field">
              <label for="login-username">Username</label>
              <input
                id="login-username"
                v-model.trim="loginForm.username"
                type="text"
                autocomplete="username"
                placeholder="Enter your username"
                required
              />
            </div>

            <div class="field">
              <label for="login-password">Password</label>
              <input
                id="login-password"
                v-model="loginForm.password"
                type="password"
                autocomplete="current-password"
                placeholder="••••••••"
                required
              />
            </div>

            <button class="btn-primary" type="submit" :disabled="loginLoading">
              {{ loginLoading ? "Signing in…" : "Sign in" }}
            </button>

            <div v-if="loginError" class="alert alert-error">{{ loginError }}</div>

            <div class="divider"><span>or</span></div>

            <button class="btn-secondary" type="button" @click="showRegister = true">
              Create an account
            </button>
          </form>

          <!-- ── Register face ── -->
          <form class="card face-back" @submit.prevent="onRegister">
            <div class="card-header">
              <h2>Create account</h2>
              <p>Fill in your details to get started.</p>
            </div>

            <div class="field-row">
              <div class="field">
                <label for="reg-firstname">First name</label>
                <input
                  id="reg-firstname"
                  v-model.trim="regForm.personnelName"
                  type="text"
                  placeholder="Jane"
                  required
                />
              </div>
              <div class="field">
                <label for="reg-lastname">Last name</label>
                <input
                  id="reg-lastname"
                  v-model.trim="regForm.personnelSurname"
                  type="text"
                  placeholder="Doe"
                  required
                />
              </div>
            </div>

            <div class="field">
              <label for="reg-username">Username</label>
              <input
                id="reg-username"
                v-model.trim="regForm.username"
                type="text"
                autocomplete="username"
                placeholder="jane_doe"
                required
              />
            </div>

            <div class="field">
              <label for="reg-email">Email</label>
              <input
                id="reg-email"
                v-model.trim="regForm.email"
                type="email"
                autocomplete="email"
                placeholder="jane@company.com"
                required
              />
            </div>

            <div class="field">
              <label for="reg-password">Password</label>
              <input
                id="reg-password"
                v-model="regForm.password"
                type="password"
                autocomplete="new-password"
                placeholder="Min. 8 characters"
                required
                minlength="8"
              />
            </div>

            <div class="field">
              <label for="reg-role">Role</label>
              <select id="reg-role" v-model="regForm.userType" required>
                <option value="" disabled>Select your role…</option>
                <option value="PERSONNEL">Developer (Personnel)</option>
                <option value="VENDOR_ADMIN">Vendor Admin</option>
                <option value="MANAGER">Product Manager</option>
              </select>
            </div>

            <button class="btn-primary" type="submit" :disabled="regLoading">
              {{ regLoading ? "Creating account…" : "Create account" }}
            </button>

            <div v-if="regError" class="alert alert-error">{{ regError }}</div>
            <div v-if="regSuccess" class="alert alert-success">{{ regSuccess }}</div>

            <div class="divider"><span>or</span></div>

            <button class="btn-secondary" type="button" @click="showRegister = false">
              Back to Sign in
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { login, register } from "../services/authService.js";

const router  = useRouter();
const showRegister = ref(false);

// ── Login state ──────────────────────────────────────────────
const loginLoading = ref(false);
const loginError   = ref(null);
const loginForm = reactive({ username: "", password: "" });

async function onSubmit() {
  loginError.value = null;
  if (!loginForm.username || !loginForm.password) {
    loginError.value = "Please enter your username and password.";
    return;
  }
  loginLoading.value = true;
  try {
    const user = await login({ username: loginForm.username, password: loginForm.password });
    if (user.userType === "MANAGER")      await router.push("/pm/dashboard");
    else if (user.userType === "VENDOR_ADMIN") await router.push("/vendor/dashboard");
    else                                  await router.push("/personnel");
  } catch (e) {
    loginError.value =
      e?.response?.data?.message ?? e?.message ?? "Login failed. Check your credentials.";
  } finally {
    loginLoading.value = false;
  }
}

// ── Register state ───────────────────────────────────────────
const regLoading = ref(false);
const regError   = ref(null);
const regSuccess = ref(null);
const regForm = reactive({
  username: "",
  email: "",
  password: "",
  personnelName: "",
  personnelSurname: "",
  userType: "",
});

async function onRegister() {
  regError.value   = null;
  regSuccess.value = null;
  regLoading.value = true;
  try {
    const user = await register({ ...regForm });
    regSuccess.value = `Account created! Welcome, ${user.personnelName ?? regForm.username}.`;
    // Auto-navigate after a short delay
    setTimeout(async () => {
      if (user.userType === "MANAGER")           await router.push("/pm/dashboard");
      else if (user.userType === "VENDOR_ADMIN") await router.push("/vendor/dashboard");
      else                                       await router.push("/personnel");
    }, 1000);
  } catch (e) {
    regError.value =
      e?.response?.data?.message ?? e?.message ?? "Registration failed. Please try again.";
  } finally {
    regLoading.value = false;
  }
}
</script>

<style scoped>
/* ── Page layout ─────────────────────────────────────────────── */
.login-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.content {
  width: 100%;
  max-width: 1040px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 64px;
  align-items: center;
}

@media (max-width: 900px) {
  .content {
    grid-template-columns: 1fr;
    gap: 40px;
    max-width: 480px;
  }
  .left { display: none; }
}

/* ── Left branding ───────────────────────────────────────────── */
.left .brand .logo {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: grid;
  place-items: center;
  font-weight: 800;
  font-size: 1.25rem;
  letter-spacing: 0.05em;
  background: var(--brand-primary);
  color: white;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

.title {
  margin-top: 24px;
  font-size: 2.5rem;
  line-height: 1.1;
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.subtitle {
  margin-top: 16px;
  font-size: 1.125rem;
  line-height: 1.6;
  color: var(--text-secondary);
}

.bullets {
  margin-top: 32px;
  padding-left: 20px;
  color: var(--text-secondary);
  font-size: 0.9375rem;
}
.bullets li { margin: 12px 0; }

/* ── Card flip ───────────────────────────────────────────────── */
.right {
  perspective: 1200px;
}

.card-wrapper {
  position: relative;
  transform-style: preserve-3d;
  transition: transform 0.55s cubic-bezier(0.45, 0, 0.15, 1);
}

.card-wrapper.flipped {
  transform: rotateY(180deg);
}

.card {
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  padding: 40px;
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-md);
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

.face-front {
  position: relative;
  z-index: 2;
}

.face-back {
  position: absolute;
  inset: 0;
  height: max-content;
  transform: rotateY(180deg);
}

/* ── Card internals ──────────────────────────────────────────── */
.card-header h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
}

.card-header p {
  margin: 8px 0 0;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.field {
  margin-top: 18px;
  flex: 1;
}

.field-row {
  display: flex;
  gap: 12px;
  margin-top: 0;
}

label {
  display: block;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

input[type="text"],
input[type="password"],
input[type="email"],
select {
  width: 100%;
  border-radius: var(--radius-md);
  padding: 10px 14px;
  border: 1px solid var(--border-strong);
  background: var(--bg-surface);
  color: var(--text-primary);
  font-size: 0.9375rem;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='%236b7280'%3E%3Cpath d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 36px;
  cursor: pointer;
}

input::placeholder { color: var(--text-tertiary); }

input:focus,
select:focus {
  outline: none;
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

/* ── Buttons ─────────────────────────────────────────────────── */
.btn-primary {
  margin-top: 22px;
  width: 100%;
  border: none;
  border-radius: var(--radius-md);
  padding: 12px;
  font-weight: 600;
  font-size: 0.9375rem;
  cursor: pointer;
  background: var(--brand-primary);
  color: white;
  transition: background-color 0.2s ease;
}
.btn-primary:hover:not(:disabled) { background: var(--brand-primary-hover); }
.btn-primary:disabled { opacity: 0.65; cursor: not-allowed; }

.btn-secondary {
  width: 100%;
  border-radius: var(--radius-md);
  padding: 12px;
  font-weight: 600;
  font-size: 0.9375rem;
  cursor: pointer;
  background: var(--bg-surface);
  border: 1px solid var(--border-strong);
  color: var(--text-primary);
  transition: background-color 0.2s ease;
}
.btn-secondary:hover { background: var(--bg-surface-hover); }

/* ── Alerts ──────────────────────────────────────────────────── */
.alert {
  margin-top: 14px;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
}

.alert-error {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: var(--brand-danger);
}

.alert-success {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #166534;
}

/* ── Divider ─────────────────────────────────────────────────── */
.divider {
  margin: 20px 0;
  display: flex;
  align-items: center;
  color: var(--text-tertiary);
  font-size: 0.875rem;
}
.divider::before, .divider::after {
  content: "";
  flex: 1;
  height: 1px;
  background: var(--border-subtle);
}
.divider span { padding: 0 16px; }
</style>