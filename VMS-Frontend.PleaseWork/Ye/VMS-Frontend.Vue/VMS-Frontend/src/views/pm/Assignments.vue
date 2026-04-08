<template>
  <div class="assignments-page">

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
.page-header h2 { margin:0; font-size:20px; font-weight:800; color:#0f172a; }
.filters { display:flex; gap:12px; }
.filters select { background:#ffffff; border:1px solid #cbd5e1; border-radius:8px; padding:9px 14px; color:#1e293b; font-size:13px; outline:none; cursor:pointer; }

/* Read-only table */
.tasks-table { width:100%; border-collapse:collapse; font-size:13px; background:#ffffff; border:1px solid #e2e8f0; border-radius:12px; overflow:hidden; box-shadow:0 1px 3px rgba(0,0,0,0.06); }
.tasks-table th { text-align:left; padding:12px 16px; color:#64748b; font-weight:600; border-bottom:1px solid #e2e8f0; font-size:11px; text-transform:uppercase; letter-spacing:0.06em; background:#f8fafc; }
.tasks-table td { padding:13px 16px; color:#1e293b; border-bottom:1px solid #f1f5f9; }
.tasks-table tr:last-child td { border-bottom:none; }
.tasks-table tr:hover td { background:#f8fafc; }
.task-name-cell { font-weight:600; color:#0f172a; }
.project-cell { color:#475569; font-size:12px; }
.date-cell { color:#94a3b8; font-size:12px; }

.badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; }
.badge.green { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }
.badge.amber { background:#fffbeb; color:#d97706; border:1px solid #fde68a; }
.badge.red { background:#fef2f2; color:#dc2626; border:1px solid #fecaca; }
.badge.blue { background:#eff6ff; color:#2563eb; border:1px solid #bfdbfe; }
.badge.gray { background:#f1f5f9; color:#64748b; border:1px solid #e2e8f0; }

.status-badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:10px; font-weight:600; }
.status-badge.green { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }
.status-badge.amber { background:#fffbeb; color:#d97706; border:1px solid #fde68a; }
.status-badge.gray { background:#f1f5f9; color:#64748b; border:1px solid #e2e8f0; }

.loading-text, .empty-text { color:#94a3b8; font-size:14px; text-align:center; padding:40px; }
</style>
