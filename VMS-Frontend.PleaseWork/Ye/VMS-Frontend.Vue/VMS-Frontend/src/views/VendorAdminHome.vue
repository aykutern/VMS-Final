<template>
  <div class="vendor-dashboard">
    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon blue">
          <svg viewBox="0 0 24 24"><path d="M20 6h-2.18c.07-.44.18-.86.18-1a3 3 0 00-6 0c0 .14.11.56.18 1H10c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-7-1a1 1 0 112 0c0 .14-.06.27-.09.41A.999.999 0 0113 5zm7 15H10V8h2v2h6V8h2v12z"/></svg>
        </div>
        <div>
          <div class="stat-value">{{ projects.length }}</div>
          <div class="stat-label">My Projects</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <svg viewBox="0 0 24 24"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14H7v-2h5v2zm5-4H7v-2h10v2zm0-4H7V7h10v2z"/></svg>
        </div>
        <div>
          <div class="stat-value">{{ activeSprintsCount }}</div>
          <div class="stat-label">Active Sprints</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon amber">
          <svg viewBox="0 0 24 24"><path d="M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/></svg>
        </div>
        <div>
          <div class="stat-value">{{ pendingTasks }}</div>
          <div class="stat-label">Pending Tasks</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple">
          <svg viewBox="0 0 24 24"><path d="M9 11H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2zm2-7h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z"/></svg>
        </div>
        <div>
          <div class="stat-value">{{ completedTasksCount }}</div>
          <div class="stat-label">Completed Tasks</div>
        </div>
      </div>
    </div>

    <!-- Pie Chart Card -->
    <div class="card chart-card">
      <div class="chart-container">
        <Pie v-if="chartReady" :data="chartData" :options="chartOptions" />
        <div v-else class="loading-text">Loading chart…</div>
      </div>
      <div class="chart-legend">
        <div class="legend-title">Task Distribution</div>
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

    <div class="content-grid">
      <!-- My Projects -->
      <div class="card">
        <div class="card-header">
          <h3>My Projects</h3>
          <router-link to="/vendor/projects" class="link-btn">View All</router-link>
        </div>
        <div v-if="loading" class="loading-text">Loading…</div>
        <div v-else class="project-list">
          <div v-for="p in projects" :key="p.id" class="project-item">
            <div class="project-dot"></div>
            <div>
              <div class="project-name">{{ p.projectName }}</div>
              <div class="project-pm">PM: {{ p.projectManagerName ?? '—' }}</div>
            </div>
          </div>
          <div v-if="projects.length === 0" class="empty-text">No projects assigned yet.</div>
        </div>
      </div>

      <!-- Active Sprint Tasks -->
      <div class="card">
        <div class="card-header">
          <h3>Sprint Overview</h3>
          <router-link to="/vendor/sprints" class="link-btn">View Sprints</router-link>
        </div>
        <div v-if="loading" class="loading-text">Loading…</div>
        <div v-else class="sprint-list">
          <div v-for="s in sprints.filter(s => s.status === 'ACTIVE')" :key="s.id" class="sprint-item">
            <div class="sprint-header">
              <span class="sprint-name">{{ s.sprintName }}</span>
              <span class="badge green">ACTIVE</span>
            </div>
            <div class="sprint-goal">{{ s.goal?.slice(0, 80) }}{{ s.goal?.length > 80 ? '…' : '' }}</div>
            <div class="sprint-dates">Ends: {{ s.endDate }}</div>
          </div>
          <div v-if="sprints.filter(s => s.status === 'ACTIVE').length === 0" class="empty-text">No active sprints.</div>
        </div>
      </div>

      <!-- Task Status Summary -->
      <div class="card full-width">
        <div class="card-header">
          <h3>Task Status Board</h3>
          <router-link to="/vendor/assignments" class="link-btn">Manage Tasks</router-link>
        </div>
        <div v-if="loading" class="loading-text">Loading…</div>
        <div v-else class="task-columns">
          <div v-for="col in columns" :key="col.status" class="task-col">
            <div class="col-header">
              <span :class="['col-dot', col.color]"></span>
              <span class="col-title">{{ col.label }}</span>
              <span class="col-count">{{ tasksByStatus(col.status).length }}</span>
            </div>
            <div v-for="t in tasksByStatus(col.status).slice(0, 3)" :key="t.id" class="mini-task">
              {{ t.name }}
            </div>
            <div v-if="tasksByStatus(col.status).length === 0" class="col-empty">No tasks</div>
          </div>
        </div>
      </div>
    </div>
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
const projects = ref([]);
const sprints = ref([]);
const assignments = ref([]);
const loading = ref(true);
const chartReady = ref(false);
const dashStats = ref({ todoCount: 0, inProgressCount: 0, completedCount: 0, businessDaysLeft: -1 });

const columns = [
  { status: "TODO", label: "To Do", color: "gray" },
  { status: "IN_PROGRESS", label: "In Progress", color: "amber" },
  { status: "COMPLETED", label: "Completed", color: "green" },
];

