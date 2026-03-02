<template>
  <header class="topbar">
    <div class="topbar-title">{{ title }}</div>
    <div class="topbar-right">
      <div class="user-pill">
        <div class="avatar-sm">{{ initials }}</div>
        <span>{{ user?.username }}</span>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from "vue";
import { getCurrentUser } from "../services/authService.js";

defineProps({
  title: { type: String, default: "Dashboard" },
});

const user = getCurrentUser();
const initials = computed(() => {
  const n = user?.personnelName?.[0] ?? "";
  const s = user?.personnelSurname?.[0] ?? "";
  return (n + s).toUpperCase() || "U";
});
</script>

<style scoped>
.topbar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  background: rgba(8, 14, 28, 0.75);
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
  backdrop-filter: blur(16px);
  position: sticky;
  top: 0;
  z-index: 10;
}

.topbar-title {
  font-size: 16px;
  font-weight: 700;
  color: #e2eaff;
  letter-spacing: 0.01em;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-pill {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 999px;
  padding: 5px 14px 5px 6px;
  font-size: 13px;
  color: rgba(200, 215, 255, 0.85);
  font-weight: 500;
}

.avatar-sm {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #3b82f6);
  display: grid;
  place-items: center;
  font-size: 10px;
  font-weight: 700;
  color: #fff;
}
</style>
