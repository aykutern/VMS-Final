<template>
  <div class="pm-dashboard">
    <!-- Stats Row -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon blue">
          <svg viewBox="0 0 24 24"><path d="M20 6h-2.18c.07-.44.18-.86.18-1a3 3 0 00-6 0c0 .14.11.56.18 1H10c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-7-1a1 1 0 112 0c0 .14-.06.27-.09.41A.999.999 0 0113 5zm7 15H10V8h2v2h6V8h2v12z"/></svg>
        </div>
        <div>
          <div class="stat-value">{{ stats.totalProjects }}</div>
          <div class="stat-label">Total Projects</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <svg viewBox="0 0 24 24"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14H7v-2h5v2zm5-4H7v-2h10v2zm0-4H7V7h10v2z"/></svg>
        </div>
        <div>
          <div class="stat-value">{{ stats.activeSprints }}</div>
          <div class="stat-label">Active Sprints</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon amber">
          <svg viewBox="0 0 24 24"><path d="M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/></svg>
        </div>
        <div>
          <div class="stat-value">{{ stats.openTasks }}</div>
          <div class="stat-label">Open Tasks</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple">
          <svg viewBox="0 0 24 24"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>
        </div>
        <div>
          <div class="stat-value">{{ stats.vendors }}</div>
          <div class="stat-label">Active Vendors</div>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <!-- Recent Projects -->
      <div class="card">
        <div class="card-header">
          <h3>Recent Projects</h3>
          <router-link to="/pm/projects" class="link-btn">View All</router-link>
        </div>
        <div v-if="loadingData" class="loading-text">Loading…</div>
        <div v-else class="project-list">
          <div v-for="p in projects.slice(0, 4)" :key="p.id" class="project-item" @click="$router.push('/pm/projects/' + p.id)">
            <div class="project-name">{{ p.projectName }}</div>
            <div class="project-meta">
              <span class="badge blue">{{ p.vendorName }}</span>
            </div>
          </div>
          <div v-if="projects.length === 0" class="empty-text">No projects yet.</div>
        </div>
      </div>

      <!-- Recent Announcements -->
      <div class="card">
        <div class="card-header">
          <h3>Recent Announcements</h3>
        </div>
        <div v-if="loadingData" class="loading-text">Loading…</div>
        <div v-else class="announcement-list">
          <div v-for="a in announcements.slice(0, 4)" :key="a.id" class="announcement-item">
            <div class="ann-title">{{ a.title }}</div>
            <div class="ann-content">{{ a.content?.slice(0, 90) }}…</div>
          </div>
          <div v-if="announcements.length === 0" class="empty-text">No announcements yet.</div>
        </div>
      </div>

      <!-- Active Sprints -->
      <div class="card full-width">
        <div class="card-header">
          <h3>Active Sprints</h3>
          <router-link to="/pm/sprints" class="link-btn">Manage Sprints</router-link>
        </div>
        <div v-if="loadingData" class="loading-text">Loading…</div>
        <div v-else class="sprint-table-wrap">
          <table class="sprint-table" v-if="activeSprints.length > 0">
            <thead>
              <tr>
                <th>Sprint</th>
                <th>Project</th>
                <th>Goal</th>
                <th>End Date</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in activeSprints" :key="s.id">
                <td>{{ s.sprintName }}</td>
                <td>{{ s.projectName }}</td>
                <td>{{ s.goal?.slice(0, 60) }}…</td>
                <td>{{ s.endDate }}</td>
                <td><span class="badge green">{{ s.status }}</span></td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-text">No active sprints.</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { http } from "../lib/http.js";

const projects = ref([]);
const announcements = ref([]);
const sprints = ref([]);
const loadingData = ref(true);

const activeSprints = computed(() => sprints.value.filter(s => s.status === "ACTIVE"));

const stats = computed(() => ({
  totalProjects: projects.value.length,
  activeSprints: activeSprints.value.length,
  openTasks: 0, // will be enriched
  vendors: [...new Set(projects.value.map(p => p.vendorId))].filter(Boolean).length,
}));

onMounted(async () => {
  try {
    const [pRes, sRes, aRes] = await Promise.all([
      http.get("/api/projects"),
      http.get("/api/sprints"),
      http.get("/api/announcements"),
    ]);
    projects.value = pRes.data;
    sprints.value = sRes.data;
    announcements.value = aRes.data;
  } catch (e) {
    console.error(e);
  } finally {
    loadingData.value = false;
  }
});
</script>

<style scoped>
.pm-dashboard { display: flex; flex-direction: column; gap: 24px; }

.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }

.stat-card {
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 44px; height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}
.stat-icon svg { width: 22px; height: 22px; fill: #fff; }
.stat-icon.blue { background: rgba(59,130,246,0.2); }
.stat-icon.green { background: rgba(34,197,94,0.2); }
.stat-icon.amber { background: rgba(251,191,36,0.2); }
.stat-icon.purple { background: rgba(168,85,247,0.2); }

.stat-value { font-size: 26px; font-weight: 800; color: #f3f7ff; }
.stat-label { font-size: 12px; color: rgba(200,215,255,0.55); margin-top: 2px; }

.content-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }

.card {
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 16px;
  padding: 20px;
}
.card.full-width { grid-column: 1 / -1; }

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.card-header h3 { margin: 0; font-size: 15px; font-weight: 700; color: #e2eaff; }

.link-btn {
  font-size: 12px;
  color: #93c5fd;
  text-decoration: none;
  font-weight: 600;
}
.link-btn:hover { color: #bfdbfe; }

.project-item {
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 6px;
}
.project-item:hover { background: rgba(255,255,255,0.05); }
.project-name { font-size: 14px; font-weight: 600; color: #e2eaff; }
.project-meta { margin-top: 4px; }

.announcement-item { padding: 10px 0; border-bottom: 1px solid rgba(255,255,255,0.06); }
.announcement-item:last-child { border-bottom: none; }
.ann-title { font-size: 14px; font-weight: 600; color: #e2eaff; }
.ann-content { font-size: 12px; color: rgba(200,215,255,0.55); margin-top: 4px; line-height: 1.5; }

.sprint-table-wrap { overflow-x: auto; }
.sprint-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.sprint-table th { text-align: left; padding: 10px 12px; color: rgba(200,215,255,0.5); font-weight: 600; border-bottom: 1px solid rgba(255,255,255,0.07); }
.sprint-table td { padding: 10px 12px; color: #d1deff; border-bottom: 1px solid rgba(255,255,255,0.06); }
.sprint-table tr:last-child td { border-bottom: none; }
.sprint-table tr:hover td { background: rgba(255,255,255,0.03); }

.badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 11px; font-weight: 600; }
.badge.blue { background: rgba(59,130,246,0.15); color: #93c5fd; }
.badge.green { background: rgba(34,197,94,0.15); color: #86efac; }

.loading-text { color: rgba(200,215,255,0.4); font-size: 14px; text-align: center; padding: 20px; }
.empty-text { color: rgba(200,215,255,0.4); font-size: 13px; text-align: center; padding: 16px; }
</style>
