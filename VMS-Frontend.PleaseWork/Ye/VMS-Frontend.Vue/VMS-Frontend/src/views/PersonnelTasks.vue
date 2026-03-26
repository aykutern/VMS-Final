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
.page-header h2 { margin:0; font-size:20px; font-weight:800; color:#f3f7ff; }
.task-pills { display:flex; gap:8px; }
.pill { padding:4px 14px; border-radius:999px; font-size:12px; font-weight:600; }
.pill.gray { background:rgba(100,116,139,0.15); color:#94a3b8; }
.pill.amber { background:rgba(251,191,36,0.15); color:#fde68a; }
.pill.green { background:rgba(34,197,94,0.15); color:#86efac; }

.kanban { display:grid; grid-template-columns:repeat(3,1fr); gap:16px; }
.kanban-col { background:rgba(255,255,255,0.03); border:2px solid rgba(255,255,255,0.06); border-radius:16px; padding:16px; min-height:400px; display:flex; flex-direction:column; gap:10px; transition:border-color 0.15s; }
.kanban-col.drag-over { border-color:rgba(99,102,241,0.5); background:rgba(99,102,241,0.05); }
.col-header { display:flex; align-items:center; gap:8px; }
.col-dot { width:10px; height:10px; border-radius:50%; flex-shrink:0; }
.col-dot.gray { background:#64748b; }
.col-dot.amber { background:#f59e0b; }
.col-dot.purple { background:#a855f7; }
.col-dot.green { background:#22c55e; }
.col-title { font-size:14px; font-weight:700; color:#e2eaff; flex:1; }
.col-count { font-size:12px; background:rgba(255,255,255,0.1); border-radius:999px; padding:2px 8px; color:#94a3b8; }
.task-stack { display:flex; flex-direction:column; gap:10px; }
.task-card { background:rgba(255,255,255,0.05); border:1px solid rgba(255,255,255,0.09); border-radius:12px; padding:14px; display:flex; flex-direction:column; gap:8px; cursor:grab; user-select:none; transition:border-color 0.15s,transform 0.1s; }
.task-card:active { cursor:grabbing; transform:rotate(1deg) scale(1.02); }
.task-card:hover { border-color:rgba(99,102,241,0.35); }
.drag-handle { font-size:14px; color:rgba(200,215,255,0.25); margin-bottom:-4px; }
.task-name { font-size:13px; font-weight:600; color:#e2eaff; line-height:1.4; }
.task-meta { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.task-project { font-size:11px; color:rgba(200,215,255,0.5); }
.task-date { font-size:11px; color:rgba(200,215,255,0.35); }
.col-empty { font-size:12px; color:rgba(200,215,255,0.25); text-align:center; padding:20px; border:2px dashed rgba(255,255,255,0.08); border-radius:10px; }
.badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; }
.badge.green { background:rgba(34,197,94,0.15); color:#86efac; }
.badge.amber { background:rgba(251,191,36,0.15); color:#fde68a; }
.badge.red { background:rgba(239,68,68,0.15); color:#fca5a5; }
.badge.blue { background:rgba(59,130,246,0.15); color:#93c5fd; }
.badge.gray { background:rgba(148,163,184,0.12); color:#94a3b8; }
.rank-badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; background:rgba(139,92,246,0.15); color:#c4b5fd; }
.rank-badge.rank-1 { background:rgba(34,197,94,0.12); color:#86efac; }
.rank-badge.rank-2 { background:rgba(59,130,246,0.12); color:#93c5fd; }
.rank-badge.rank-3 { background:rgba(251,191,36,0.12); color:#fde68a; }
.rank-badge.rank-4 { background:rgba(249,115,22,0.12); color:#fdba74; }
.rank-badge.rank-5 { background:rgba(239,68,68,0.12); color:#fca5a5; }
.task-rejection { font-size:11px; color:#fca5a5; background:rgba(239,68,68,0.08); padding:6px 8px; border-radius:6px; border:1px dashed rgba(239,68,68,0.25); line-height:1.4; }
.loading-text { color:rgba(200,215,255,0.4); font-size:14px; text-align:center; padding:60px; }
</style>
