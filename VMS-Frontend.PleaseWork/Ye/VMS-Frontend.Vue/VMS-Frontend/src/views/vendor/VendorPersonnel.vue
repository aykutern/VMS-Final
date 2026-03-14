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
.page-header h2 { margin:0; font-size:20px; font-weight:800; color:#f3f7ff; }
.vendor-label { font-size:13px; color:rgba(200,215,255,0.5); background:rgba(255,255,255,0.06); padding:4px 12px; border-radius:999px; }

.stats-bar { display:grid; grid-template-columns:repeat(4,1fr); gap:12px; }
.stat-card { background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:14px; padding:16px; text-align:center; }
.stat-num { font-size:28px; font-weight:800; color:#f3f7ff; }
.stat-label { font-size:12px; color:rgba(200,215,255,0.5); margin-top:4px; }

.team-grid { display:flex; flex-direction:column; gap:16px; }
.member-card { background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.08); border-radius:18px; padding:20px; display:flex; flex-direction:column; gap:16px; }
.member-header { display:flex; align-items:center; gap:14px; }
.member-avatar { width:48px; height:48px; border-radius:14px; background:linear-gradient(135deg,#6366f1,#a855f7); display:grid; place-items:center; font-size:18px; font-weight:800; color:#fff; flex-shrink:0; }
.member-info { flex:1; }
.member-name { font-size:15px; font-weight:700; color:#e2eaff; }
.member-meta { display:flex; align-items:center; gap:8px; margin-top:4px; }
.member-username { font-size:12px; color:rgba(200,215,255,0.5); }
.member-email { font-size:12px; color:rgba(200,215,255,0.4); margin-top:4px; }
.role-chip { display:inline-block; padding:2px 10px; border-radius:999px; font-size:11px; font-weight:600; }
.role-chip.blue { background:rgba(59,130,246,0.15); color:#93c5fd; }
.role-chip.purple { background:rgba(168,85,247,0.15); color:#d8b4fe; }

.member-tasks { border-top:1px solid rgba(255,255,255,0.07); padding-top:14px; }
.tasks-title { font-size:13px; font-weight:700; color:rgba(200,215,255,0.6); margin-bottom:12px; display:flex; align-items:center; gap:8px; }
.task-total-badge { font-size:11px; background:rgba(255,255,255,0.1); border-radius:999px; padding:2px 8px; color:#94a3b8; }

.task-mini-kanban { display:grid; grid-template-columns:repeat(3,1fr); gap:10px; }
.mini-col { background:rgba(255,255,255,0.03); border:2px solid rgba(255,255,255,0.05); border-radius:12px; padding:10px; min-height:60px; transition:border-color 0.15s; }
.mini-col.drag-over { border-color:rgba(99,102,241,0.5); background:rgba(99,102,241,0.05); }
.mini-col-header { display:flex; align-items:center; gap:6px; margin-bottom:8px; font-size:11px; color:rgba(200,215,255,0.5); font-weight:600; }
.mini-dot { width:7px; height:7px; border-radius:50%; flex-shrink:0; }
.mini-dot.gray { background:#64748b; }
.mini-dot.amber { background:#f59e0b; }
.mini-dot.green { background:#22c55e; }
.mini-count { margin-left:auto; font-size:10px; background:rgba(255,255,255,0.08); border-radius:999px; padding:1px 6px; }
.mini-task { display:flex; align-items:flex-start; gap:6px; background:rgba(255,255,255,0.05); border:1px solid rgba(255,255,255,0.08); border-radius:8px; padding:8px; margin-bottom:6px; cursor:grab; user-select:none; transition:border-color 0.15s; }
.mini-task:hover { border-color:rgba(99,102,241,0.35); }
.mini-task:active { cursor:grabbing; }
.priority-dot { width:7px; height:7px; border-radius:50%; flex-shrink:0; }
.priority-dot.red { background:#ef4444; }
.priority-dot.amber { background:#f59e0b; }
.priority-dot.blue { background:#3b82f6; }
.priority-dot.gray { background:#64748b; }
.mini-task-name { font-size:11px; font-weight:600; color:#d1deff; line-height:1.3; flex:1; }
.mini-project { font-size:10px; color:rgba(200,215,255,0.35); white-space:nowrap; margin-left:auto; }

.no-tasks { font-size:12px; color:rgba(200,215,255,0.3); text-align:center; padding:12px; }
.loading-text { color:rgba(200,215,255,0.4); font-size:14px; text-align:center; padding:40px; }
</style>
