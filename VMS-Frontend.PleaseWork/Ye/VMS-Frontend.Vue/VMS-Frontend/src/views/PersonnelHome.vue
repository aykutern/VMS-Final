<template>
  <div class="personnel-dashboard">

    <!-- Welcome hero -->
    <div class="welcome-hero">
      <div class="avatar">{{ initials }}</div>
      <div class="welcome-text">
        <h2>Welcome back, {{ user?.personnelName || user?.username }}!</h2>
        <p>Here's everything that's happening on your projects today.</p>
      </div>
      <div class="date-chip">{{ today }}</div>
    </div>

    <div v-if="loading" class="loading-text">Loading your dashboard…</div>
    <template v-else>

      <!-- Summary pills row -->
      <div class="summary-row">
        <div class="summary-card blue">
          <div class="s-num">{{ myTasks.length }}</div>
          <div class="s-label">My Tasks</div>
        </div>
        <div class="summary-card amber">
          <div class="s-num">{{ myTasks.filter(t => t.status === 'IN_PROGRESS').length }}</div>
          <div class="s-label">In Progress</div>
        </div>
        <div class="summary-card green">
          <div class="s-num">{{ myTasks.filter(t => t.status === 'COMPLETED').length }}</div>
          <div class="s-label">Completed</div>
        </div>
        <div class="summary-card purple">
          <div class="s-num">{{ activeSprints.length }}</div>
          <div class="s-label">Active Sprint{{ activeSprints.length !== 1 ? 's' : '' }}</div>
        </div>
      </div>

      <!-- Two-column grid: sprints + projects + announcements -->
      <div class="main-grid">

        <!-- Active Sprints -->
        <div class="section-box" v-if="activeSprints.length > 0">
          <div class="section-title">🏃 Active Sprint{{ activeSprints.length > 1 ? 's' : '' }}</div>
          <div v-for="s in activeSprints" :key="s.id" class="sprint-chip">
            <div class="sprint-name-row">
              <span class="sprint-name">{{ s.sprintName }}</span>
              <span class="sprint-project">{{ s.projectName }}</span>
            </div>
            <div class="sprint-goal">{{ s.goal }}</div>
            <div class="sprint-dates">{{ s.startDate }} → {{ s.endDate }}</div>
            <div class="sprint-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: sprintProgress(s) + '%' }"></div>
              </div>
              <span class="progress-label">{{ sprintProgress(s) }}% elapsed</span>
            </div>
          </div>
        </div>

        <!-- My Projects -->
        <div class="section-box">
          <div class="section-title">📁 My Projects</div>
          <div v-if="projects.length === 0" class="empty-note">No projects assigned yet.</div>
          <div v-for="p in projects" :key="p.id" class="project-chip">
            <div class="proj-avatar">{{ p.projectName?.[0] }}</div>
            <div>
              <div class="proj-name">{{ p.projectName }}</div>
              <div class="proj-vendor">{{ p.vendorName }}</div>
            </div>
          </div>
        </div>

        <!-- Announcements -->
        <div class="section-box">
          <div class="section-title">📢 Announcements</div>
          <div v-if="announcements.length === 0" class="empty-note">No announcements yet.</div>
          <div v-for="a in announcements" :key="a.id" class="ann-chip">
            <div class="ann-title">{{ a.title }}</div>
            <div class="ann-body">{{ a.content }}</div>
          </div>
        </div>

      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { http } from "../lib/http.js";
import { getCurrentUser } from "../services/authService.js";

const user = getCurrentUser();
const myTasks = ref([]);
const sprints = ref([]);
const projects = ref([]);
const announcements = ref([]);
const loading = ref(true);

const today = new Date().toLocaleDateString("en-GB", { weekday: "long", day: "numeric", month: "long", year: "numeric" });

const initials = computed(() => {
  const n = user?.personnelName ?? user?.username ?? "U";
  const s = user?.personnelSurname ?? "";
  return (n[0] + (s[0] ?? "")).toUpperCase();
});

const activeSprints = computed(() => sprints.value.filter(s => s.status === "ACTIVE"));

function sprintProgress(s) {
  if (!s.startDate || !s.endDate) return 0;
  const start = new Date(s.startDate).getTime();
  const end = new Date(s.endDate).getTime();
  const now = Date.now();
  if (now <= start) return 0;
  if (now >= end) return 100;
  return Math.round(((now - start) / (end - start)) * 100);
}

