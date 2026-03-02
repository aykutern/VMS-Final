<template>
  <div class="project-detail">
    <div class="breadcrumb">
      <router-link to="/pm/projects">Projects</router-link>
      <span>/</span>
      <span>{{ project?.projectName ?? 'Loading…' }}</span>
    </div>

    <div v-if="loading" class="loading-text">Loading project…</div>
    <template v-else>
      <div class="project-hero">
        <div class="project-avatar">{{ project?.projectName?.[0] ?? 'P' }}</div>
        <div>
          <h2>{{ project?.projectName }}</h2>
          <p>Vendor: <strong>{{ project?.vendorName ?? '—' }}</strong> · Manager: <strong>{{ project?.projectManagerName ?? '—' }}</strong></p>
        </div>
      </div>

      <!-- Tabs -->
      <div class="tabs">
        <button v-for="t in tabs" :key="t" :class="['tab-btn', { active: activeTab === t }]" @click="activeTab = t">{{ t }}</button>
      </div>

      <!-- Announcements Tab -->
      <div v-if="activeTab === 'Announcements'" class="tab-content">
        <div class="section-header">
          <h3>Announcements</h3>
          <button class="primary-btn" @click="showAnnModal = true">+ Add</button>
        </div>
        <div class="announcement-list">
          <div v-for="a in announcements" :key="a.id" class="ann-card">
            <div class="ann-title">{{ a.title }}</div>
            <div class="ann-content">{{ a.content }}</div>
          </div>
          <div v-if="announcements.length === 0" class="empty-text">No announcements for this project.</div>
        </div>
      </div>

      <!-- Sprints Tab -->
      <div v-if="activeTab === 'Sprints'" class="tab-content">
        <div class="section-header">
          <h3>Sprints</h3>
          <button class="primary-btn" @click="openSprintModal">+ New Sprint</button>
        </div>
        <div class="sprint-cards">
          <div v-for="s in sprints" :key="s.id" class="sprint-card">
            <div class="sprint-top">
              <span class="sprint-name">{{ s.sprintName }}</span>
              <span :class="['badge', s.status === 'ACTIVE' ? 'green' : s.status === 'COMPLETED' ? 'gray' : 'amber']">{{ s.status }}</span>
            </div>
            <div class="sprint-goal">{{ s.goal }}</div>
            <div class="sprint-dates">{{ s.startDate }} → {{ s.endDate }}</div>
          </div>
          <div v-if="sprints.length === 0" class="empty-text">No sprints yet.</div>
        </div>
      </div>

      <!-- Tasks Tab — Drag-and-Drop Kanban -->
      <div v-if="activeTab === 'Tasks'" class="tab-content">
        <div class="section-header">
          <h3>Assignments</h3>
          <button class="primary-btn" @click="showTaskModal = true">+ New Task</button>
        </div>

        <div class="kanban">
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
                @dragstart="onDragStart(t)"
                @dragend="dragTarget = null"
              >
                <div class="drag-handle">⠿</div>
                <div class="task-name">{{ t.name }}</div>
                <div class="task-meta">
                  <span :class="['badge', priorityColor(t.priority)]">{{ t.priority }}</span>
                  <span class="task-date">{{ t.assignedAt }}</span>
                </div>
              </div>
              <div v-if="tasksByStatus(col.status).length === 0" class="col-empty">Drop tasks here</div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- Announcement Modal -->
    <div v-if="showAnnModal" class="modal-backdrop" @click.self="showAnnModal = false">
      <div class="modal">
        <div class="modal-header"><h3>New Announcement</h3><button class="close-btn" @click="showAnnModal = false">✕</button></div>
        <div class="form-group"><label>Title</label><input v-model="annForm.title" placeholder="Announcement title" /></div>
        <div class="form-group"><label>Content</label><textarea v-model="annForm.content" rows="4" placeholder="Write your announcement…"></textarea></div>
        <div class="modal-actions">
          <button class="secondary-btn" @click="showAnnModal = false">Cancel</button>
          <button class="primary-btn" @click="createAnnouncement">Post Announcement</button>
        </div>
      </div>
    </div>

    <!-- Sprint Modal -->
    <div v-if="showSprintModal" class="modal-backdrop" @click.self="showSprintModal = false">
      <div class="modal">
        <div class="modal-header"><h3>New Sprint</h3><button class="close-btn" @click="showSprintModal = false">✕</button></div>
        <div class="form-group"><label>Sprint Name</label><input v-model="sprintForm.sprintName" placeholder="Sprint 1 — Foundation" /></div>
        <div class="form-group"><label>Goal</label><textarea v-model="sprintForm.goal" rows="3" placeholder="Sprint goal…"></textarea></div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
          <div class="form-group"><label>Start Date</label><input v-model="sprintForm.startDate" type="date" /></div>
          <div class="form-group"><label>End Date</label><input v-model="sprintForm.endDate" type="date" /></div>
        </div>
        <div class="form-group">
          <label>Status</label>
          <select v-model="sprintForm.status">
            <option>PLANNED</option>
            <option>ACTIVE</option>
          </select>
          <span v-if="sprintForm.status === 'ACTIVE' && hasActiveSprint" class="warn-text">⚠ Current active sprint will be set to PLANNED.</span>
        </div>
        <div class="modal-actions">
          <button class="secondary-btn" @click="showSprintModal = false">Cancel</button>
          <button class="primary-btn" @click="createSprint">Create Sprint</button>
        </div>
      </div>
    </div>

    <!-- Task Modal -->
    <div v-if="showTaskModal" class="modal-backdrop" @click.self="showTaskModal = false">
      <div class="modal">
        <div class="modal-header"><h3>New Task</h3><button class="close-btn" @click="showTaskModal = false">✕</button></div>
        <div class="form-group"><label>Task Name</label><input v-model="taskForm.name" placeholder="e.g. Setup CI/CD pipeline" /></div>
        <div class="form-group">
          <label>Priority</label>
          <select v-model="taskForm.priority">
            <option>HIGH</option><option>MEDIUM</option><option>LOW</option><option>OPTIONAL</option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="secondary-btn" @click="showTaskModal = false">Cancel</button>
          <button class="primary-btn" @click="createTask">Create Task</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import { http } from "../../lib/http.js";