const activeSprintsCount = computed(() => sprints.value.filter(s => s.status === "ACTIVE").length);
const pendingTasks = computed(() => assignments.value.filter(a => a.status === "TODO" || a.status === "IN_PROGRESS").length);
const completedTasksCount = computed(() => assignments.value.filter(a => a.status === "COMPLETED").length);
function tasksByStatus(status) { return assignments.value.filter(a => a.status === status); }

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
    const [pRes, sRes, aRes, dRes] = await Promise.all([
      http.get(vendorId ? `/api/projects?vendorId=${vendorId}` : "/api/projects"),
      http.get(vendorId ? `/api/sprints?vendorId=${vendorId}` : "/api/sprints"),
      http.get(vendorId ? `/api/assignments?vendorId=${vendorId}` : "/api/assignments"),
      http.get(vendorId ? `/api/dashboard/stats?vendorId=${vendorId}` : "/api/dashboard/stats"),
    ]);
    projects.value = pRes.data;
    sprints.value = sRes.data;
    assignments.value = aRes.data;
    dashStats.value = dRes.data;
    chartReady.value = true;
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});
</script>

<style scoped>
.vendor-dashboard { display:flex; flex-direction:column; gap:24px; }
.stats-row { display:grid; grid-template-columns:repeat(4,1fr); gap:16px; }
.stat-card { background:#ffffff; border:1px solid #e2e8f0; border-radius:12px; padding:20px; display:flex; align-items:center; gap:16px; box-shadow:0 1px 3px rgba(0,0,0,0.04); }
.stat-icon { width:44px; height:44px; border-radius:10px; display:grid; place-items:center; flex-shrink:0; }
.stat-icon svg { width:22px; height:22px; }
.stat-icon.blue { background:#eff6ff; } .stat-icon.blue svg { fill:#2563eb; }
.stat-icon.green { background:#f0fdf4; } .stat-icon.green svg { fill:#16a34a; }
.stat-icon.amber { background:#fffbeb; } .stat-icon.amber svg { fill:#d97706; }
.stat-icon.purple { background:#f5f3ff; } .stat-icon.purple svg { fill:#7c3aed; }
.stat-value { font-size:26px; font-weight:800; color:#0f172a; }
.stat-label { font-size:12px; color:#64748b; margin-top:2px; }

/* Pie Chart Card */
.chart-card { display:flex; align-items:center; gap:32px; padding:24px; }
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

.content-grid { display:grid; grid-template-columns:1fr 1fr; gap:20px; }
.card { background:#ffffff; border:1px solid #e2e8f0; border-radius:12px; padding:20px; box-shadow:0 1px 3px rgba(0,0,0,0.04); }
.card.full-width { grid-column:1/-1; }
.card-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.card-header h3 { margin:0; font-size:15px; font-weight:700; color:#0f172a; }
.link-btn { font-size:12px; color:#2563eb; text-decoration:none; font-weight:600; }
.link-btn:hover { color:#1d4ed8; }
.project-item { display:flex; align-items:center; gap:10px; padding:10px 0; border-bottom:1px solid #f1f5f9; }
.project-item:last-child { border-bottom:none; }
.project-dot { width:8px; height:8px; border-radius:50%; background:#3b82f6; flex-shrink:0; }
.project-name { font-size:14px; font-weight:600; color:#0f172a; }
.project-pm { font-size:12px; color:#64748b; margin-top:2px; }
.sprint-item { padding:12px 0; border-bottom:1px solid #f1f5f9; }
.sprint-item:last-child { border-bottom:none; }
.sprint-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:6px; }
.sprint-name { font-size:14px; font-weight:600; color:#0f172a; }
.sprint-goal { font-size:12px; color:#475569; line-height:1.5; }
.sprint-dates { font-size:11px; color:#94a3b8; margin-top:4px; }
.task-columns { display:grid; grid-template-columns:repeat(3,1fr); gap:16px; }
.task-col { background:#f8fafc; border:1px solid #e2e8f0; border-radius:10px; padding:14px; display:flex; flex-direction:column; gap:8px; }
.col-header { display:flex; align-items:center; gap:7px; }
.col-dot { width:9px; height:9px; border-radius:50%; flex-shrink:0; }
.col-dot.gray { background:#64748b; }
.col-dot.amber { background:#f59e0b; }
.col-dot.green { background:#22c55e; }
.col-title { font-size:13px; font-weight:700; color:#0f172a; flex:1; }
.col-count { font-size:12px; background:#e2e8f0; border-radius:999px; padding:2px 8px; color:#64748b; font-weight:600; }
.mini-task { font-size:12px; color:#475569; padding:6px 8px; background:#ffffff; border:1px solid #e2e8f0; border-radius:6px; }
.col-empty { font-size:12px; color:#94a3b8; text-align:center; padding:10px; }
.badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; }
.badge.green { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }
.loading-text,.empty-text { color:#94a3b8; font-size:13px; text-align:center; padding:20px; }
</style>
