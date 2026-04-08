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

        <!-- Capacity bar -->
        <div class="capacity-bar-wrap" v-if="s.maxCapacity">
          <div class="capacity-label">{{ s.currentLoad || 0 }} / {{ s.maxCapacity }} pts</div>
          <div class="capacity-bar">
            <div class="capacity-fill" :style="{ width: Math.min(100, ((s.currentLoad || 0) / s.maxCapacity) * 100) + '%' }"></div>
          </div>
        </div>

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
            <input v-model="form.startDate" type="date" @change="autoEndDate" />
          </div>
          <div class="form-group">
            <label>End Date (auto: 2 weeks)</label>
            <input v-model="form.endDate" type="date" disabled />
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
              <tr><th>Task</th><th>Assignee</th><th>Status</th><th>Priority</th><th>Rank</th><th>Completed</th></tr>
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
                <td><span :class="['rank-badge', 'rank-' + t.rank]">★ {{ t.rank }}</span></td>
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

function autoEndDate() {
  if (form.value.startDate) {
    const start = new Date(form.value.startDate);
    start.setDate(start.getDate() + 13);
    form.value.endDate = start.toISOString().split('T')[0];
  }
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
/* ── Layout ─────────────────────────────────────── */
.vendor-sprints { display:flex; flex-direction:column; gap:24px; }

/* ── Toolbar ─────────────────────────────────────── */
.toolbar { display:flex; align-items:center; gap:12px; }
.toolbar select {
  background:#ffffff;
  border:1px solid #cbd5e1;
  border-radius:8px;
  padding:9px 14px;
  color:#1e293b;
  font-size:13px;
  outline:none;
  cursor:pointer;
}
.spacer { flex:1; }

/* ── Sprint Cards ─────────────────────────────────── */
.sprint-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(300px,1fr)); gap:16px; }
.sprint-card {
  background:#ffffff;
  border:1px solid #e2e8f0;
  border-radius:12px;
  padding:20px;
  display:flex;
  flex-direction:column;
  gap:10px;
  cursor:pointer;
  transition:border-color 0.2s, box-shadow 0.2s;
  position:relative;
  box-shadow:0 1px 3px rgba(0,0,0,0.04);
}
.sprint-card:hover { border-color:#a78bfa; box-shadow:0 4px 12px rgba(99,102,241,0.1); }
.sprint-top { display:flex; align-items:flex-start; justify-content:space-between; gap:10px; }
.sprint-name { font-size:15px; font-weight:700; color:#0f172a; }
.sprint-project { font-size:12px; color:#6366f1; margin-top:3px; font-weight:600; }
.sprint-goal { font-size:13px; color:#64748b; line-height:1.6; }
.sprint-dates { font-size:12px; color:#94a3b8; font-weight:500; }
.dbl-hint { position:absolute; bottom:8px; right:12px; font-size:10px; color:#cbd5e1; }

/* ── Member avatars on card ───────────────────────── */
.members-row { display:flex; flex-wrap:wrap; gap:6px; }
.member-chip {
  width:28px; height:28px; border-radius:50%;
  background:linear-gradient(135deg,#6366f1,#a855f7);
  display:grid; place-items:center;
  font-size:10px; font-weight:700; color:#fff; flex-shrink:0;
}
.members-empty { font-size:11px; color:#cbd5e1; }

/* ── Status Badges ────────────────────────────────── */
.badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; white-space:nowrap; }
.badge.green { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }
.badge.amber { background:#fffbeb; color:#d97706; border:1px solid #fde68a; }
.badge.gray  { background:#f1f5f9; color:#64748b; border:1px solid #e2e8f0; }

/* ── Capacity Bar ─────────────────────────────────── */
.capacity-bar-wrap { display:flex; align-items:center; gap:8px; }
.capacity-label { font-size:11px; font-weight:600; color:#64748b; white-space:nowrap; }
.capacity-bar { flex:1; height:6px; background:#e2e8f0; border-radius:999px; overflow:hidden; }
.capacity-fill { height:100%; background:linear-gradient(90deg,#6366f1,#a855f7); border-radius:999px; transition:width 0.3s; }

/* ── Modal Backdrop ───────────────────────────────── */
.modal-backdrop {
  position:fixed; inset:0;
  background:rgba(15,23,42,0.5);
  backdrop-filter:blur(4px);
  display:flex; align-items:center; justify-content:center;
  z-index:100;
}

/* ── Create Sprint Modal ──────────────────────────── */
.modal {
  background:#ffffff;
  border:1px solid #e2e8f0;
  border-radius:16px;
  padding:28px;
  width:min(540px,95vw);
  display:flex; flex-direction:column; gap:16px;
  max-height:90vh; overflow-y:auto;
  box-shadow:0 20px 60px rgba(0,0,0,0.12);
}
.modal-header { display:flex; align-items:center; justify-content:space-between; }
.modal-header h3 { margin:0; font-size:16px; font-weight:800; color:#0f172a; }
.close-btn { background:transparent; border:none; color:#94a3b8; font-size:18px; cursor:pointer; }
.close-btn:hover { color:#475569; }

.form-group { display:flex; flex-direction:column; gap:7px; }
.form-group label { font-size:12px; font-weight:600; color:#475569; text-transform:uppercase; letter-spacing:0.07em; }
.form-group input,
.form-group select,
.form-group textarea {
  background:#f8fafc;
  border:1px solid #e2e8f0;
  border-radius:8px;
  padding:10px 14px;
  color:#0f172a;
  font-size:13px;
  outline:none;
  resize:vertical;
}
.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus { border-color:#6366f1; box-shadow:0 0 0 3px rgba(99,102,241,0.1); }
.form-row { display:grid; grid-template-columns:1fr 1fr; gap:12px; }

/* ── Developer Picker ─────────────────────────────── */
.dev-picker { display:flex; flex-direction:column; gap:6px; max-height:200px; overflow-y:auto; padding:4px 0; }
.dev-option {
  display:flex; align-items:center; gap:10px;
  padding:8px 10px; border-radius:8px;
  cursor:pointer; transition:background 0.15s;
}
.dev-option:hover:not(.disabled) { background:#f5f3ff; }
.dev-option.selected { background:#ede9fe; }
.dev-option.disabled { opacity:0.5; cursor:default; }
.dev-option input[type="checkbox"] { accent-color:#6366f1; width:15px; height:15px; flex-shrink:0; }
.dev-avatar {
  width:26px; height:26px; border-radius:50%;
  background:linear-gradient(135deg,#6366f1,#a855f7);
  display:grid; place-items:center;
  font-size:10px; font-weight:700; color:#fff; flex-shrink:0;
}
.dev-name { font-size:13px; color:#0f172a; font-weight:500; flex:1; }
.dev-tag { font-size:10px; color:#d97706; background:#fffbeb; border:1px solid #fde68a; border-radius:999px; padding:2px 8px; }
.dev-empty { font-size:13px; color:#94a3b8; padding:8px 0; }

.modal-actions { display:flex; gap:10px; justify-content:flex-end; }
.primary-btn {
  background:linear-gradient(135deg,#3b82f6,#6366f1);
  color:#fff; border:none; border-radius:8px;
  padding:10px 20px; font-size:13px; font-weight:600; cursor:pointer;
  transition:opacity 0.15s;
}
.primary-btn:hover { opacity:0.9; }
.primary-btn:disabled { opacity:0.5; cursor:default; }
.secondary-btn {
  background:#f8fafc; color:#475569;
  border:1px solid #e2e8f0; border-radius:8px;
  padding:10px 20px; font-size:13px; font-weight:600; cursor:pointer;
}
.secondary-btn:hover { background:#f1f5f9; }
.form-error { color:#dc2626; font-size:13px; background:#fef2f2; border:1px solid #fecaca; border-radius:8px; padding:8px 12px; }

/* ── Detail Modal ─────────────────────────────────── */
.detail-modal {
  background:#ffffff;
  border:1px solid #e2e8f0;
  border-radius:16px;
  padding:28px;
  width:min(720px,95vw);
  display:flex; flex-direction:column; gap:20px;
  max-height:85vh; overflow-y:auto;
  position:relative;
  box-shadow:0 20px 60px rgba(0,0,0,0.12);
}
.detail-header { padding-right:36px; }
.detail-title-block { flex:1; min-width:0; }
.detail-meta { display:flex; align-items:center; gap:10px; margin-top:6px; }
.detail-header h3 { margin:0; font-size:18px; font-weight:800; color:#0f172a; }
.detail-meta p { margin:0; font-size:12px; color:#64748b; }
.detail-goal {
  font-size:13px; color:#475569; line-height:1.6;
  padding:12px; background:#f8fafc;
  border:1px solid #e2e8f0; border-radius:8px;
}
.detail-section { border-top:1px solid #f1f5f9; padding-top:16px; }
.detail-section-title {
  font-size:12px; font-weight:700; color:#94a3b8;
  text-transform:uppercase; letter-spacing:0.08em; margin-bottom:12px;
}

.member-chips { display:flex; flex-wrap:wrap; gap:8px; }
.member-chip-lg {
  padding:4px 12px;
  background:#ede9fe; border:1px solid #ddd6fe;
  border-radius:999px; font-size:12px; color:#7c3aed; font-weight:500;
}

/* ── Tasks Table ──────────────────────────────────── */
.tasks-table { width:100%; border-collapse:collapse; font-size:13px; }
.tasks-table th {
  text-align:left; padding:9px 12px;
  color:#64748b; font-weight:600;
  border-bottom:2px solid #f1f5f9; white-space:nowrap;
}
.tasks-table td {
  padding:9px 12px; color:#0f172a;
  border-bottom:1px solid #f1f5f9;
}
.tasks-table tr:last-child td { border-bottom:none; }
.tasks-table tr:hover td { background:#f8fafc; }

.status-badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:10px; font-weight:600; }
.status-badge.green { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }
.status-badge.amber { background:#fffbeb; color:#d97706; border:1px solid #fde68a; }
.status-badge.gray  { background:#f1f5f9; color:#64748b; border:1px solid #e2e8f0; }

/* ── Rank Badge ───────────────────────────────────── */
.rank-badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; background:#f5f3ff; color:#7c3aed; border:1px solid #ddd6fe; }
.rank-badge.rank-1 { background:#f0fdf4; color:#16a34a; border-color:#bbf7d0; }
.rank-badge.rank-2 { background:#eff6ff; color:#2563eb; border-color:#bfdbfe; }
.rank-badge.rank-3 { background:#fffbeb; color:#d97706; border-color:#fde68a; }
.rank-badge.rank-4 { background:#fff7ed; color:#ea580c; border-color:#fed7aa; }
.rank-badge.rank-5 { background:#fef2f2; color:#dc2626; border-color:#fecaca; }

/* ── States ───────────────────────────────────────── */
.loading-text, .empty-state, .empty-text { color:#94a3b8; font-size:14px; text-align:center; padding:40px; }
</style>
