<template>
  <div class="vendor-sprints">
    <div class="page-header">
      <h2>Sprints</h2>
      <select v-model="filterProject" @change="fetchSprints">
        <option value="">All Projects</option>
        <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.projectName }}</option>
      </select>
    </div>

    <div v-if="loading" class="loading-text">Loading sprints…</div>
    <div v-else class="sprint-grid">
      <div v-for="s in sprints" :key="s.id" class="sprint-card">
        <div class="sprint-top">
          <div>
            <div class="sprint-name">{{ s.sprintName }}</div>
            <div class="sprint-project">{{ s.projectName }}</div>
          </div>
          <span :class="['badge', s.status === 'ACTIVE' ? 'green' : s.status === 'COMPLETED' ? 'gray' : 'amber']">{{ s.status }}</span>
        </div>
        <div class="sprint-goal">{{ s.goal }}</div>
        <div class="sprint-footer">
          <div class="sprint-dates">{{ s.startDate }} → {{ s.endDate }}</div>
        </div>
      </div>
      <div v-if="sprints.length === 0" class="empty-state">No sprints found.</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { http } from "../../lib/http.js";
import { getCurrentUser } from "../../services/authService.js";

const user = getCurrentUser();
const sprints = ref([]);
const projects = ref([]);
const loading = ref(true);
const filterProject = ref("");

onMounted(async () => {
  try {
    const vendorId = user?.vendorId;
    const [sRes, pRes] = await Promise.all([
      http.get("/api/sprints"),
      http.get(vendorId ? `/api/projects?vendorId=${vendorId}` : "/api/projects"),
    ]);
    sprints.value = sRes.data;
    projects.value = pRes.data;
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});

async function fetchSprints() {
  const url = filterProject.value ? `/api/sprints?projectId=${filterProject.value}` : "/api/sprints";
  const r = await http.get(url);
  sprints.value = r.data;
}
</script>

<style scoped>
.vendor-sprints { display:flex; flex-direction:column; gap:24px; }
.page-header { display:flex; align-items:center; justify-content:space-between; }
.page-header h2 { margin:0; font-size:20px; font-weight:800; color:#f3f7ff; }
.page-header select { background:rgba(255,255,255,0.06); border:1px solid rgba(255,255,255,0.12); border-radius:10px; padding:9px 14px; color:#e2eaff; font-size:13px; outline:none; cursor:pointer; }
.page-header select option { background:#0f1a2e; }
.sprint-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(300px,1fr)); gap:16px; }
.sprint-card { background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:16px; padding:20px; display:flex; flex-direction:column; gap:12px; }
.sprint-top { display:flex; align-items:flex-start; justify-content:space-between; gap:10px; }
.sprint-name { font-size:15px; font-weight:700; color:#e2eaff; }
.sprint-project { font-size:12px; color:#93c5fd; margin-top:3px; font-weight:600; }
.sprint-goal { font-size:13px; color:rgba(200,215,255,0.6); line-height:1.6; }
.sprint-footer { display:flex; align-items:center; justify-content:space-between; }
.sprint-dates { font-size:12px; color:rgba(200,215,255,0.4); font-weight:500; }
.badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; white-space:nowrap; }
.badge.green { background:rgba(34,197,94,0.15); color:#86efac; }
.badge.amber { background:rgba(251,191,36,0.15); color:#fde68a; }
.badge.gray { background:rgba(148,163,184,0.12); color:#94a3b8; }
.loading-text,.empty-state { color:rgba(200,215,255,0.4); font-size:14px; text-align:center; padding:40px; }
</style>
