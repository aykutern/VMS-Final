<template>
  <div class="personnel-page">
    <div class="page-header">
      <span class="vendor-label">{{ vendorName }}</span>
    </div>

    <div v-if="loading" class="loading-text">Loading team…</div>
    <template v-else>
      <!-- Stats bar -->
      <div class="stats-bar">
        <div class="stat-card">
          <div class="stat-num">{{ personnel.length }}</div>
          <div class="stat-label">Team Members</div>
        </div>
        <div class="stat-card">
          <div class="stat-num">{{ assignments.length }}</div>
          <div class="stat-label">Total Tasks</div>
        </div>
        <div class="stat-card">
          <div class="stat-num">{{ assignments.filter(a => a.status === 'IN_PROGRESS').length }}</div>
          <div class="stat-label">In Progress</div>
        </div>
        <div class="stat-card">
          <div class="stat-num">{{ assignments.filter(a => a.status === 'COMPLETED').length }}</div>
          <div class="stat-label">Completed</div>
        </div>
      </div>

      <!-- Team member cards -->
      <div class="team-grid">
        <div v-for="p in personnel" :key="p.id" class="member-card">
          <div class="member-header">
            <div class="member-avatar">{{ initials(p) }}</div>
            <div class="member-info">
              <div class="member-name">{{ p.personnelName }} {{ p.personnelSurname }}</div>
              <div class="member-meta">
                <span class="member-username">@{{ p.username }}</span>
                <span :class="['role-chip', p.userType === 'VENDOR_ADMIN' ? 'purple' : 'blue']">
                  {{ p.userType === 'VENDOR_ADMIN' ? 'Admin' : 'Developer' }}
                </span>
              </div>
              <div class="member-email">{{ p.email }}</div>
            </div>
          </div>

          <!-- Task mini-kanban for this member -->
          <div class="member-tasks">
            <div class="tasks-title">
              Tasks <span class="task-total-badge">{{ memberTasks(p.id).length }}</span>
            </div>
            <div
              v-if="memberTasks(p.id).length > 0"
              class="task-mini-kanban"
            >
              <div v-for="col in columns" :key="col.status" class="mini-col"
                @dragover.prevent
                @dragenter.prevent="dragTarget = `${p.id}_${col.status}`"
                @drop="onDrop(p.id, col.status)"
                :class="{ 'drag-over': dragTarget === `${p.id}_${col.status}` }"
              >
                <div class="mini-col-header">
                  <span :class="['mini-dot', col.color]"></span>
                  <span>{{ col.label }}</span>
                  <span class="mini-count">{{ memberTasksByStatus(p.id, col.status).length }}</span>
                </div>
                <div
                  v-for="t in memberTasksByStatus(p.id, col.status)"
                  :key="t.id"
                  class="mini-task"
                  draggable="true"
                  @dragstart="startDrag(t, p.id)"
                  @dragend="dragTarget = null"
                >
                  <span :class="['priority-dot', priorityColor(t.priority)]"></span>
                  <span class="mini-task-name">{{ t.name }}</span>
                  <span class="mini-project">{{ t.projectName }}</span>
                </div>
              </div>
            </div>
            <div v-else class="no-tasks">No tasks assigned yet</div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { http } from "../../lib/http.js";
import { getCurrentUser } from "../../services/authService.js";

const user = getCurrentUser();
const personnel = ref([]);
const assignments = ref([]);
const projects = ref([]);
const loading = ref(true);
const vendorName = ref("");

// Drag state — keyed as `userId_status` string
const draggingTask = ref(null);
const draggingFromUser = ref(null);
const dragTarget = ref(null);

const columns = [
  { status: "TODO", label: "To Do", color: "gray" },
  { status: "IN_PROGRESS", label: "In Progress", color: "amber" },
  { status: "COMPLETED", label: "Completed", color: "green" },
];

function initials(p) {
  return ((p.personnelName?.[0] ?? '') + (p.personnelSurname?.[0] ?? '')).toUpperCase() || 'U';
}
function priorityColor(pr) { return { HIGH: "red", MEDIUM: "amber", LOW: "blue", OPTIONAL: "gray" }[pr] ?? "gray"; }

// Since Assignment doesn't have a userId FK yet, we distribute tasks across members by assignment order for demo
// In real use, you'd filter by assignedUserId once that field exists
function memberTasks(userId) {
  // Distribute assignments among personnel in round-robin for display
  const idx = personnel.value.findIndex(p => p.id === userId);
  if (idx < 0 || personnel.value.length === 0) return [];
  return assignments.value.filter((_, i) => i % personnel.value.length === idx);
}
function memberTasksByStatus(userId, status) {
  return memberTasks(userId).filter(t => t.status === status);
}

function startDrag(task, userId) {
  draggingTask.value = task;
  draggingFromUser.value = userId;
}

async function onDrop(userId, newStatus) {
  dragTarget.value = null;
  if (!draggingTask.value || draggingTask.value.status === newStatus) return;
  const task = draggingTask.value;
  const old = task.status;
  task.status = newStatus;
  try {
    await http.patch(`/api/assignments/${task.id}/status`, { status: newStatus });
  } catch (e) {
    task.status = old;
    console.error(e);
  }
  draggingTask.value = null;
  draggingFromUser.value = null;
}

