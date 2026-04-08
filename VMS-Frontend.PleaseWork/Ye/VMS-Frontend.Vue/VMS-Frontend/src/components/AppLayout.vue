<template>
  <div class="app-layout">
    <Sidebar />
    <div class="main-area">
      <TopBar :title="pageTitle" />
      <div class="page-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import Sidebar from "./Sidebar.vue";
import TopBar from "./TopBar.vue";
import { computed } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();

const titleMap = {
  // PM
  "pm-dashboard":        "Dashboard",
  "pm-projects":         "Projects",
  "pm-project-detail":   "Project Detail",
  "pm-sprints":          "Sprints",
  "pm-assignments":      "Assignments",
  "pm-vendors":          "Vendors",
  "pm-announcements":    "Announcements",
  // Vendor Admin
  "vendor-dashboard":    "Dashboard",
  "vendor-projects":     "My Projects",
  "vendor-sprints":      "Sprints",
  "vendor-assignments":  "Assignments",
  "vendor-personnel":    "Team Personnel",
  "vendor-performance":  "Employee Performance",
  "vendor-announcements":"Announcements",
  // Personnel
  "personnel-dashboard": "Dashboard",
  "personnel-tasks":     "My Tasks",
  "personnel-announcements": "Announcements",
};

const pageTitle = computed(() => titleMap[route.name] ?? "VMS");
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-page);
  color: var(--text-primary);
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.page-content {
  flex: 1;
  padding: 32px 40px;
  overflow-y: auto;
}

/* Responsive adjustment for page content */
@media (max-width: 768px) {
  .page-content {
    padding: 20px;
  }
}
</style>
