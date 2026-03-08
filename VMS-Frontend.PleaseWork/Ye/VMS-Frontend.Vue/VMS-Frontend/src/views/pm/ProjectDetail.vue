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

      <!-- Tasks Tab — Read-Only Observer -->
      <div v-if="activeTab === 'Tasks'" class="tab-content">
        <div class="section-header">
          <h3>Assignments</h3>
        </div>

        <div v-if="assignments.length === 0" class="empty-text">No tasks yet.</div>
        <table v-else class="tasks-table">
          <thead>
            <tr>
              <th>Task</th>
              <th>Priority</th>
              <th>Status</th>
              <th>Assigned</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in assignments" :key="t.id">
              <td class="task-name-cell">{{ t.name }}</td>
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

const annForm = ref({ title: "", content: "" });
const sprintForm = ref({ sprintName: "", goal: "", startDate: "", endDate: "", status: "PLANNED" });

const hasActiveSprint = computed(() => sprints.value.some(s => s.status === "ACTIVE"));
function priorityColor(p) { return { HIGH: "red", MEDIUM: "amber", LOW: "blue", OPTIONAL: "gray" }[p] ?? "gray"; }

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

/* Read-only Tasks Table */
.tasks-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.tasks-table th { text-align: left; padding: 10px 14px; color: rgba(200,215,255,0.5); font-weight: 600; border-bottom: 1px solid rgba(255,255,255,0.07); white-space: nowrap; font-size: 11px; text-transform: uppercase; letter-spacing: 0.06em; }
.tasks-table td { padding: 12px 14px; color: #d1deff; border-bottom: 1px solid rgba(255,255,255,0.04); }
.tasks-table tr:last-child td { border-bottom: none; }
.tasks-table tr:hover td { background: rgba(255,255,255,0.03); }
.task-name-cell { font-weight: 600; color: #e2eaff; }
.date-cell { color: rgba(200,215,255,0.45); font-size: 12px; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 11px; font-weight: 600; }
.badge.green { background: rgba(34,197,94,0.15); color: #86efac; }
.badge.amber { background: rgba(251,191,36,0.15); color: #fde68a; }
.badge.red { background: rgba(239,68,68,0.15); color: #fca5a5; }
.badge.blue { background: rgba(59,130,246,0.15); color: #93c5fd; }
.badge.gray { background: rgba(148,163,184,0.12); color: #94a3b8; }

.status-badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 10px; font-weight: 600; }
.status-badge.green { background: rgba(34,197,94,0.15); color: #86efac; }
.status-badge.amber { background: rgba(251,191,36,0.15); color: #fde68a; }
.status-badge.gray { background: rgba(148,163,184,0.12); color: #94a3b8; }

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
