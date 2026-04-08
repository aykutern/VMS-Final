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

.breadcrumb { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #64748b; }
.breadcrumb a { color: #2563eb; text-decoration: none; }
.breadcrumb a:hover { color: #1d4ed8; }

.project-hero { display: flex; align-items: center; gap: 16px; }
.project-avatar { width: 56px; height: 56px; border-radius: 14px; background: linear-gradient(135deg,#3b82f6,#6366f1); display: grid; place-items: center; font-size: 22px; font-weight: 800; color: #fff; flex-shrink: 0; }
.project-hero h2 { margin: 0; font-size: 22px; font-weight: 800; color: #0f172a; }
.project-hero p { margin: 4px 0 0; font-size: 13px; color: #64748b; }

.tabs { display: flex; gap: 4px; border-bottom: 2px solid #e2e8f0; }
.tab-btn { padding: 10px 18px; background: transparent; border: none; border-bottom: 2px solid transparent; color: #64748b; font-size: 14px; font-weight: 600; cursor: pointer; transition: color 0.15s; margin-bottom: -2px; }
.tab-btn.active { border-bottom-color: #2563eb; color: #2563eb; }
.tab-btn:hover:not(.active) { color: #1e293b; }

.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.section-header h3 { margin: 0; font-size: 15px; font-weight: 700; color: #0f172a; }

.ann-card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 10px; padding: 16px; margin-bottom: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.ann-title { font-size: 14px; font-weight: 700; color: #0f172a; }
.ann-content { font-size: 13px; color: #475569; margin-top: 6px; line-height: 1.6; }

.sprint-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 14px; }
.sprint-card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px; display: flex; flex-direction: column; gap: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.sprint-top { display: flex; align-items: center; justify-content: space-between; }
.sprint-name { font-size: 14px; font-weight: 700; color: #0f172a; }
.sprint-goal { font-size: 12px; color: #475569; line-height: 1.5; }
.sprint-dates { font-size: 11px; color: #94a3b8; }

/* Read-only Tasks Table */
.tasks-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.tasks-table th { text-align: left; padding: 10px 14px; color: #64748b; font-weight: 600; border-bottom: 1px solid #e2e8f0; white-space: nowrap; font-size: 11px; text-transform: uppercase; letter-spacing: 0.06em; }
.tasks-table td { padding: 12px 14px; color: #1e293b; border-bottom: 1px solid #f1f5f9; }
.tasks-table tr:last-child td { border-bottom: none; }
.tasks-table tr:hover td { background: #f8fafc; }
.task-name-cell { font-weight: 600; color: #0f172a; }
.date-cell { color: #94a3b8; font-size: 12px; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 11px; font-weight: 600; }
.badge.green { background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }
.badge.amber { background: #fffbeb; color: #d97706; border: 1px solid #fde68a; }
.badge.red { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
.badge.blue { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
.badge.gray { background: #f1f5f9; color: #64748b; border: 1px solid #e2e8f0; }

.status-badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 10px; font-weight: 600; }
.status-badge.green { background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }
.status-badge.amber { background: #fffbeb; color: #d97706; border: 1px solid #fde68a; }
.status-badge.gray { background: #f1f5f9; color: #64748b; border: 1px solid #e2e8f0; }

.warn-text { font-size: 12px; color: #d97706; margin-top: 4px; display: block; }

.tab-content { display: flex; flex-direction: column; }
.primary-btn { padding: 9px 18px; background: #2563eb; color: #fff; border: none; border-radius: 8px; font-weight: 700; font-size: 14px; cursor: pointer; transition: background 0.15s; }
.primary-btn:hover { background: #1d4ed8; }
.secondary-btn { padding: 9px 16px; background: #f1f5f9; color: #475569; border: 1px solid #e2e8f0; border-radius: 8px; font-weight: 600; font-size: 14px; cursor: pointer; }
.secondary-btn:hover { background: #e2e8f0; }
.close-btn { background: transparent; border: none; color: #94a3b8; font-size: 18px; cursor: pointer; }
.close-btn:hover { color: #475569; }
.modal-backdrop { position: fixed; inset: 0; background: rgba(15,23,42,0.5); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 28px; width: min(500px,90vw); display: flex; flex-direction: column; gap: 16px; box-shadow: 0 20px 40px rgba(0,0,0,0.15); }
.modal-header { display: flex; align-items: center; justify-content: space-between; }
.modal-header h3 { margin: 0; font-size: 17px; font-weight: 800; color: #0f172a; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 13px; color: #475569; font-weight: 600; }
.form-group input,.form-group select,.form-group textarea { background: #f8fafc; border: 1px solid #cbd5e1; border-radius: 8px; padding: 10px 14px; color: #0f172a; font-size: 14px; outline: none; resize: vertical; }
.form-group input:focus,.form-group select:focus,.form-group textarea:focus { border-color: #2563eb; box-shadow: 0 0 0 3px rgba(37,99,235,0.1); }
.modal-actions { display: flex; gap: 10px; justify-content: flex-end; }

.loading-text, .empty-text { color: #94a3b8; font-size: 14px; text-align: center; padding: 32px; }
</style>