onMounted(async () => {
  try {
    const vendorId = user?.vendorId;
    const [uRes, aRes, pRes] = await Promise.all([
      http.get(vendorId ? `/api/users?vendorId=${vendorId}` : "/api/users"),
      http.get("/api/assignments"),
      http.get(vendorId ? `/api/projects?vendorId=${vendorId}` : "/api/projects"),
    ]);
    personnel.value = uRes.data;
    assignments.value = aRes.data;
    projects.value = pRes.data;
    // Get vendor name from first project
    if (pRes.data.length > 0) vendorName.value = pRes.data[0].vendorName ?? "";
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});
</script>

<style scoped>
.personnel-page { display:flex; flex-direction:column; gap:24px; }
.page-header { display:flex; align-items:center; gap:16px; }
.vendor-label { font-size:13px; color:#64748b; background:#f1f5f9; padding:4px 12px; border-radius:999px; border:1px solid #e2e8f0; font-weight:500; }

.stats-bar { display:grid; grid-template-columns:repeat(4,1fr); gap:12px; }
.stat-card { background:#ffffff; border:1px solid #e2e8f0; border-radius:12px; padding:16px; text-align:center; box-shadow:0 1px 3px rgba(0,0,0,0.04); }
.stat-num { font-size:28px; font-weight:800; color:#0f172a; }
.stat-label { font-size:12px; color:#64748b; margin-top:4px; }

.team-grid { display:flex; flex-direction:column; gap:16px; }
.member-card { background:#ffffff; border:1px solid #e2e8f0; border-radius:14px; padding:20px; display:flex; flex-direction:column; gap:16px; box-shadow:0 1px 3px rgba(0,0,0,0.04); }
.member-header { display:flex; align-items:center; gap:14px; }
.member-avatar { width:48px; height:48px; border-radius:12px; background:linear-gradient(135deg,#6366f1,#a855f7); display:grid; place-items:center; font-size:18px; font-weight:800; color:#fff; flex-shrink:0; }
.member-info { flex:1; }
.member-name { font-size:15px; font-weight:700; color:#0f172a; }
.member-meta { display:flex; align-items:center; gap:8px; margin-top:4px; }
.member-username { font-size:12px; color:#64748b; }
.member-email { font-size:12px; color:#94a3b8; margin-top:4px; }
.role-chip { display:inline-block; padding:2px 10px; border-radius:999px; font-size:11px; font-weight:600; }
.role-chip.blue { background:#eff6ff; color:#2563eb; border:1px solid #bfdbfe; }
.role-chip.purple { background:#f5f3ff; color:#7c3aed; border:1px solid #ddd6fe; }

.member-tasks { border-top:1px solid #f1f5f9; padding-top:14px; }
.tasks-title { font-size:13px; font-weight:700; color:#475569; margin-bottom:12px; display:flex; align-items:center; gap:8px; }
.task-total-badge { font-size:11px; background:#e2e8f0; border-radius:999px; padding:2px 8px; color:#64748b; font-weight:600; }

.task-mini-kanban { display:grid; grid-template-columns:repeat(3,1fr); gap:10px; }
.mini-col { background:#f8fafc; border:2px solid #e2e8f0; border-radius:10px; padding:10px; min-height:60px; transition:border-color 0.15s; }
.mini-col.drag-over { border-color:#6366f1; background:#f5f3ff; }
.mini-col-header { display:flex; align-items:center; gap:6px; margin-bottom:8px; font-size:11px; color:#64748b; font-weight:600; text-transform:uppercase; letter-spacing:0.04em; }
.mini-dot { width:7px; height:7px; border-radius:50%; flex-shrink:0; }
.mini-dot.gray { background:#64748b; }
.mini-dot.amber { background:#f59e0b; }
.mini-dot.green { background:#22c55e; }
.mini-count { margin-left:auto; font-size:10px; background:#e2e8f0; border-radius:999px; padding:1px 6px; color:#64748b; }
.mini-task { display:flex; align-items:flex-start; gap:6px; background:#ffffff; border:1px solid #e2e8f0; border-radius:8px; padding:8px; margin-bottom:6px; cursor:grab; user-select:none; transition:border-color 0.15s; box-shadow:0 1px 2px rgba(0,0,0,0.03); }
.mini-task:hover { border-color:#a78bfa; }
.mini-task:active { cursor:grabbing; }
.priority-dot { width:7px; height:7px; border-radius:50%; flex-shrink:0; margin-top:3px; }
.priority-dot.red { background:#ef4444; }
.priority-dot.amber { background:#f59e0b; }
.priority-dot.blue { background:#3b82f6; }
.priority-dot.gray { background:#64748b; }
.mini-task-name { font-size:11px; font-weight:600; color:#1e293b; line-height:1.3; flex:1; }
.mini-project { font-size:10px; color:#94a3b8; white-space:nowrap; margin-left:auto; }

.no-tasks { font-size:12px; color:#94a3b8; text-align:center; padding:12px; }
.loading-text { color:#94a3b8; font-size:14px; text-align:center; padding:40px; }
</style>