const route = useRoute();
const projectId = route.params.id;

const project = ref(null);
const announcements = ref([]);
const sprints = ref([]);
const assignments = ref([]);
const loading = ref(true);
const activeTab = ref("Announcements");
const tabs = ["Announcements", "Sprints", "Tasks"];

const showAnnModal = ref(false);
const showSprintModal = ref(false);
const showTaskModal = ref(false);

const annForm = ref({ title: "", content: "" });
const sprintForm = ref({ sprintName: "", goal: "", startDate: "", endDate: "", status: "PLANNED" });
const taskForm = ref({ name: "", priority: "MEDIUM" });

// Drag-and-drop state
const draggingTask = ref(null);
const dragTarget = ref(null);

const columns = [
  { status: "TODO", label: "To Do", color: "gray" },
  { status: "IN_PROGRESS", label: "In Progress", color: "amber" },
  { status: "COMPLETED", label: "Completed", color: "green" },
];

const hasActiveSprint = computed(() => sprints.value.some(s => s.status === "ACTIVE"));
function tasksByStatus(status) { return assignments.value.filter(a => a.status === status); }
function priorityColor(p) { return { HIGH: "red", MEDIUM: "amber", LOW: "blue", OPTIONAL: "gray" }[p] ?? "gray"; }

function onDragStart(task) { draggingTask.value = task; }

// ── Sprint auto-date helpers ──────────────────────────────────────────────
function getWeekMonday(date) {
  const d = new Date(date);
  const day = d.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  d.setDate(d.getDate() + diff);
  d.setHours(0, 0, 0, 0);
  return d;
}
function toISO(d) { return d.toISOString().split('T')[0]; }

function openSprintModal() {
  const active = sprints.value.find(s => s.status === 'ACTIVE');
  let startMonday;
  if (active?.endDate) {
    const afterEnd = new Date(active.endDate);
    afterEnd.setDate(afterEnd.getDate() + 1);
    startMonday = getWeekMonday(afterEnd);
    if (startMonday <= new Date(active.endDate)) startMonday.setDate(startMonday.getDate() + 7);
  } else {
    startMonday = getWeekMonday(new Date());
  }
  const endFriday = new Date(startMonday);
  endFriday.setDate(endFriday.getDate() + 25);
  sprintForm.value = {
    sprintName: "", goal: "",
    startDate: toISO(startMonday),
    endDate: toISO(endFriday),
    status: active ? "PLANNED" : "ACTIVE",
  };
  showSprintModal.value = true;
}

