<template>
  <div class="sprints-page">
    <div class="page-header">
      <h2>Sprint Management</h2>
      <button class="primary-btn" @click="openModal">+ New Sprint</button>
    </div>

    <div class="filters">
      <select v-model="filterProject" @change="filterSprints">
        <option value="">All Projects</option>
        <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.projectName }}</option>
      </select>
      <select v-model="filterStatus" @change="filterSprints">
        <option value="">All Statuses</option>
        <option>PLANNED</option>
        <option>ACTIVE</option>
        <option>COMPLETED</option>
      </select>
    </div>

    <div v-if="loading" class="loading-text">Loading sprints…</div>
    <div v-else class="sprint-grid">
      <div v-for="s in filteredSprints" :key="s.id" class="sprint-card">
        <div class="sprint-top">
          <div class="sprint-name">{{ s.sprintName }}</div>
          <span :class="['badge', s.status === 'ACTIVE' ? 'green' : s.status === 'COMPLETED' ? 'gray' : 'amber']">{{ s.status }}</span>
        </div>
        <div class="sprint-project">{{ s.projectName }}</div>
        <div class="sprint-goal">{{ s.goal }}</div>
        <div class="sprint-dates">
          <span>{{ s.startDate }}</span>
          <span>→</span>
          <span>{{ s.endDate }}</span>
        </div>
      </div>
      <div v-if="filteredSprints.length === 0" class="empty-state">No sprints found.</div>
    </div>

    <!-- New Sprint Modal -->
    <div v-if="showModal" class="modal-backdrop" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header"><h3>New Sprint</h3><button class="close-btn" @click="showModal = false">✕</button></div>
        <div class="form-group">
          <label>Project</label>
          <select v-model="form.projectId" @change="refreshDates">
            <option value="">— Select project —</option>
            <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.projectName }}</option>
          </select>
        </div>
        <div class="form-group"><label>Sprint Name</label><input v-model="form.sprintName" placeholder="Sprint 1 — Foundation" /></div>
        <div class="form-group"><label>Goal</label><textarea v-model="form.goal" rows="3" placeholder="What do you want to achieve?"></textarea></div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
          <div class="form-group"><label>Start Date</label><input v-model="form.startDate" type="date" /></div>
          <div class="form-group"><label>End Date</label><input v-model="form.endDate" type="date" /></div>
        </div>
        <div class="form-group">
          <label>Status</label>
          <select v-model="form.status">
            <option>PLANNED</option><option>ACTIVE</option>
          </select>
          <span v-if="form.status === 'ACTIVE' && hasActiveSprint(form.projectId)" class="warn-text">⚠ Existing active sprint will be set to PLANNED.</span>
        </div>
        <div class="modal-actions">
          <button class="secondary-btn" @click="showModal = false">Cancel</button>
          <button class="primary-btn" @click="createSprint" :disabled="saving">{{ saving ? 'Creating…' : 'Create Sprint' }}</button>
        </div>
        <div v-if="formError" class="form-error">{{ formError }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { http } from "../../lib/http.js";

const sprints = ref([]);
const projects = ref([]);
const loading = ref(true);
const showModal = ref(false);
const saving = ref(false);
const formError = ref(null);
const filterProject = ref("");
const filterStatus = ref("");

const form = ref({ projectId: "", sprintName: "", goal: "", startDate: "", endDate: "", status: "PLANNED" });

/** Get Monday of the week containing `date` */
function getWeekMonday(date) {
  const d = new Date(date);
  const day = d.getDay(); // 0=Sun
  const diff = day === 0 ? -6 : 1 - day;
  d.setDate(d.getDate() + diff);
  d.setHours(0, 0, 0, 0);
  return d;
}

/** Format Date as YYYY-MM-DD */
function toISO(d) { return d.toISOString().split('T')[0]; }

/** Compute start/end dates for a new sprint given project's sprints */
function computeDates(projectId) {
  const projectSprints = sprints.value.filter(s => s.projectId == projectId);
  const active = projectSprints.find(s => s.status === 'ACTIVE');
  let startMonday;
  if (active?.endDate) {
    // Start from Monday after active sprint ends
    const afterEnd = new Date(active.endDate);
    afterEnd.setDate(afterEnd.getDate() + 1);
    startMonday = getWeekMonday(afterEnd);
    // If that Monday is same or before end, go to next Monday
    if (startMonday <= new Date(active.endDate)) {
      startMonday.setDate(startMonday.getDate() + 7);
    }
  } else {
    startMonday = getWeekMonday(new Date());
  }
  const endFriday = new Date(startMonday);
  endFriday.setDate(endFriday.getDate() + 25); // Mon + 25 = Fri 3 weeks+1 later
  return { startDate: toISO(startMonday), endDate: toISO(endFriday) };
}

function hasActiveSprint(projectId) {
  return sprints.value.some(s => s.projectId == projectId && s.status === 'ACTIVE');
}

function refreshDates() {
  if (form.value.projectId) {
    const { startDate, endDate } = computeDates(form.value.projectId);
    form.value.startDate = startDate;
    form.value.endDate = endDate;
  }
}

function openModal() {
  form.value = { projectId: "", sprintName: "", goal: "", startDate: "", endDate: "", status: "PLANNED" };
  showModal.value = true;
}

const filteredSprints = computed(() => sprints.value.filter(s => {
  if (filterProject.value && s.projectId !== filterProject.value) return false;
  if (filterStatus.value && s.status !== filterStatus.value) return false;
  return true;
}));

onMounted(async () => {
  try {
    const [sRes, pRes] = await Promise.all([http.get("/api/sprints"), http.get("/api/projects")]);
    sprints.value = sRes.data;
    projects.value = pRes.data;
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});

async function createSprint() {
  if (!form.value.projectId || !form.value.sprintName) { formError.value = "Project and sprint name are required."; return; }
  saving.value = true; formError.value = null;
  try {
    await http.post("/api/sprints", { ...form.value, projectId: Number(form.value.projectId), status: form.value.status ?? "PLANNED" });
    const r = await http.get("/api/sprints");
    sprints.value = r.data;
    showModal.value = false;
    form.value = { projectId: "", sprintName: "", goal: "", startDate: "", endDate: "", status: "PLANNED" };
  } catch (e) { formError.value = e?.response?.data?.message ?? "Failed to create sprint."; }
  finally { saving.value = false; }
}

function filterSprints() {}
</script>

<style scoped>
.sprints-page { display:flex; flex-direction:column; gap:24px; }
.page-header { display:flex; align-items:center; justify-content:space-between; }
.page-header h2 { margin:0; font-size:20px; font-weight:800; color:#f3f7ff; }
.filters { display:flex; gap:12px; }
.filters select { background:rgba(255,255,255,0.06); border:1px solid rgba(255,255,255,0.12); border-radius:10px; padding:9px 14px; color:#e2eaff; font-size:13px; outline:none; cursor:pointer; }
.filters select option { background:#0f1a2e; }
.sprint-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(300px,1fr)); gap:16px; }
.sprint-card { background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:16px; padding:20px; display:flex; flex-direction:column; gap:10px; }
.sprint-top { display:flex; align-items:flex-start; justify-content:space-between; gap:8px; }
.sprint-name { font-size:15px; font-weight:700; color:#e2eaff; }
.sprint-project { font-size:12px; color:#93c5fd; font-weight:600; }
.sprint-goal { font-size:13px; color:rgba(200,215,255,0.6); line-height:1.6; }
.sprint-dates { display:flex; gap:8px; font-size:12px; color:rgba(200,215,255,0.4); font-weight:500; }
.badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; white-space:nowrap; }
.badge.green { background:rgba(34,197,94,0.15); color:#86efac; }
.badge.amber { background:rgba(251,191,36,0.15); color:#fde68a; }
.badge.gray { background:rgba(148,163,184,0.12); color:#94a3b8; }
.primary-btn { padding:10px 20px; background:linear-gradient(135deg,#3b82f6,#6366f1); color:#fff; border:none; border-radius:10px; font-weight:700; font-size:14px; cursor:pointer; }
.primary-btn:disabled { opacity:0.5; cursor:not-allowed; }
.secondary-btn { padding:9px 16px; background:rgba(255,255,255,0.07); color:#d1deff; border:1px solid rgba(255,255,255,0.1); border-radius:10px; font-weight:600; font-size:14px; cursor:pointer; }
.close-btn { background:transparent; border:none; color:rgba(200,215,255,0.5); font-size:18px; cursor:pointer; }
.modal-backdrop { position:fixed; inset:0; background:rgba(0,0,0,0.6); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; z-index:100; }
.modal { background:#0f1a2e; border:1px solid rgba(255,255,255,0.12); border-radius:20px; padding:28px; width:min(500px,90vw); display:flex; flex-direction:column; gap:16px; }
.modal-header { display:flex; align-items:center; justify-content:space-between; }
.modal-header h3 { margin:0; font-size:17px; font-weight:800; color:#f3f7ff; }
.form-group { display:flex; flex-direction:column; gap:8px; }
.form-group label { font-size:13px; color:rgba(200,215,255,0.7); font-weight:600; }
.form-group input,.form-group select,.form-group textarea { background:rgba(255,255,255,0.06); border:1px solid rgba(255,255,255,0.12); border-radius:10px; padding:10px 14px; color:#e2eaff; font-size:14px; outline:none; resize:vertical; }
.form-group select option { background:#0f1a2e; }
.modal-actions { display:flex; gap:10px; justify-content:flex-end; }
.form-error { color:#f87171; font-size:13px; }
.loading-text,.empty-state { color:rgba(200,215,255,0.4); font-size:14px; text-align:center; padding:40px; }
.warn-text { font-size:12px; color:#fbbf24; margin-top:4px; display:block; }
</style>