onMounted(async () => {
  try {
    const vendorId = user?.vendorId;
    const pRes = await http.get(vendorId ? `/api/projects?vendorId=${vendorId}` : "/api/projects");
    projects.value = pRes.data;
    const projectIds = pRes.data.map(p => p.id);

    const [aRes, sRes, annRes] = await Promise.all([
      http.get("/api/assignments"),
      http.get("/api/sprints"),
      http.get("/api/announcements"),
    ]);
    myTasks.value = aRes.data.filter(a => projectIds.includes(a.projectId));
    sprints.value = sRes.data.filter(s => projectIds.includes(s.projectId));
    announcements.value = annRes.data.filter(a => projectIds.includes(a.projectId));
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});
</script>

<style scoped>
.personnel-dashboard { display:flex; flex-direction:column; gap:24px; }

.welcome-hero { display:flex; align-items:center; gap:16px; background:rgba(99,102,241,0.08); border:1px solid rgba(99,102,241,0.2); border-radius:18px; padding:20px 24px; }
.avatar { width:56px; height:56px; border-radius:16px; background:linear-gradient(135deg,#6366f1,#a855f7); display:grid; place-items:center; font-size:20px; font-weight:800; color:#fff; flex-shrink:0; }
.welcome-text h2 { margin:0; font-size:20px; font-weight:800; color:#f3f7ff; }
.welcome-text p { margin:4px 0 0; font-size:13px; color:rgba(200,215,255,0.6); }
.date-chip { margin-left:auto; font-size:12px; color:rgba(200,215,255,0.5); background:rgba(255,255,255,0.06); padding:6px 14px; border-radius:999px; white-space:nowrap; }

.summary-row { display:grid; grid-template-columns:repeat(4,1fr); gap:12px; }
.summary-card { background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:14px; padding:16px 20px; }
.summary-card.blue { border-color:rgba(59,130,246,0.2); }
.summary-card.amber { border-color:rgba(251,191,36,0.2); }
.summary-card.green { border-color:rgba(34,197,94,0.2); }
.summary-card.purple { border-color:rgba(168,85,247,0.2); }
.s-num { font-size:28px; font-weight:800; color:#f3f7ff; }
.s-label { font-size:12px; color:rgba(200,215,255,0.5); margin-top:2px; }

.main-grid { display:flex; flex-direction:column; gap:16px; }
.section-box { background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.08); border-radius:16px; padding:18px; }
.section-title { font-size:13px; font-weight:700; color:rgba(200,215,255,0.6); margin-bottom:14px; }

.sprint-chip { background:rgba(99,102,241,0.06); border:1px solid rgba(99,102,241,0.2); border-radius:12px; padding:14px; margin-bottom:10px; }
.sprint-name-row { display:flex; align-items:center; justify-content:space-between; gap:8px; margin-bottom:6px; }
.sprint-name { font-size:14px; font-weight:700; color:#e2eaff; }
.sprint-project { font-size:11px; color:#93c5fd; background:rgba(59,130,246,0.1); padding:2px 8px; border-radius:999px; }
.sprint-goal { font-size:12px; color:rgba(200,215,255,0.55); line-height:1.5; margin-bottom:6px; }
.sprint-dates { font-size:11px; color:rgba(200,215,255,0.4); margin-bottom:10px; }
.sprint-progress { display:flex; align-items:center; gap:10px; }
.progress-bar { flex:1; height:5px; background:rgba(255,255,255,0.08); border-radius:999px; overflow:hidden; }
.progress-fill { height:100%; background:linear-gradient(90deg,#6366f1,#a855f7); border-radius:999px; transition:width 0.3s; }
.progress-label { font-size:10px; color:rgba(200,215,255,0.4); white-space:nowrap; }

.project-chip { display:flex; align-items:center; gap:12px; padding:10px; background:rgba(255,255,255,0.03); border-radius:10px; margin-bottom:8px; }
.proj-avatar { width:34px; height:34px; border-radius:10px; background:linear-gradient(135deg,#3b82f6,#6366f1); display:grid; place-items:center; font-size:14px; font-weight:800; color:#fff; flex-shrink:0; }
.proj-name { font-size:13px; font-weight:600; color:#e2eaff; }
.proj-vendor { font-size:11px; color:rgba(200,215,255,0.45); margin-top:2px; }

.ann-chip { background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.06); border-radius:10px; padding:12px; margin-bottom:8px; }
.ann-title { font-size:13px; font-weight:700; color:#e2eaff; margin-bottom:5px; }
.ann-body { font-size:12px; color:rgba(200,215,255,0.55); line-height:1.5; }

.empty-note { font-size:13px; color:rgba(200,215,255,0.3); padding:8px 0; }
.loading-text { color:rgba(200,215,255,0.4); font-size:14px; text-align:center; padding:60px; }
</style>