async function onDrop(newStatus) {
  dragTarget.value = null;
  if (!draggingTask.value || draggingTask.value.status === newStatus) return;
  const task = draggingTask.value;
  const oldStatus = task.status;
  task.status = newStatus; // optimistic update
  try {
    await http.patch(`/api/assignments/${task.id}/status`, { status: newStatus });
  } catch (e) {
    task.status = oldStatus; // rollback
    console.error(e);
  }
  draggingTask.value = null;
}

onMounted(async () => {
  try {
    const [pRes, aRes, sRes, tRes] = await Promise.all([
      http.get(`/api/projects/${projectId}`),
      http.get(`/api/announcements?projectId=${projectId}`),
      http.get(`/api/sprints?projectId=${projectId}`),
      http.get(`/api/assignments?projectId=${projectId}`),
    ]);
    project.value = pRes.data;
    announcements.value = aRes.data;
    sprints.value = sRes.data;
    assignments.value = tRes.data;
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});

async function createAnnouncement() {
  await http.post("/api/announcements", { title: annForm.value.title, content: annForm.value.content, projectId: Number(projectId) });
  const r = await http.get(`/api/announcements?projectId=${projectId}`);
  announcements.value = r.data;
  showAnnModal.value = false;
  annForm.value = { title: "", content: "" };
}

async function createSprint() {
  await http.post("/api/sprints", { ...sprintForm.value, projectId: Number(projectId) });
  const r = await http.get(`/api/sprints?projectId=${projectId}`);
  sprints.value = r.data;
  showSprintModal.value = false;
  sprintForm.value = { sprintName: "", goal: "", startDate: "", endDate: "", status: "PLANNED" };
}

async function createTask() {
  await http.post("/api/assignments", { name: taskForm.value.name, priority: taskForm.value.priority, projectId: Number(projectId), assignedAt: new Date().toISOString().split("T")[0] });
  const r = await http.get(`/api/assignments?projectId=${projectId}`);
  assignments.value = r.data;
  showTaskModal.value = false;
  taskForm.value = { name: "", priority: "MEDIUM" };
}
</script>

<style scoped>
.project-detail { display: flex; flex-direction: column; gap: 24px; }

.breadcrumb { display: flex; align-items: center; gap: 8px; font-size: 13px; color: rgba(200,215,255,0.5); }
.breadcrumb a { color: #93c5fd; text-decoration: none; }

.project-hero { display: flex; align-items: center; gap: 16px; }
.project-avatar { width: 56px; height: 56px; border-radius: 16px; background: linear-gradient(135deg,#3b82f6,#6366f1); display: grid; place-items: center; font-size: 22px; font-weight: 800; color: #fff; flex-shrink: 0; }
.project-hero h2 { margin: 0; font-size: 22px; font-weight: 800; color: #f3f7ff; }
.project-hero p { margin: 4px 0 0; font-size: 13px; color: rgba(200,215,255,0.6); }

.tabs { display: flex; gap: 4px; border-bottom: 1px solid rgba(255,255,255,0.08); }
.tab-btn { padding: 10px 18px; background: transparent; border: none; border-bottom: 2px solid transparent; color: rgba(200,215,255,0.55); font-size: 14px; font-weight: 600; cursor: pointer; transition: color 0.15s; margin-bottom: -1px; }
.tab-btn.active { border-bottom-color: #3b82f6; color: #93c5fd; }
.tab-btn:hover:not(.active) { color: #d1deff; }

.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.section-header h3 { margin: 0; font-size: 15px; font-weight: 700; color: #e2eaff; }

.ann-card { background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 16px; margin-bottom: 10px; }
.ann-title { font-size: 14px; font-weight: 700; color: #e2eaff; }
.ann-content { font-size: 13px; color: rgba(200,215,255,0.6); margin-top: 6px; line-height: 1.6; }

.sprint-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 14px; }
.sprint-card { background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); border-radius: 14px; padding: 18px; display: flex; flex-direction: column; gap: 8px; }
.sprint-top { display: flex; align-items: center; justify-content: space-between; }
.sprint-name { font-size: 14px; font-weight: 700; color: #e2eaff; }
.sprint-goal { font-size: 12px; color: rgba(200,215,255,0.6); line-height: 1.5; }
.sprint-dates { font-size: 11px; color: rgba(200,215,255,0.4); }

/* Kanban */
.kanban { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.kanban-col { background: rgba(255,255,255,0.03); border: 2px solid rgba(255,255,255,0.06); border-radius: 16px; padding: 16px; min-height: 300px; display: flex; flex-direction: column; gap: 10px; transition: border-color 0.15s; }
.kanban-col.drag-over { border-color: rgba(99,102,241,0.5); background: rgba(99,102,241,0.05); }
.col-header { display: flex; align-items: center; gap: 8px; }
.col-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.col-dot.gray { background: #64748b; }
.col-dot.amber { background: #f59e0b; }
.col-dot.green { background: #22c55e; }
.col-title { font-size: 14px; font-weight: 700; color: #e2eaff; flex: 1; }
.col-count { font-size: 12px; background: rgba(255,255,255,0.1); border-radius: 999px; padding: 2px 8px; color: #94a3b8; }
.task-stack { display: flex; flex-direction: column; gap: 10px; }
.task-card { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.09); border-radius: 12px; padding: 12px 14px; display: flex; flex-direction: column; gap: 8px; cursor: grab; user-select: none; transition: border-color 0.15s, transform 0.1s; }
.task-card:active { cursor: grabbing; transform: rotate(1deg) scale(1.02); }
.task-card:hover { border-color: rgba(99,102,241,0.35); }
.drag-handle { font-size: 14px; color: rgba(200,215,255,0.25); margin-bottom: -4px; }
.task-name { font-size: 13px; font-weight: 600; color: #e2eaff; line-height: 1.4; }
.task-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.task-date { font-size: 11px; color: rgba(200,215,255,0.35); }
.col-empty { font-size: 12px; color: rgba(200,215,255,0.25); text-align: center; padding: 20px; border: 2px dashed rgba(255,255,255,0.08); border-radius: 10px; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 11px; font-weight: 600; }
.badge.green { background: rgba(34,197,94,0.15); color: #86efac; }
.badge.amber { background: rgba(251,191,36,0.15); color: #fde68a; }
.badge.red { background: rgba(239,68,68,0.15); color: #fca5a5; }
.badge.blue { background: rgba(59,130,246,0.15); color: #93c5fd; }
.badge.gray { background: rgba(148,163,184,0.12); color: #94a3b8; }

.warn-text { font-size: 12px; color: #fbbf24; margin-top: 4px; display: block; }

.tab-content { display: flex; flex-direction: column; }
.primary-btn { padding: 9px 18px; background: linear-gradient(135deg,#3b82f6,#6366f1); color: #fff; border: none; border-radius: 10px; font-weight: 700; font-size: 14px; cursor: pointer; }
.secondary-btn { padding: 9px 16px; background: rgba(255,255,255,0.07); color: #d1deff; border: 1px solid rgba(255,255,255,0.1); border-radius: 10px; font-weight: 600; font-size: 14px; cursor: pointer; }
.close-btn { background: transparent; border: none; color: rgba(200,215,255,0.5); font-size: 18px; cursor: pointer; }
.modal-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #0f1a2e; border: 1px solid rgba(255,255,255,0.12); border-radius: 20px; padding: 28px; width: min(500px,90vw); display: flex; flex-direction: column; gap: 16px; }
.modal-header { display: flex; align-items: center; justify-content: space-between; }
.modal-header h3 { margin: 0; font-size: 17px; font-weight: 800; color: #f3f7ff; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 13px; color: rgba(200,215,255,0.7); font-weight: 600; }
.form-group input,.form-group select,.form-group textarea { background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.12); border-radius: 10px; padding: 10px 14px; color: #e2eaff; font-size: 14px; outline: none; resize: vertical; }
.form-group select option { background: #0f1a2e; }
.modal-actions { display: flex; gap: 10px; justify-content: flex-end; }

.loading-text, .empty-text { color: rgba(200,215,255,0.4); font-size: 14px; text-align: center; padding: 32px; }
</style>
