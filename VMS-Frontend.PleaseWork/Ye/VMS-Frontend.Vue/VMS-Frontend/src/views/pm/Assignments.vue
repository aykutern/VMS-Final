<template>
  <div class="assignments-page">
    <div class="page-header">
      <h2>Assignments</h2>
    </div>

    <div class="filters">
      <select v-model="filterProject" @change="fetchAssignments">
        <option value="">All Projects</option>
        <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.projectName }}</option>
      </select>
      <select v-model="filterStatus">
        <option value="">All Statuses</option>
        <option>TODO</option><option>IN_PROGRESS</option><option>COMPLETED</option>
      </select>
    </div>

    <div v-if="loading" class="loading-text">Loading…</div>
    <template v-else>
      <div v-if="filtered.length === 0" class="empty-text">No assignments found.</div>
      <table v-else class="tasks-table">
        <thead>
          <tr>
            <th>Task</th>
            <th>Project</th>
            <th>Priority</th>
            <th>Status</th>
            <th>Assigned</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in filtered" :key="t.id">
            <td class="task-name-cell">{{ t.name }}</td>
            <td class="project-cell">{{ t.projectName || '—' }}</td>
            <td><span :class="['badge', priorityColor(t.priority)]">{{ t.priority }}</span></td>
            <td>
              <span :class="['status-badge', t.status === 'COMPLETED' ? 'green' : t.status === 'IN_PROGRESS' ? 'amber' : 'gray']">
                {{ t.status?.replace('_', ' ') }}
              </span>
            </td>
            <td class="date-cell">{{ t.assignedAt || '—' }}</td>
          </tr>
        </tbody>
      </table>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { http } from "../../lib/http.js";

const assignments = ref([]);
const projects = ref([]);
const loading = ref(true);
const filterProject = ref("");
const filterStatus = ref("");

const filtered = computed(() => assignments.value.filter(a => {
  if (filterProject.value && a.projectId != filterProject.value) return false;
  if (filterStatus.value && a.status !== filterStatus.value) return false;
  return true;
}));

onMounted(async () => {
  try {
    const [aRes, pRes] = await Promise.all([http.get("/api/assignments"), http.get("/api/projects")]);
    assignments.value = aRes.data;
    projects.value = pRes.data;
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});

async function fetchAssignments() {
  const url = filterProject.value ? `/api/assignments?projectId=${filterProject.value}` : "/api/assignments";
  const r = await http.get(url);
  assignments.value = r.data;
}

function priorityColor(p) { return { HIGH: "red", MEDIUM: "amber", LOW: "blue", OPTIONAL: "gray" }[p] ?? "gray"; }
</script>

<style scoped>
.assignments-page { display:flex; flex-direction:column; gap:24px; }
.page-header { display:flex; align-items:center; justify-content:space-between; }
.page-header h2 { margin:0; font-size:20px; font-weight:800; color:#f3f7ff; }
.filters { display:flex; gap:12px; }
.filters select { background:rgba(255,255,255,0.06); border:1px solid rgba(255,255,255,0.12); border-radius:10px; padding:9px 14px; color:#e2eaff; font-size:13px; outline:none; cursor:pointer; }
.filters select option { background:#0f1a2e; }

/* Read-only table */
.tasks-table { width:100%; border-collapse:collapse; font-size:13px; background:rgba(255,255,255,0.02); border:1px solid rgba(255,255,255,0.07); border-radius:16px; overflow:hidden; }
.tasks-table th { text-align:left; padding:12px 16px; color:rgba(200,215,255,0.5); font-weight:600; border-bottom:1px solid rgba(255,255,255,0.07); font-size:11px; text-transform:uppercase; letter-spacing:0.06em; background:rgba(255,255,255,0.02); }
.tasks-table td { padding:13px 16px; color:#d1deff; border-bottom:1px solid rgba(255,255,255,0.04); }
.tasks-table tr:last-child td { border-bottom:none; }
.tasks-table tr:hover td { background:rgba(255,255,255,0.03); }
.task-name-cell { font-weight:600; color:#e2eaff; }
.project-cell { color:rgba(200,215,255,0.6); font-size:12px; }
.date-cell { color:rgba(200,215,255,0.4); font-size:12px; }

.badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; }
.badge.green { background:rgba(34,197,94,0.15); color:#86efac; }
.badge.amber { background:rgba(251,191,36,0.15); color:#fde68a; }
.badge.red { background:rgba(239,68,68,0.15); color:#fca5a5; }
.badge.blue { background:rgba(59,130,246,0.15); color:#93c5fd; }
.badge.gray { background:rgba(148,163,184,0.12); color:#94a3b8; }

.status-badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:10px; font-weight:600; }
.status-badge.green { background:rgba(34,197,94,0.15); color:#86efac; }
.status-badge.amber { background:rgba(251,191,36,0.15); color:#fde68a; }
.status-badge.gray { background:rgba(148,163,184,0.12); color:#94a3b8; }

.loading-text, .empty-text { color:rgba(200,215,255,0.4); font-size:14px; text-align:center; padding:40px; }
</style>
