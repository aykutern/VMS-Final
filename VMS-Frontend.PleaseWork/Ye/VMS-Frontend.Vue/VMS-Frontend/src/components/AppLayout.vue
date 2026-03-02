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
  "pm-dashboard": "Dashboard",
  "pm-projects": "Projects",
  "pm-project-detail": "Project Detail",
  "pm-sprints": "Sprints",
  "pm-assignments": "Assignments",
  "pm-vendors": "Vendors",
  "vendor-dashboard": "Dashboard",
  "vendor-projects": "My Projects",
  "vendor-sprints": "Sprints",
  "vendor-assignments": "My Tasks",
  "personnel-dashboard": "My Tasks",
};

const pageTitle = computed(() => titleMap[route.name] ?? "VMS");
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: #07101e;
  font-family: "Inter", ui-sans-serif, system-ui, -apple-system, Segoe UI, sans-serif;
  color: #e2eaff;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.page-content {
  flex: 1;
  padding: 28px;
  overflow-y: auto;
}
</style>
