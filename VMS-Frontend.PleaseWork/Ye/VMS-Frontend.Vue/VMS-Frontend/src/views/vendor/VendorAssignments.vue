<template>
  <div class="vendor-assignments">
    <div class="page-header">
      <div class="filters">
        <select v-model="filterProject" @change="fetchAssignments">
          <option value="">All Projects</option>
          <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.projectName }}</option>
        </select>
      </div>
    </div>

    <div v-if="loading" class="loading-text">Loading…</div>
    <div v-else class="kanban">
      <div
        v-for="col in columns"
        :key="col.status"
        class="kanban-col"
        @dragover.prevent
        @dragenter.prevent="dragTarget = col.status"
        @drop="onDrop(col.status)"
        :class="{ 'drag-over': dragTarget === col.status }"
      >
        <div class="col-header">
          <span :class="['col-dot', col.color]"></span>
          <span class="col-title">{{ col.label }}</span>
          <span class="col-count">{{ tasksByStatus(col.status).length }}</span>
        </div>
        <div class="task-stack">
          <div
            v-for="t in tasksByStatus(col.status)"
            :key="t.id"
            class="task-card"
            draggable="true"
            @dragstart="draggingTask = t"
            @dragend="dragTarget = null"
          >
            <div class="drag-handle">⠿</div>
            <div class="task-name">{{ t.name }}</div>
            <div class="task-meta">
              <span :class="['badge', priorityColor(t.priority)]">{{ t.priority }}</span>
              <span :class="['rank-badge', 'rank-' + t.rank]">★ {{ t.rank }}</span>
              <span class="task-project">{{ t.projectName }}</span>
            </div>
          </div>
          <div v-if="tasksByStatus(col.status).length === 0" class="col-empty">Drop tasks here</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { http } from "../../lib/http.js";
import { getCurrentUser } from "../../services/authService.js";

const user = getCurrentUser();
const assignments = ref([]);
const projects = ref([]);
const loading = ref(true);
const filterProject = ref("");
const draggingTask = ref(null);
const dragTarget = ref(null);

const columns = [
  { status: "TODO", label: "To Do", color: "gray" },
  { status: "IN_PROGRESS", label: "In Progress", color: "amber" },
  { status: "COMPLETED", label: "Completed", color: "green" },
];

function tasksByStatus(status) { return assignments.value.filter(a => a.status === status); }
function priorityColor(p) { return { HIGH: "red", MEDIUM: "amber", LOW: "blue", OPTIONAL: "gray" }[p] ?? "gray"; }

onMounted(async () => {
  try {
    const vendorId = user?.vendorId;
    const [aRes, pRes] = await Promise.all([
      http.get(vendorId ? `/api/assignments?vendorId=${vendorId}` : "/api/assignments"),
      http.get(vendorId ? `/api/projects?vendorId=${vendorId}` : "/api/projects"),
    ]);
    assignments.value = aRes.data;
    projects.value = pRes.data;
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});

async function fetchAssignments() {
  const vendorId = user?.vendorId;
  let url;
  if (filterProject.value) {
    url = `/api/assignments?projectId=${filterProject.value}`;
  } else {
    url = vendorId ? `/api/assignments?vendorId=${vendorId}` : "/api/assignments";
  }
  const r = await http.get(url);
  assignments.value = r.data;
}

async function onDrop(newStatus) {
  dragTarget.value = null;
  if (!draggingTask.value || draggingTask.value.status === newStatus) return;
  const task = draggingTask.value;
  const oldStatus = task.status;
  task.status = newStatus;
  try {
    await http.patch(`/api/assignments/${task.id}/status`, { status: newStatus });
  } catch (e) {
    task.status = oldStatus;
    console.error(e);
  }
  draggingTask.value = null;
}
</script>

<style scoped>
.vendor-assignments { display:flex; flex-direction:column; gap:24px; }
.page-header { display:flex; align-items:center; justify-content:space-between; }
.filters select { background:#ffffff; border:1px solid #cbd5e1; border-radius:8px; padding:9px 14px; color:#1e293b; font-size:13px; outline:none; cursor:pointer; }
.kanban { display:grid; grid-template-columns:repeat(3,1fr); gap:16px; }
.kanban-col { background:#f8fafc; border:2px solid #e2e8f0; border-radius:12px; padding:16px; min-height:300px; display:flex; flex-direction:column; gap:10px; transition:border-color 0.15s; }
.kanban-col.drag-over { border-color:#6366f1; background:#f5f3ff; }
.col-header { display:flex; align-items:center; gap:8px; }
.col-dot { width:10px; height:10px; border-radius:50%; flex-shrink:0; }
.col-dot.gray { background:#64748b; }
.col-dot.amber { background:#f59e0b; }
.col-dot.green { background:#22c55e; }
.col-title { font-size:14px; font-weight:700; color:#0f172a; flex:1; }
.col-count { font-size:12px; background:#e2e8f0; border-radius:999px; padding:2px 8px; color:#64748b; font-weight:600; }
.task-stack { display:flex; flex-direction:column; gap:10px; }
.task-card { background:#ffffff; border:1px solid #e2e8f0; border-radius:10px; padding:14px; display:flex; flex-direction:column; gap:8px; cursor:grab; user-select:none; transition:border-color 0.15s,box-shadow 0.15s; box-shadow:0 1px 3px rgba(0,0,0,0.04); }
.task-card:active { cursor:grabbing; transform:rotate(1deg) scale(1.02); }
.task-card:hover { border-color:#a78bfa; box-shadow:0 4px 12px rgba(99,102,241,0.08); }
.drag-handle { font-size:14px; color:#cbd5e1; margin-bottom:-4px; }
.task-name { font-size:13px; font-weight:600; color:#0f172a; line-height:1.4; }
.task-meta { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.task-project { font-size:11px; color:#94a3b8; }
.col-empty { font-size:12px; color:#94a3b8; text-align:center; padding:16px; border:2px dashed #e2e8f0; border-radius:10px; }
.badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; }
.badge.green { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }
.badge.amber { background:#fffbeb; color:#d97706; border:1px solid #fde68a; }
.badge.red { background:#fef2f2; color:#dc2626; border:1px solid #fecaca; }
.badge.blue { background:#eff6ff; color:#2563eb; border:1px solid #bfdbfe; }
.badge.gray { background:#f1f5f9; color:#64748b; border:1px solid #e2e8f0; }
.rank-badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; background:#f5f3ff; color:#7c3aed; border:1px solid #ddd6fe; }
.rank-badge.rank-1 { background:#f0fdf4; color:#16a34a; border-color:#bbf7d0; }
.rank-badge.rank-2 { background:#eff6ff; color:#2563eb; border-color:#bfdbfe; }
.rank-badge.rank-3 { background:#fffbeb; color:#d97706; border-color:#fde68a; }
.rank-badge.rank-4 { background:#fff7ed; color:#ea580c; border-color:#fed7aa; }
.rank-badge.rank-5 { background:#fef2f2; color:#dc2626; border-color:#fecaca; }
.loading-text { color:#94a3b8; font-size:14px; text-align:center; padding:40px; }
</style>
