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

      <!-- Pie Chart Card -->
      <div class="section-box chart-card">
        <div class="chart-container">
          <Pie v-if="chartReady" :data="chartData" :options="chartOptions" />
          <div v-else class="loading-text">Loading chart…</div>
        </div>
        <div class="chart-legend">
          <div class="legend-title">My Task Distribution</div>
          <div class="legend-items">
            <div class="legend-item"><span class="legend-dot" style="background:#64748b"></span> Todo <strong>{{ dashStats.todoCount }}</strong></div>
            <div class="legend-item"><span class="legend-dot" style="background:#f59e0b"></span> In Progress <strong>{{ dashStats.inProgressCount }}</strong></div>
            <div class="legend-item"><span class="legend-dot" style="background:#22c55e"></span> Completed <strong>{{ dashStats.completedCount }}</strong></div>
          </div>
          <div class="sprint-countdown" v-if="dashStats.businessDaysLeft >= 0">
            <div class="countdown-number">{{ dashStats.businessDaysLeft }}</div>
            <div class="countdown-label">business days left</div>
          </div>
        </div>
      </div>

      <!-- Two-column grid: sprints + projects + announcements -->
      <div class="main-grid">

        <!-- Active Sprints -->
        <div class="section-box" v-if="activeSprints.length > 0">
          <div class="section-title"><span class="s-icon">🏃</span> Active Sprint{{ activeSprints.length > 1 ? 's' : '' }}</div>
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
          <div class="section-title"><span class="s-icon">📁</span> My Projects</div>
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
          <div class="section-title"><span class="s-icon">📢</span> Announcements</div>
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
import { Pie } from "vue-chartjs";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";

ChartJS.register(ArcElement, Tooltip, Legend);

const user = getCurrentUser();
const myTasks = ref([]);
const sprints = ref([]);
const projects = ref([]);
const announcements = ref([]);
const loading = ref(true);
const chartReady = ref(false);
const dashStats = ref({ todoCount: 0, inProgressCount: 0, completedCount: 0, businessDaysLeft: -1 });

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

const chartData = computed(() => ({
  labels: ["Todo", "In Progress", "Completed"],
  datasets: [{
    data: [dashStats.value.todoCount, dashStats.value.inProgressCount, dashStats.value.completedCount],
    backgroundColor: ["#64748b", "#f59e0b", "#22c55e"],
    borderColor: ["#475569", "#d97706", "#16a34a"],
    borderWidth: 2,
    hoverOffset: 8,
  }],
}));

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: "#0f1a2e",
      titleColor: "#e2eaff",
      bodyColor: "#d1deff",
      borderColor: "rgba(255,255,255,0.1)",
      borderWidth: 1,
      cornerRadius: 10,
      padding: 12,
    },
  },
};

