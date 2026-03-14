<template>
  <div class="vendor-sprints">
    <!-- Toolbar: filter + new sprint button -->
    <div class="toolbar">
      <select v-model="filterProject" @change="fetchSprints">
        <option value="">All Projects</option>
        <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.projectName }}</option>
      </select>
      <div class="spacer"></div>
      <button class="primary-btn" @click="openCreateModal">+ New Sprint</button>
    </div>

    <div v-if="loading" class="loading-text">Loading sprints…</div>
    <div v-else class="sprint-grid">
      <div
        v-for="s in sprints"
        :key="s.id"
        class="sprint-card"
        @dblclick="openSprintDetail(s)"
        title="Double-click to view tasks"
      >
        <div class="sprint-top">
          <div>
            <div class="sprint-name">{{ s.sprintName }}</div>
            <div class="sprint-project">{{ s.projectName }}</div>
          </div>
          <span :class="['badge', s.status === 'ACTIVE' ? 'green' : s.status === 'COMPLETED' ? 'gray' : 'amber']">{{ s.status }}</span>
        </div>
        <div class="sprint-goal" v-if="s.goal">{{ s.goal }}</div>

        <!-- Member avatars -->
        <div v-if="s.members && s.members.length > 0" class="members-row">
          <span v-for="m in s.members" :key="m.id" class="member-chip" :title="m.fullName">
            {{ initials(m.fullName) }}
          </span>
        </div>
        <div v-else class="members-empty">No developers assigned</div>

        <div class="sprint-dates">{{ s.startDate }} → {{ s.endDate }}</div>
        <div class="dbl-hint">Double-click for details</div>
      </div>
      <div v-if="sprints.length === 0" class="empty-state">No sprints found.</div>
    </div>

    <!-- ── Create Sprint Modal ────────────────────────────────── -->
    <div v-if="showModal" class="modal-backdrop" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>New Sprint</h3>
          <button class="close-btn" @click="showModal = false">✕</button>
        </div>

        <div class="form-group">
          <label>Project</label>
          <select v-model="form.projectId" @change="onProjectChange">
            <option value="">— Select project —</option>
            <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.projectName }}</option>
          </select>
        </div>

        <div class="form-group">
          <label>Sprint Name</label>
          <input v-model="form.sprintName" placeholder="Sprint 1 — Foundation" />
        </div>

        <div class="form-group">
          <label>Goal</label>
          <textarea v-model="form.goal" rows="2" placeholder="What do you want to achieve?"></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Start Date</label>
            <input v-model="form.startDate" type="date" />
          </div>
          <div class="form-group">
            <label>End Date</label>
            <input v-model="form.endDate" type="date" />
          </div>
        </div>

        <div class="form-group">
          <label>Status</label>
          <select v-model="form.status">
            <option>PLANNED</option>
            <option>ACTIVE</option>
          </select>
        </div>

        <!-- Developer picker -->
        <div class="form-group">
          <label>Assign Developers</label>
          <div v-if="availablePersonnel.length === 0" class="dev-empty">
            {{ form.projectId ? 'No developers available for this project\'s vendor.' : 'Select a project first.' }}
          </div>
          <div v-else class="dev-picker">
            <label
              v-for="dev in availablePersonnel"
              :key="dev.id"
              class="dev-option"
              :class="{ selected: form.memberIds.includes(dev.id), disabled: dev.unavailable }"
            >
              <input
                type="checkbox"
                :value="dev.id"
                v-model="form.memberIds"
                :disabled="dev.unavailable && !form.memberIds.includes(dev.id)"
              />
              <span class="dev-avatar">{{ initials(dev.fullName) }}</span>
              <span class="dev-name">{{ dev.fullName }}</span>
              <span v-if="dev.unavailable" class="dev-tag">in sprint</span>
            </label>
          </div>
        </div>

        <div class="modal-actions">
          <button class="secondary-btn" @click="showModal = false">Cancel</button>
          <button class="primary-btn" @click="createSprint" :disabled="saving">
            {{ saving ? 'Creating…' : 'Create Sprint' }}
          </button>
        </div>
        <div v-if="formError" class="form-error">{{ formError }}</div>
      </div>
    </div>

    <!-- ── Sprint Detail Modal ────────────────────────────────── -->
    <div v-if="detailSprint" class="modal-backdrop" @click.self="detailSprint = null">
      <div class="detail-modal">
        <button class="close-btn" @click="detailSprint = null">✕</button>
        <div class="detail-header">
          <div class="detail-title-block">
            <h3>{{ detailSprint.sprintName }}</h3>
            <div class="detail-meta">
              <span :class="['badge', detailSprint.status === 'ACTIVE' ? 'green' : detailSprint.status === 'COMPLETED' ? 'gray' : 'amber']">
                {{ detailSprint.status }}
              </span>
              <p>{{ detailSprint.projectName }} · {{ detailSprint.startDate }} → {{ detailSprint.endDate }}</p>
            </div>
          </div>
        </div>
        <div class="detail-goal" v-if="detailSprint.goal">{{ detailSprint.goal }}</div>

        <!-- Members -->
        <div class="detail-section" v-if="detailSprint.members && detailSprint.members.length">
          <div class="detail-section-title">Team ({{ detailSprint.members.length }})</div>
          <div class="member-chips">
            <span v-for="m in detailSprint.members" :key="m.id" class="member-chip-lg">{{ m.fullName }}</span>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-section-title">Tasks ({{ sprintTasks.length }})</div>
          <div v-if="loadingTasks" class="loading-text">Loading tasks…</div>
          <table v-else-if="sprintTasks.length > 0" class="tasks-table">
            <thead>
              <tr><th>Task</th><th>Assignee</th><th>Status</th><th>Priority</th><th>Completed</th></tr>
            </thead>
            <tbody>
              <tr v-for="t in sprintTasks" :key="t.id">
                <td>{{ t.name }}</td>
                <td>{{ t.assigneeName || '—' }}</td>
                <td>
                  <span :class="['status-badge', t.status === 'COMPLETED' ? 'green' : t.status === 'IN_PROGRESS' ? 'amber' : 'gray']">
                    {{ t.status?.replace('_', ' ') }}
                  </span>
                </td>
                <td>{{ t.priority }}</td>
                <td>{{ t.completedAt || '—' }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-text">No tasks in this sprint.</div>
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
const sprints = ref([]);
const projects = ref([]);
const loading = ref(true);
const filterProject = ref("");

// Create modal
const showModal = ref(false);
const saving = ref(false);
const formError = ref(null);
const availablePersonnel = ref([]);
const form = ref({
  projectId: "", sprintName: "", goal: "", startDate: "", endDate: "",
  status: "PLANNED", memberIds: []
});

// Detail modal
const detailSprint = ref(null);
const sprintTasks = ref([]);
const loadingTasks = ref(false);

function initials(name) {
  if (!name) return "?";
  return name.split(" ").map(p => p[0]).join("").toUpperCase().slice(0, 2);
}

async function openCreateModal() {
  form.value = { projectId: "", sprintName: "", goal: "", startDate: "", endDate: "", status: "PLANNED", memberIds: [] };
  availablePersonnel.value = [];
  formError.value = null;
  showModal.value = true;
}

async function onProjectChange() {
  form.value.memberIds = [];
  availablePersonnel.value = [];
  if (!form.value.projectId) return;

  try {
    // Find the vendor for the selected project
    const proj = projects.value.find(p => p.id === form.value.projectId);
    const vendorId = proj?.vendorId ?? user?.vendorId;
    if (!vendorId) return;

    // Load all users for this vendor (PERSONNEL only)
    const usersRes = await http.get(`/api/users?vendorId=${vendorId}`);
    const personnel = usersRes.data.filter(u => u.userType === "PERSONNEL");

    // Load active sprints to know who's already assigned
    const sprintsRes = await http.get(`/api/sprints?projectId=${form.value.projectId}`);
    const activeSprints = sprintsRes.data.filter(s => s.status === "ACTIVE");
    const assignedIds = new Set(activeSprints.flatMap(s => (s.members || []).map(m => m.id)));

    availablePersonnel.value = personnel.map(p => ({
      id: p.id,
      fullName: p.personnelName + " " + p.personnelSurname,
      unavailable: assignedIds.has(p.id)
    }));
  } catch (e) { console.error(e); }
}

async function createSprint() {
  if (!form.value.projectId || !form.value.sprintName || !form.value.startDate || !form.value.endDate) {
    formError.value = "Project, name, and dates are required.";
    return;
  }
  saving.value = true;
  formError.value = null;
  try {
    await http.post("/api/sprints", {
      projectId: Number(form.value.projectId),
      sprintName: form.value.sprintName,
      goal: form.value.goal,
      startDate: form.value.startDate,
      endDate: form.value.endDate,
      status: form.value.status,
      memberIds: form.value.memberIds
    });
    showModal.value = false;
    await fetchSprints();
  } catch (e) {
    formError.value = e?.response?.data?.message ?? "Failed to create sprint.";
  } finally {
    saving.value = false;
  }
}

async function openSprintDetail(sprint) {
  detailSprint.value = sprint;
  sprintTasks.value = [];
  loadingTasks.value = true;
  try {
    const r = await http.get(`/api/assignments?sprintId=${sprint.id}`);
    sprintTasks.value = r.data;
  } catch (e) { console.error(e); }
  finally { loadingTasks.value = false; }
}

onMounted(async () => {
  try {
    const vendorId = user?.vendorId;
    const [sRes, pRes] = await Promise.all([
      http.get("/api/sprints"),
      http.get(vendorId ? `/api/projects?vendorId=${vendorId}` : "/api/projects"),
    ]);
    // Filter sprints to this vendor's projects
    const projectIds = new Set(pRes.data.map(p => p.id));
    sprints.value = sRes.data.filter(s => projectIds.has(s.projectId));
    projects.value = pRes.data;
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});

async function fetchSprints() {
  try {
    const vendorId = user?.vendorId;
    let url = filterProject.value ? `/api/sprints?projectId=${filterProject.value}` : "/api/sprints";
    const r = await http.get(url);
    if (filterProject.value) {
      sprints.value = r.data;
    } else {
      const projectIds = new Set(projects.value.map(p => p.id));
      sprints.value = r.data.filter(s => projectIds.has(s.projectId));
    }
  } catch (e) { console.error(e); }
}
</script>

<style scoped>
.vendor-sprints { display:flex; flex-direction:column; gap:24px; }

.toolbar { display:flex; align-items:center; gap:12px; }
.toolbar select { background:rgba(255,255,255,0.06); border:1px solid rgba(255,255,255,0.12); border-radius:10px; padding:9px 14px; color:#e2eaff; font-size:13px; outline:none; cursor:pointer; }
.toolbar select option { background:#0f1a2e; }
.spacer { flex:1; }

.sprint-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(300px,1fr)); gap:16px; }
.sprint-card { background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:16px; padding:20px; display:flex; flex-direction:column; gap:10px; cursor:pointer; transition:border-color 0.2s,background 0.2s; position:relative; }
.sprint-card:hover { border-color:rgba(99,102,241,0.3); background:rgba(99,102,241,0.04); }
.sprint-top { display:flex; align-items:flex-start; justify-content:space-between; gap:10px; }
.sprint-name { font-size:15px; font-weight:700; color:#e2eaff; }
.sprint-project { font-size:12px; color:#93c5fd; margin-top:3px; font-weight:600; }
.sprint-goal { font-size:13px; color:rgba(200,215,255,0.6); line-height:1.6; }
.sprint-dates { font-size:12px; color:rgba(200,215,255,0.4); font-weight:500; }
.dbl-hint { position:absolute; bottom:8px; right:12px; font-size:10px; color:rgba(200,215,255,0.2); }

/* Member avatars on card */
.members-row { display:flex; flex-wrap:wrap; gap:6px; }
.member-chip { width:28px; height:28px; border-radius:50%; background:linear-gradient(135deg,#6366f1,#a855f7); display:grid; place-items:center; font-size:10px; font-weight:700; color:#fff; flex-shrink:0; }
.members-empty { font-size:11px; color:rgba(200,215,255,0.25); }

.badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; white-space:nowrap; }
.badge.green { background:rgba(34,197,94,0.15); color:#86efac; }
.badge.amber { background:rgba(251,191,36,0.15); color:#fde68a; }
.badge.gray { background:rgba(148,163,184,0.12); color:#94a3b8; }

/* Modal */
.modal-backdrop { position:fixed; inset:0; background:rgba(0,0,0,0.6); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; z-index:100; }
.modal { background:#0f1a2e; border:1px solid rgba(255,255,255,0.12); border-radius:20px; padding:28px; width:min(540px,95vw); display:flex; flex-direction:column; gap:16px; max-height:90vh; overflow-y:auto; }
.modal-header { display:flex; align-items:center; justify-content:space-between; }
.modal-header h3 { margin:0; font-size:16px; font-weight:800; color:#f3f7ff; }
.close-btn { background:transparent; border:none; color:rgba(200,215,255,0.5); font-size:18px; cursor:pointer; }
.close-btn:hover { color:#e2eaff; }

.form-group { display:flex; flex-direction:column; gap:7px; }
.form-group label { font-size:12px; font-weight:600; color:rgba(200,215,255,0.6); text-transform:uppercase; letter-spacing:0.07em; }
.form-group input, .form-group select, .form-group textarea { background:rgba(255,255,255,0.06); border:1px solid rgba(255,255,255,0.1); border-radius:10px; padding:10px 14px; color:#e2eaff; font-size:13px; outline:none; resize:vertical; }
.form-group input:focus, .form-group select:focus, .form-group textarea:focus { border-color:rgba(99,102,241,0.5); }
.form-group select option { background:#0f1a2e; }
.form-row { display:grid; grid-template-columns:1fr 1fr; gap:12px; }

/* Developer picker */
.dev-picker { display:flex; flex-direction:column; gap:6px; max-height:200px; overflow-y:auto; padding:4px 0; }
.dev-option { display:flex; align-items:center; gap:10px; padding:8px 10px; border-radius:10px; cursor:pointer; transition:background 0.15s; }
.dev-option:hover:not(.disabled) { background:rgba(99,102,241,0.08); }
.dev-option.selected { background:rgba(99,102,241,0.12); }
.dev-option.disabled { opacity:0.5; cursor:default; }
.dev-option input[type="checkbox"] { accent-color:#6366f1; width:15px; height:15px; flex-shrink:0; }
.dev-avatar { width:26px; height:26px; border-radius:50%; background:linear-gradient(135deg,#6366f1,#a855f7); display:grid; place-items:center; font-size:10px; font-weight:700; color:#fff; flex-shrink:0; }
.dev-name { font-size:13px; color:#e2eaff; font-weight:500; flex:1; }
.dev-tag { font-size:10px; color:#fde68a; background:rgba(251,191,36,0.12); border-radius:999px; padding:2px 8px; }
.dev-empty { font-size:13px; color:rgba(200,215,255,0.35); padding:8px 0; }

.modal-actions { display:flex; gap:10px; justify-content:flex-end; }
.primary-btn { background:linear-gradient(135deg,#3b82f6,#6366f1); color:#fff; border:none; border-radius:10px; padding:10px 20px; font-size:13px; font-weight:600; cursor:pointer; }
.primary-btn:disabled { opacity:0.5; cursor:default; }
.secondary-btn { background:rgba(255,255,255,0.06); color:#e2eaff; border:1px solid rgba(255,255,255,0.1); border-radius:10px; padding:10px 20px; font-size:13px; font-weight:600; cursor:pointer; }
.form-error { color:#fca5a5; font-size:13px; background:rgba(239,68,68,0.08); border-radius:8px; padding:8px 12px; }

/* Detail modal */
.detail-modal { background:#0f1a2e; border:1px solid rgba(255,255,255,0.12); border-radius:20px; padding:28px; width:min(720px,95vw); display:flex; flex-direction:column; gap:20px; max-height:85vh; overflow-y:auto; position:relative; }
.detail-header { padding-right:36px; }
.detail-title-block { flex:1; min-width:0; }
.detail-meta { display:flex; align-items:center; gap:10px; margin-top:6px; }
.detail-header h3 { margin:0; font-size:18px; font-weight:800; color:#f3f7ff; }
.detail-meta p { margin:0; font-size:12px; color:rgba(200,215,255,0.5); }
.detail-goal { font-size:13px; color:rgba(200,215,255,0.6); line-height:1.6; padding:12px; background:rgba(255,255,255,0.03); border-radius:10px; }
.detail-section { border-top:1px solid rgba(255,255,255,0.07); padding-top:16px; }
.detail-section-title { font-size:12px; font-weight:700; color:rgba(200,215,255,0.5); text-transform:uppercase; letter-spacing:0.08em; margin-bottom:12px; }

.member-chips { display:flex; flex-wrap:wrap; gap:8px; }
.member-chip-lg { padding:4px 12px; background:rgba(99,102,241,0.12); border:1px solid rgba(99,102,241,0.25); border-radius:999px; font-size:12px; color:#a5b4fc; font-weight:500; }

.tasks-table { width:100%; border-collapse:collapse; font-size:13px; }
.tasks-table th { text-align:left; padding:9px 12px; color:rgba(200,215,255,0.5); font-weight:600; border-bottom:1px solid rgba(255,255,255,0.07); white-space:nowrap; }
.tasks-table td { padding:9px 12px; color:#d1deff; border-bottom:1px solid rgba(255,255,255,0.04); }
.tasks-table tr:last-child td { border-bottom:none; }
.tasks-table tr:hover td { background:rgba(255,255,255,0.03); }
.status-badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:10px; font-weight:600; }
.status-badge.green { background:rgba(34,197,94,0.15); color:#86efac; }
.status-badge.amber { background:rgba(251,191,36,0.15); color:#fde68a; }
.status-badge.gray { background:rgba(148,163,184,0.12); color:#94a3b8; }

.loading-text, .empty-state, .empty-text { color:rgba(200,215,255,0.4); font-size:14px; text-align:center; padding:40px; }
</style>
