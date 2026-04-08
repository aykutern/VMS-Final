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
.page-header h2 { margin:0; font-size:20px; font-weight:800; color:#0f172a; }
.filters { display:flex; gap:12px; }
.filters select { background:#ffffff; border:1px solid #cbd5e1; border-radius:8px; padding:9px 14px; color:#1e293b; font-size:13px; outline:none; cursor:pointer; }
.sprint-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(300px,1fr)); gap:16px; }
.sprint-card { background:#ffffff; border:1px solid #e2e8f0; border-radius:12px; padding:20px; display:flex; flex-direction:column; gap:10px; cursor:pointer; transition:border-color 0.2s,box-shadow 0.2s; position:relative; box-shadow:0 1px 3px rgba(0,0,0,0.04); }
.sprint-card:hover { border-color:#a78bfa; box-shadow:0 4px 12px rgba(99,102,241,0.1); }
.sprint-top { display:flex; align-items:flex-start; justify-content:space-between; gap:8px; }
.sprint-name { font-size:15px; font-weight:700; color:#0f172a; }
.sprint-project { font-size:12px; color:#2563eb; font-weight:600; }
.sprint-goal { font-size:13px; color:#475569; line-height:1.6; }
.sprint-dates { display:flex; gap:8px; font-size:12px; color:#94a3b8; font-weight:500; }
.dbl-hint { position:absolute; bottom:8px; right:12px; font-size:10px; color:#cbd5e1; }
.badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; white-space:nowrap; }
.badge.green { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }
.badge.amber { background:#fffbeb; color:#d97706; border:1px solid #fde68a; }
.badge.gray { background:#f1f5f9; color:#64748b; border:1px solid #e2e8f0; }

/* Detail Modal */
.detail-modal { background:#ffffff; border:1px solid #e2e8f0; border-radius:16px; padding:28px; width:min(720px,95vw); display:flex; flex-direction:column; gap:20px; max-height:85vh; overflow-y:auto; position:relative; box-shadow:0 20px 40px rgba(0,0,0,0.15); }
.close-btn { position:absolute; top:16px; right:16px; background:transparent; border:none; color:#94a3b8; font-size:20px; cursor:pointer; line-height:1; z-index:1; }
.close-btn:hover { color:#475569; }
.detail-header { padding-right:36px; }
.detail-title-block { flex:1; min-width:0; }
.detail-meta { display:flex; align-items:center; gap:10px; margin-top:6px; }
.detail-header h3 { margin:0; font-size:18px; font-weight:800; color:#0f172a; }
.detail-meta p { margin:0; font-size:12px; color:#64748b; }
.detail-goal { font-size:13px; color:#475569; line-height:1.6; padding:12px; background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; }
.detail-section { border-top:1px solid #f1f5f9; padding-top:16px; }
.detail-section-title { font-size:12px; font-weight:700; color:#64748b; text-transform:uppercase; letter-spacing:0.08em; margin-bottom:12px; }

/* Tasks table inside detail */
.tasks-table { width:100%; border-collapse:collapse; font-size:13px; }
.tasks-table th { text-align:left; padding:9px 12px; color:#64748b; font-weight:600; border-bottom:1px solid #e2e8f0; white-space:nowrap; }
.tasks-table td { padding:9px 12px; color:#1e293b; border-bottom:1px solid #f1f5f9; }
.tasks-table tr:last-child td { border-bottom:none; }
.tasks-table tr:hover td { background:#f8fafc; }
.status-badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:10px; font-weight:600; }
.status-badge.green { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }
.status-badge.amber { background:#fffbeb; color:#d97706; border:1px solid #fde68a; }
.status-badge.gray { background:#f1f5f9; color:#64748b; border:1px solid #e2e8f0; }

.primary-btn { padding:10px 20px; background:#2563eb; color:#fff; border:none; border-radius:8px; font-weight:700; font-size:14px; cursor:pointer; transition:background 0.15s; }
.primary-btn:hover { background:#1d4ed8; }
.primary-btn:disabled { opacity:0.5; cursor:not-allowed; }
.secondary-btn { padding:9px 16px; background:#f1f5f9; color:#475569; border:1px solid #e2e8f0; border-radius:8px; font-weight:600; font-size:14px; cursor:pointer; }
.modal-backdrop { position:fixed; inset:0; background:rgba(15,23,42,0.5); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; z-index:100; }
.modal { background:#ffffff; border:1px solid #e2e8f0; border-radius:16px; padding:28px; width:min(500px,90vw); display:flex; flex-direction:column; gap:16px; box-shadow:0 20px 40px rgba(0,0,0,0.15); }
.modal-header { display:flex; align-items:center; justify-content:space-between; }
.modal-header h3 { margin:0; font-size:17px; font-weight:800; color:#0f172a; }
.form-group { display:flex; flex-direction:column; gap:8px; }
.form-group label { font-size:13px; color:#475569; font-weight:600; }
.form-group input,.form-group select,.form-group textarea { background:#f8fafc; border:1px solid #cbd5e1; border-radius:8px; padding:10px 14px; color:#0f172a; font-size:14px; outline:none; resize:vertical; }
.form-group input:focus,.form-group select:focus,.form-group textarea:focus { border-color:#2563eb; box-shadow:0 0 0 3px rgba(37,99,235,0.1); }
.modal-actions { display:flex; gap:10px; justify-content:flex-end; }
.form-error { color:#dc2626; font-size:13px; }
.loading-text,.empty-state,.empty-text { color:#94a3b8; font-size:14px; text-align:center; padding:40px; }
.warn-text { font-size:12px; color:#d97706; margin-top:4px; display:block; }
</style>