onMounted(async () => {
  try {
    const vendorId = user?.vendorId;
    const userId = user?.id;
    const pRes = await http.get(vendorId ? `/api/projects?vendorId=${vendorId}` : "/api/projects");
    projects.value = pRes.data;
    const projectIds = pRes.data.map(p => p.id);

    const [aRes, sRes, annRes, dRes] = await Promise.all([
      http.get("/api/assignments"),
      http.get("/api/sprints"),
      http.get("/api/announcements"),
      http.get(userId ? `/api/dashboard/stats?userId=${userId}` : "/api/dashboard/stats"),
    ]);
    myTasks.value = aRes.data.filter(a => projectIds.includes(a.projectId));
    sprints.value = sRes.data.filter(s => projectIds.includes(s.projectId));
    announcements.value = annRes.data.filter(a => projectIds.includes(a.projectId));
    dashStats.value = dRes.data;
    chartReady.value = true;
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});
</script>

<style scoped>
.personnel-dashboard { display:flex; flex-direction:column; gap:24px; }

.welcome-hero { display:flex; align-items:center; gap:16px; background:linear-gradient(135deg,#eff6ff,#f5f3ff); border:1px solid #ddd6fe; border-radius:14px; padding:20px 24px; }
.avatar { width:56px; height:56px; border-radius:14px; background:linear-gradient(135deg,#6366f1,#a855f7); display:grid; place-items:center; font-size:20px; font-weight:800; color:#fff; flex-shrink:0; }
.welcome-text h2 { margin:0; font-size:20px; font-weight:800; color:#0f172a; }
.welcome-text p { margin:4px 0 0; font-size:13px; color:#64748b; }
.date-chip { margin-left:auto; font-size:12px; color:#64748b; background:#ffffff; padding:6px 14px; border-radius:999px; white-space:nowrap; border:1px solid #e2e8f0; }

.summary-row { display:grid; grid-template-columns:repeat(4,1fr); gap:12px; }
.summary-card { background:#ffffff; border:1px solid #e2e8f0; border-radius:12px; padding:16px 20px; box-shadow:0 1px 3px rgba(0,0,0,0.04); }
.summary-card.blue { border-top:3px solid #3b82f6; }
.summary-card.amber { border-top:3px solid #f59e0b; }
.summary-card.green { border-top:3px solid #22c55e; }
.summary-card.purple { border-top:3px solid #a855f7; }
.s-num { font-size:28px; font-weight:800; color:#0f172a; }
.s-label { font-size:12px; color:#64748b; margin-top:2px; }

/* Pie Chart Card */
.chart-card { display:flex; align-items:center; gap:32px; padding:24px !important; }
.chart-container { width:180px; height:180px; flex-shrink:0; }
.chart-legend { flex:1; display:flex; flex-direction:column; gap:14px; }
.legend-title { font-size:15px; font-weight:700; color:#0f172a; }
.legend-items { display:flex; flex-direction:column; gap:8px; }
.legend-item { display:flex; align-items:center; gap:8px; font-size:13px; color:#475569; }
.legend-item strong { margin-left:auto; color:#0f172a; font-size:16px; }
.legend-dot { width:10px; height:10px; border-radius:50%; flex-shrink:0; }
.sprint-countdown { margin-top:8px; display:flex; align-items:baseline; gap:8px; padding:12px 16px; background:#eff6ff; border:1px solid #bfdbfe; border-radius:10px; }
.countdown-number { font-size:28px; font-weight:800; color:#2563eb; }
.countdown-label { font-size:13px; color:#64748b; }

.main-grid { display:flex; flex-direction:column; gap:16px; }
.section-box { background:#ffffff; border:1px solid #e2e8f0; border-radius:12px; padding:18px; box-shadow:0 1px 3px rgba(0,0,0,0.04); }
.section-title { font-size:13px; font-weight:700; color:#475569; margin-bottom:14px; display:flex; align-items:center; gap:6px; text-transform:uppercase; letter-spacing:0.05em; }
.s-icon { display:inline-flex; align-items:center; line-height:1; font-size:15px; }

.sprint-chip { background:#f8fafc; border:1px solid #e2e8f0; border-radius:10px; padding:14px; margin-bottom:10px; }
.sprint-name-row { display:flex; align-items:center; justify-content:space-between; gap:8px; margin-bottom:6px; }
.sprint-name { font-size:14px; font-weight:700; color:#0f172a; }
.sprint-project { font-size:11px; color:#2563eb; background:#eff6ff; padding:2px 8px; border-radius:999px; border:1px solid #bfdbfe; font-weight:600; }
.sprint-goal { font-size:12px; color:#475569; line-height:1.5; margin-bottom:6px; }
.sprint-dates { font-size:11px; color:#94a3b8; margin-bottom:10px; }
.sprint-progress { display:flex; align-items:center; gap:10px; }
.progress-bar { flex:1; height:5px; background:#e2e8f0; border-radius:999px; overflow:hidden; }
.progress-fill { height:100%; background:linear-gradient(90deg,#6366f1,#a855f7); border-radius:999px; transition:width 0.3s; }
.progress-label { font-size:10px; color:#94a3b8; white-space:nowrap; }

.project-chip { display:flex; align-items:center; gap:12px; padding:10px; background:#f8fafc; border:1px solid #f1f5f9; border-radius:10px; margin-bottom:8px; min-width:0; }
.proj-avatar { width:34px; height:34px; border-radius:8px; background:linear-gradient(135deg,#3b82f6,#6366f1); display:grid; place-items:center; font-size:14px; font-weight:800; color:#fff; flex-shrink:0; }
.proj-name { font-size:13px; font-weight:600; color:#0f172a; }
.proj-vendor { font-size:11px; color:#64748b; margin-top:2px; }

.ann-chip { background:#f8fafc; border:1px solid #f1f5f9; border-radius:10px; padding:12px; margin-bottom:8px; }
.ann-title { font-size:13px; font-weight:700; color:#0f172a; margin-bottom:5px; }
.ann-body { font-size:12px; color:#475569; line-height:1.5; }

.empty-note { font-size:13px; color:#94a3b8; padding:8px 0; }
.loading-text { color:#94a3b8; font-size:14px; text-align:center; padding:60px; }
</style>
