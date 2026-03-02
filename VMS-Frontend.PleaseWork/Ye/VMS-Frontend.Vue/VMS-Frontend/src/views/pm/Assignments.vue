<template>
  <div class="assignments-page">
    <div class="page-header">
      <h2>Assignments</h2>
      <button class="primary-btn" @click="showModal = true">+ New Task</button>
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
              <span class="task-project">{{ t.projectName }}</span>
            </div>
            <div v-if="t.assignedAt" class="task-date">Assigned: {{ t.assignedAt }}</div>
          </div>
          <div v-if="tasksByStatus(col.status).length === 0" class="col-empty">Drop tasks here</div>
        </div>
      </div>
    </div>

    <!-- New Task Modal -->
    <div v-if="showModal" class="modal-backdrop" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header"><h3>New Task</h3><button class="close-btn" @click="showModal = false">✕</button></div>
        <div class="form-group">
          <label>Project</label>
          <select v-model="form.projectId">
            <option value="">— Select project —</option>
            <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.projectName }}</option>
          </select>
        </div>
        <div class="form-group"><label>Task Name</label><input v-model="form.name" placeholder="e.g. Implement product listing API" /></div>
        <div class="form-group">
          <label>Priority</label>
          <select v-model="form.priority">
            <option>HIGH</option><option selected>MEDIUM</option><option>LOW</option><option>OPTIONAL</option>
          </select>
        </div>
        <div class="form-group">
          <label>Sprint (optional)</label>
          <select v-model="form.sprintId">
            <option value="">— No sprint —</option>
            <option v-for="s in filteredSprints" :key="s.id" :value="s.id">{{ s.sprintName }}</option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="secondary-btn" @click="showModal = false">Cancel</button>
          <button class="primary-btn" @click="createTask" :disabled="saving">{{ saving ? 'Creating…' : 'Create Task' }}</button>
        </div>
        <div v-if="formError" class="form-error">{{ formError }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { http } from "../../lib/http.js";

const assignments = ref([]);
const projects = ref([]);
const sprints = ref([]);
const loading = ref(true);
const showModal = ref(false);
const saving = ref(false);
const formError = ref(null);
const filterProject = ref("");
const filterStatus = ref("");

const form = ref({ projectId: "", name: "", priority: "MEDIUM", sprintId: "" });

// Drag-and-drop state
const draggingTask = ref(null);
const dragTarget = ref(null);

const columns = [
  { status: "TODO", label: "To Do", color: "gray" },
  { status: "IN_PROGRESS", label: "In Progress", color: "amber" },
  { status: "COMPLETED", label: "Completed", color: "green" },
];

const filteredSprints = computed(() =>
  form.value.projectId ? sprints.value.filter(s => s.projectId == form.value.projectId) : sprints.value
);

const filtered = computed(() => assignments.value.filter(a => {
  if (filterProject.value && a.projectId != filterProject.value) return false;
  if (filterStatus.value && a.status !== filterStatus.value) return false;
  return true;
}));

function tasksByStatus(status) { return filtered.value.filter(a => a.status === status); }

onMounted(async () => {
  try {
    const [aRes, pRes, sRes] = await Promise.all([http.get("/api/assignments"), http.get("/api/projects"), http.get("/api/sprints")]);
    assignments.value = aRes.data;
    projects.value = pRes.data;
    sprints.value = sRes.data;
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});

async function fetchAssignments() {
  const url = filterProject.value ? `/api/assignments?projectId=${filterProject.value}` : "/api/assignments";
  const r = await http.get(url);
  assignments.value = r.data;
}

async function createTask() {
  if (!form.value.projectId || !form.value.name) { formError.value = "Project and task name are required."; return; }
  saving.value = true; formError.value = null;
  try {
    await http.post("/api/assignments", {
      name: form.value.name,
      priority: form.value.priority,
      projectId: Number(form.value.projectId),
      assignedAt: new Date().toISOString().split("T")[0],
    });
    await fetchAssignments();
    showModal.value = false;
    form.value = { projectId: "", name: "", priority: "MEDIUM", sprintId: "" };
  } catch (e) { formError.value = e?.response?.data?.message ?? "Failed to create task."; }
  finally { saving.value = false; }
}

function priorityColor(p) { return { HIGH: "red", MEDIUM: "amber", LOW: "blue", OPTIONAL: "gray" }[p] ?? "gray"; }

async function onDrop(newStatus) {
  dragTarget.value = null;
  if (!draggingTask.value || draggingTask.value.status === newStatus) return;
  const task = draggingTask.value;
  const oldStatus = task.status;
  task.status = newStatus; // optimistic
  try {
    await http.patch(`/api/assignments/${task.id}/status`, { status: newStatus });
  } catch (e) {
    task.status = oldStatus; // rollback
    console.error(e);
  }
  draggingTask.value = null;
}
</script>

<style scoped>
.assignments-page { display:flex; flex-direction:column; gap:24px; }
.page-header { display:flex; align-items:center; justify-content:space-between; }
.page-header h2 { margin:0; font-size:20px; font-weight:800; color:#f3f7ff; }
.filters { display:flex; gap:12px; }
.filters select { background:rgba(255,255,255,0.06); border:1px solid rgba(255,255,255,0.12); border-radius:10px; padding:9px 14px; color:#e2eaff; font-size:13px; outline:none; cursor:pointer; }
.filters select option { background:#0f1a2e; }
.kanban { display:grid; grid-template-columns:repeat(3,1fr); gap:16px; }
.kanban-col { background:rgba(255,255,255,0.03); border:2px solid rgba(255,255,255,0.06); border-radius:16px; padding:16px; display:flex; flex-direction:column; gap:10px; min-height:300px; transition:border-color 0.15s; }
.kanban-col.drag-over { border-color:rgba(99,102,241,0.5); background:rgba(99,102,241,0.05); }
.col-header { display:flex; align-items:center; gap:8px; margin-bottom:4px; }
.col-dot { width:10px; height:10px; border-radius:50%; flex-shrink:0; }
.col-dot.gray { background:#64748b; }
.col-dot.amber { background:#f59e0b; }
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
.col-empty { font-size:12px; color:rgba(200,215,255,0.3); text-align:center; padding:16px; }
.badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; }
.badge.green { background:rgba(34,197,94,0.15); color:#86efac; }
.badge.amber { background:rgba(251,191,36,0.15); color:#fde68a; }
.badge.red { background:rgba(239,68,68,0.15); color:#fca5a5; }
.badge.blue { background:rgba(59,130,246,0.15); color:#93c5fd; }
.badge.gray { background:rgba(148,163,184,0.12); color:#94a3b8; }
.primary-btn { padding:10px 20px; background:linear-gradient(135deg,#3b82f6,#6366f1); color:#fff; border:none; border-radius:10px; font-weight:700; font-size:14px; cursor:pointer; }
.primary-btn:disabled { opacity:0.5; cursor:not-allowed; }
.secondary-btn { padding:9px 16px; background:rgba(255,255,255,0.07); color:#d1deff; border:1px solid rgba(255,255,255,0.1); border-radius:10px; font-weight:600; font-size:14px; cursor:pointer; }
.close-btn { background:transparent; border:none; color:rgba(200,215,255,0.5); font-size:18px; cursor:pointer; }
.modal-backdrop { position:fixed; inset:0; background:rgba(0,0,0,0.6); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; z-index:100; }
.modal { background:#0f1a2e; border:1px solid rgba(255,255,255,0.12); border-radius:20px; padding:28px; width:min(480px,90vw); display:flex; flex-direction:column; gap:16px; }
.modal-header { display:flex; align-items:center; justify-content:space-between; }
.modal-header h3 { margin:0; font-size:17px; font-weight:800; color:#f3f7ff; }
.form-group { display:flex; flex-direction:column; gap:8px; }
.form-group label { font-size:13px; color:rgba(200,215,255,0.7); font-weight:600; }
.form-group input,.form-group select { background:rgba(255,255,255,0.06); border:1px solid rgba(255,255,255,0.12); border-radius:10px; padding:10px 14px; color:#e2eaff; font-size:14px; outline:none; }
.form-group select option { background:#0f1a2e; }
.modal-actions { display:flex; gap:10px; justify-content:flex-end; }
.form-error { color:#f87171; font-size:13px; }
.loading-text { color:rgba(200,215,255,0.4); font-size:14px; text-align:center; padding:40px; }
</style>
