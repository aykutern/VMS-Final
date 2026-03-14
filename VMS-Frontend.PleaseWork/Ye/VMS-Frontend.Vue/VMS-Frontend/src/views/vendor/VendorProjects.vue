<template>
  <div class="vendor-projects">
    <div v-if="loading" class="loading-text">Loading…</div>
    <div v-else class="project-grid">
      <div v-for="p in projects" :key="p.id" class="project-card" @click="selectedProject = p">
        <div class="card-top">
          <div class="project-avatar">{{ p.projectName?.[0] ?? 'P' }}</div>
          <div class="project-info">
            <div class="project-name">{{ p.projectName }}</div>
            <div class="project-pm">PM: {{ p.projectManagerName ?? '—' }}</div>
          </div>
        </div>
        <div class="project-footer">
          <span class="badge blue">{{ p.vendorName ?? 'Vendor' }}</span>
        </div>
      </div>
      <div v-if="projects.length === 0" class="empty-state">No projects assigned to your vendor yet.</div>
    </div>

    <!-- Project Detail Panel -->
    <div v-if="selectedProject" class="detail-panel">
      <div class="panel-header">
        <h3>{{ selectedProject.projectName }}</h3>
        <button class="close-btn" @click="selectedProject = null">✕</button>
      </div>
      <div class="panel-body">
        <div class="detail-row"><span>Vendor</span><strong>{{ selectedProject.vendorName }}</strong></div>
        <div class="detail-row"><span>Manager</span><strong>{{ selectedProject.projectManagerName ?? '—' }}</strong></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { http } from "../../lib/http.js";
import { getCurrentUser } from "../../services/authService.js";

const user = getCurrentUser();
const projects = ref([]);
const loading = ref(true);
const selectedProject = ref(null);

onMounted(async () => {
  try {
    const vendorId = user?.vendorId;
    const url = vendorId ? `/api/projects?vendorId=${vendorId}` : "/api/projects";
    const r = await http.get(url);
    projects.value = r.data;
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});
</script>

<style scoped>
.vendor-projects { display:flex; flex-direction:column; gap:24px; }
.page-header { display:flex; align-items:center; justify-content:space-between; }
.page-header h2 { margin:0; font-size:20px; font-weight:800; color:#f3f7ff; }
.project-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(280px,1fr)); gap:16px; }
.project-card { background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:16px; padding:20px; cursor:pointer; transition:border-color 0.2s,background 0.2s; display:flex; flex-direction:column; gap:12px; }
.project-card:hover { border-color:rgba(99,102,241,0.4); background:rgba(255,255,255,0.06); }
.card-top { display:flex; align-items:center; gap:14px; min-width:0; }
.project-avatar { width:48px; height:48px; border-radius:14px; background:linear-gradient(135deg,#3b82f6,#6366f1); display:grid; place-items:center; font-size:20px; font-weight:800; color:#fff; flex-shrink:0; }
.project-info { min-width:0; flex:1; }
.project-name { font-size:15px; font-weight:700; color:#e2eaff; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.project-pm { font-size:12px; color:rgba(200,215,255,0.5); margin-top:3px; }
.project-footer { display:flex; }
.badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; }
.badge.blue { background:rgba(59,130,246,0.15); color:#93c5fd; }
.detail-panel { background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.1); border-radius:16px; padding:24px; }
.panel-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.panel-header h3 { margin:0; font-size:16px; font-weight:800; color:#f3f7ff; }
.close-btn { background:transparent; border:none; color:rgba(200,215,255,0.5); font-size:18px; cursor:pointer; }
.detail-row { display:flex; justify-content:space-between; padding:10px 0; border-bottom:1px solid rgba(255,255,255,0.06); font-size:14px; color:rgba(200,215,255,0.6); }
.detail-row:last-child { border-bottom:none; }
.detail-row strong { color:#e2eaff; font-weight:600; }
.loading-text,.empty-state { color:rgba(200,215,255,0.4); font-size:14px; text-align:center; padding:40px; }
</style>
