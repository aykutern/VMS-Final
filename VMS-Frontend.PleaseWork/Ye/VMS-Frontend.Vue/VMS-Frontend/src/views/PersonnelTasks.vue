<template>
  <div class="my-tasks-page">
    <div class="page-header">
      <div class="task-pills">
        <span class="pill gray">{{ tasksByStatus('TODO').length }} To Do</span>
        <span class="pill amber">{{ tasksByStatus('IN_PROGRESS').length }} In Progress</span>
        <span class="pill purple">{{ tasksByStatus('IN_REVIEW').length }} In Review</span>
        <span class="pill green">{{ tasksByStatus('COMPLETED').length }} Done</span>
      </div>
    </div>

    <div v-if="loading" class="loading-text">Loading tasks…</div>
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
            <div v-if="t.rejectionReason && (col.status === 'TODO' || col.status === 'IN_PROGRESS')" class="task-rejection">
              🛑 <strong>Rejected:</strong> {{ t.rejectionReason }}
            </div>
            <div v-if="t.assignedAt" class="task-date">{{ t.assignedAt }}</div>
          </div>
          <div v-if="tasksByStatus(col.status).length === 0" class="col-empty">Drop tasks here</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { http } from "../lib/http.js";
import { getCurrentUser } from "../services/authService.js";

const user = getCurrentUser();
const assignments = ref([]);
const loading = ref(true);
const draggingTask = ref(null);
const dragTarget = ref(null);

const columns = [
  { status: "TODO", label: "To Do", color: "gray" },
  { status: "IN_PROGRESS", label: "In Progress", color: "amber" },
  { status: "IN_REVIEW", label: "In Review", color: "purple" },
  { status: "COMPLETED", label: "Completed", color: "green" },
];

function tasksByStatus(status) { return assignments.value.filter(t => t.status === status); }
function priorityColor(p) { return { HIGH: "red", MEDIUM: "amber", LOW: "blue", OPTIONAL: "gray" }[p] ?? "gray"; }

async function onDrop(newStatus) {
  dragTarget.value = null;
  if (!draggingTask.value || draggingTask.value.status === newStatus) return;
  if (newStatus === "COMPLETED") {
    alert("Only administrators can mark tasks as Completed. Please drag to 'In Review' instead.");
    draggingTask.value = null;
    return;
  }
  const task = draggingTask.value;
  const old = task.status;
  task.status = newStatus;
  try {
    await http.patch(`/api/assignments/${task.id}/status`, { status: newStatus });
  } catch (e) { task.status = old; console.error(e); }
  draggingTask.value = null;
}

onMounted(async () => {
  try {
    const userId = user?.id;
    if (userId) {
      const aRes = await http.get(`/api/assignments?assigneeId=${userId}`);
      assignments.value = aRes.data;
    }
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});
</script>

<style scoped>
.my-tasks-page { display:flex; flex-direction:column; gap:24px; }
.page-header { display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:12px; }
.task-pills { display:flex; gap:8px; flex-wrap:wrap; }
.pill { padding:4px 14px; border-radius:999px; font-size:12px; font-weight:600; }
.pill.gray { background:#f1f5f9; color:#64748b; border:1px solid #e2e8f0; }
.pill.amber { background:#fffbeb; color:#d97706; border:1px solid #fde68a; }
.pill.purple { background:#f5f3ff; color:#7c3aed; border:1px solid #ddd6fe; }
.pill.green { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }

.kanban { display:grid; grid-template-columns:repeat(4,1fr); gap:16px; }
.kanban-col { background:#f8fafc; border:2px solid #e2e8f0; border-radius:12px; padding:16px; min-height:400px; display:flex; flex-direction:column; gap:10px; transition:border-color 0.15s; }
.kanban-col.drag-over { border-color:#6366f1; background:#f5f3ff; }
.col-header { display:flex; align-items:center; gap:8px; }
.col-dot { width:10px; height:10px; border-radius:50%; flex-shrink:0; }
.col-dot.gray { background:#64748b; }
.col-dot.amber { background:#f59e0b; }
.col-dot.purple { background:#a855f7; }
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
.task-date { font-size:11px; color:#94a3b8; }
.col-empty { font-size:12px; color:#94a3b8; text-align:center; padding:20px; border:2px dashed #e2e8f0; border-radius:10px; }
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
.task-rejection { font-size:11px; color:#dc2626; background:#fef2f2; padding:6px 8px; border-radius:6px; border:1px dashed #fecaca; line-height:1.4; }
.loading-text { color:#94a3b8; font-size:14px; text-align:center; padding:60px; }
</style>
