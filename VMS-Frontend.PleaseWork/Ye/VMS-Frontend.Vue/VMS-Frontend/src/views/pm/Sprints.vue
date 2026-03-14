<template>
  <div class="sprints-page">

    <div class="filters">
      <select v-model="filterProject" @change="filterSprints">
        <option value="">All Projects</option>
        <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.projectName }}</option>
      </select>
      <select v-model="filterStatus" @change="filterSprints">
        <option value="">All Statuses</option>
        <option>PLANNED</option>
        <option>ACTIVE</option>
        <option>COMPLETED</option>
      </select>
    </div>

    <div v-if="loading" class="loading-text">Loading sprints…</div>
    <div v-else class="sprint-grid">
      <div
        v-for="s in filteredSprints"
        :key="s.id"
        class="sprint-card"
        @dblclick="openSprintDetail(s)"
        title="Double-click to view tasks"
      >
        <div class="sprint-top">
          <div class="sprint-name">{{ s.sprintName }}</div>
          <span :class="['badge', s.status === 'ACTIVE' ? 'green' : s.status === 'COMPLETED' ? 'gray' : 'amber']">{{ s.status }}</span>
        </div>
        <div class="sprint-project">{{ s.projectName }}</div>
        <div class="sprint-goal">{{ s.goal }}</div>
        <div class="sprint-dates">
          <span>{{ s.startDate }}</span>
          <span>→</span>
          <span>{{ s.endDate }}</span>
        </div>
        <div class="dbl-hint">Double-click for details</div>
      </div>
      <div v-if="filteredSprints.length === 0" class="empty-state">No sprints found.</div>
    </div>

    <!-- Sprint Detail Modal -->
    <div v-if="detailSprint" class="modal-backdrop" @click.self="detailSprint = null">
      <div class="detail-modal">
        <button class="close-btn" @click="detailSprint = null">✕</button>
        <div class="detail-header">
          <div class="detail-title-block">
            <h3>{{ detailSprint.sprintName }}</h3>
            <div class="detail-meta">
              <span :class="['badge', detailSprint.status === 'ACTIVE' ? 'green' : detailSprint.status === 'COMPLETED' ? 'gray' : 'amber']">
                {{ detailSprint.status }}
              </span>
              <p>{{ detailSprint.projectName }} · {{ detailSprint.startDate }} → {{ detailSprint.endDate }}</p>
            </div>
          </div>
        </div>
        <div class="detail-goal" v-if="detailSprint.goal">{{ detailSprint.goal }}</div>

        <div class="detail-section">
          <div class="detail-section-title">Tasks ({{ sprintTasks.length }})</div>
          <div v-if="loadingTasks" class="loading-text">Loading tasks…</div>
          <table v-else-if="sprintTasks.length > 0" class="tasks-table">
            <thead>
              <tr><th>Task</th><th>Assignee</th><th>Status</th><th>Priority</th><th>Completed</th></tr>
            </thead>
            <tbody>
              <tr v-for="t in sprintTasks" :key="t.id">
                <td>{{ t.name }}</td>
                <td>{{ t.assigneeName || '—' }}</td>
                <td>
                  <span :class="['status-badge', t.status === 'COMPLETED' ? 'green' : t.status === 'IN_PROGRESS' ? 'amber' : 'gray']">
                    {{ t.status?.replace('_', ' ') }}
                  </span>
                </td>
                <td>{{ t.priority }}</td>
                <td>{{ t.completedAt || '—' }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-text">No tasks in this sprint.</div>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { http } from "../../lib/http.js";

const sprints = ref([]);
const projects = ref([]);
const loading = ref(true);
const filterProject = ref("");
const filterStatus = ref("");

// Sprint detail state
const detailSprint = ref(null);
const sprintTasks = ref([]);
const loadingTasks = ref(false);



const filteredSprints = computed(() => sprints.value.filter(s => {
  if (filterProject.value && s.projectId !== filterProject.value) return false;
  if (filterStatus.value && s.status !== filterStatus.value) return false;
  return true;
}));

async function openSprintDetail(sprint) {
  detailSprint.value = sprint;
  sprintTasks.value = [];
  loadingTasks.value = true;
  try {
    const r = await http.get(`/api/assignments?sprintId=${sprint.id}`);
    sprintTasks.value = r.data;
  } catch (e) { console.error(e); }
  finally { loadingTasks.value = false; }
}

onMounted(async () => {
  try {
    const [sRes, pRes] = await Promise.all([http.get("/api/sprints"), http.get("/api/projects")]);
    sprints.value = sRes.data;
    projects.value = pRes.data;
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});

function filterSprints() {}
</script>

<style scoped>
.sprints-page { display:flex; flex-direction:column; gap:24px; }
.page-header { display:flex; align-items:center; justify-content:space-between; }
.page-header h2 { margin:0; font-size:20px; font-weight:800; color:#f3f7ff; }
.filters { display:flex; gap:12px; }
.filters select { background:rgba(255,255,255,0.06); border:1px solid rgba(255,255,255,0.12); border-radius:10px; padding:9px 14px; color:#e2eaff; font-size:13px; outline:none; cursor:pointer; }
.filters select option { background:#0f1a2e; }
.sprint-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(300px,1fr)); gap:16px; }
.sprint-card { background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:16px; padding:20px; display:flex; flex-direction:column; gap:10px; cursor:pointer; transition:border-color 0.2s,background 0.2s; position:relative; }
.sprint-card:hover { border-color:rgba(99,102,241,0.3); background:rgba(99,102,241,0.04); }
.sprint-top { display:flex; align-items:flex-start; justify-content:space-between; gap:8px; }
.sprint-name { font-size:15px; font-weight:700; color:#e2eaff; }
.sprint-project { font-size:12px; color:#93c5fd; font-weight:600; }
.sprint-goal { font-size:13px; color:rgba(200,215,255,0.6); line-height:1.6; }
.sprint-dates { display:flex; gap:8px; font-size:12px; color:rgba(200,215,255,0.4); font-weight:500; }
.dbl-hint { position:absolute; bottom:8px; right:12px; font-size:10px; color:rgba(200,215,255,0.2); }
.badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; white-space:nowrap; }
.badge.green { background:rgba(34,197,94,0.15); color:#86efac; }
.badge.amber { background:rgba(251,191,36,0.15); color:#fde68a; }
.badge.gray { background:rgba(148,163,184,0.12); color:#94a3b8; }

/* Detail Modal */
.detail-modal { background:#0f1a2e; border:1px solid rgba(255,255,255,0.12); border-radius:20px; padding:28px; width:min(720px,95vw); display:flex; flex-direction:column; gap:20px; max-height:85vh; overflow-y:auto; position:relative; }
.close-btn { position:absolute; top:16px; right:16px; background:transparent; border:none; color:rgba(200,215,255,0.5); font-size:20px; cursor:pointer; line-height:1; z-index:1; }
.close-btn:hover { color:#e2eaff; }
.detail-header { padding-right:36px; }
.detail-title-block { flex:1; min-width:0; }
.detail-meta { display:flex; align-items:center; gap:10px; margin-top:6px; }
.detail-header h3 { margin:0; font-size:18px; font-weight:800; color:#f3f7ff; }
.detail-meta p { margin:0; font-size:12px; color:rgba(200,215,255,0.5); }
.detail-goal { font-size:13px; color:rgba(200,215,255,0.6); line-height:1.6; padding:12px; background:rgba(255,255,255,0.03); border-radius:10px; }
.detail-section { border-top:1px solid rgba(255,255,255,0.07); padding-top:16px; }
.detail-section-title { font-size:12px; font-weight:700; color:rgba(200,215,255,0.5); text-transform:uppercase; letter-spacing:0.08em; margin-bottom:12px; }

/* Tasks table inside detail */
.tasks-table { width:100%; border-collapse:collapse; font-size:13px; }
.tasks-table th { text-align:left; padding:9px 12px; color:rgba(200,215,255,0.5); font-weight:600; border-bottom:1px solid rgba(255,255,255,0.07); white-space:nowrap; }
.tasks-table td { padding:9px 12px; color:#d1deff; border-bottom:1px solid rgba(255,255,255,0.04); }
.tasks-table tr:last-child td { border-bottom:none; }
.tasks-table tr:hover td { background:rgba(255,255,255,0.03); }
.status-badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:10px; font-weight:600; }
.status-badge.green { background:rgba(34,197,94,0.15); color:#86efac; }
.status-badge.amber { background:rgba(251,191,36,0.15); color:#fde68a; }
.status-badge.gray { background:rgba(148,163,184,0.12); color:#94a3b8; }

.primary-btn { padding:10px 20px; background:linear-gradient(135deg,#3b82f6,#6366f1); color:#fff; border:none; border-radius:10px; font-weight:700; font-size:14px; cursor:pointer; }
.primary-btn:disabled { opacity:0.5; cursor:not-allowed; }
.secondary-btn { padding:9px 16px; background:rgba(255,255,255,0.07); color:#d1deff; border:1px solid rgba(255,255,255,0.1); border-radius:10px; font-weight:600; font-size:14px; cursor:pointer; }
.close-btn { background:transparent; border:none; color:rgba(200,215,255,0.5); font-size:18px; cursor:pointer; }
.modal-backdrop { position:fixed; inset:0; background:rgba(0,0,0,0.6); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; z-index:100; }
.modal { background:#0f1a2e; border:1px solid rgba(255,255,255,0.12); border-radius:20px; padding:28px; width:min(500px,90vw); display:flex; flex-direction:column; gap:16px; }
.modal-header { display:flex; align-items:center; justify-content:space-between; }
.modal-header h3 { margin:0; font-size:17px; font-weight:800; color:#f3f7ff; }
.form-group { display:flex; flex-direction:column; gap:8px; }
.form-group label { font-size:13px; color:rgba(200,215,255,0.7); font-weight:600; }
.form-group input,.form-group select,.form-group textarea { background:rgba(255,255,255,0.06); border:1px solid rgba(255,255,255,0.12); border-radius:10px; padding:10px 14px; color:#e2eaff; font-size:14px; outline:none; resize:vertical; }
.form-group select option { background:#0f1a2e; }
.modal-actions { display:flex; gap:10px; justify-content:flex-end; }
.form-error { color:#f87171; font-size:13px; }
.loading-text,.empty-state,.empty-text { color:rgba(200,215,255,0.4); font-size:14px; text-align:center; padding:40px; }
.warn-text { font-size:12px; color:#fbbf24; margin-top:4px; display:block; }
</style>
