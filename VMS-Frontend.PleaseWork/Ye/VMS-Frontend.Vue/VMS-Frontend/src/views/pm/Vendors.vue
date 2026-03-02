<template>
  <div class="vendors-page">
    <div class="page-header">
      <h2>Vendors</h2>
      <button class="primary-btn" @click="showModal = true">+ Add Vendor</button>
    </div>

    <div v-if="loading" class="loading-text">Loading vendors…</div>
    <div v-else class="vendor-grid">
      <div
        v-for="v in vendors"
        :key="v.id"
        class="vendor-card"
        @dblclick="openDetail(v)"
        title="Double-click to view details"
      >
        <div class="vendor-avatar">{{ v.vendorName?.[0] ?? 'V' }}</div>
        <div class="vendor-info">
          <div class="vendor-name">{{ v.vendorName }}</div>
          <div class="vendor-projects">{{ projectCount(v.id) }} project(s)</div>
        </div>
        <div class="project-list">
          <div v-for="p in projectsByVendor(v.id)" :key="p.id" class="project-chip">
            {{ p.projectName }}
          </div>
        </div>
        <div class="dbl-hint">Double-click for details</div>
      </div>
      <div v-if="vendors.length === 0" class="empty-state">No vendors yet.</div>
    </div>

    <!-- Add Vendor Modal -->
    <div v-if="showModal" class="modal-backdrop" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header"><h3>Add Vendor</h3><button class="close-btn" @click="showModal = false">✕</button></div>
        <div class="form-group"><label>Vendor Name</label><input v-model="form.vendorName" placeholder="e.g. TechCorp Solutions" /></div>
        <div class="modal-actions">
          <button class="secondary-btn" @click="showModal = false">Cancel</button>
          <button class="primary-btn" @click="createVendor" :disabled="saving">{{ saving ? 'Saving…' : 'Add Vendor' }}</button>
        </div>
        <div v-if="formError" class="form-error">{{ formError }}</div>
      </div>
    </div>

    <!-- Vendor Detail Popup (double-click) -->
    <div v-if="detailVendor" class="modal-backdrop" @click.self="closeDetail">
      <div class="detail-modal">
        <div class="detail-header">
          <div class="detail-avatar">{{ detailVendor.vendorName?.[0] }}</div>
          <div>
            <h3>{{ detailVendor.vendorName }}</h3>
            <p>{{ projectCount(detailVendor.id) }} project(s) assigned</p>
          </div>
          <button class="close-btn" @click="closeDetail">✕</button>
        </div>

        <!-- Projects section -->
        <div class="detail-section">
          <div class="detail-section-title">Projects</div>
          <div class="detail-chips">
            <span v-for="p in projectsByVendor(detailVendor.id)" :key="p.id" class="project-chip">{{ p.projectName }}</span>
            <span v-if="projectsByVendor(detailVendor.id).length === 0" class="empty-inline">No projects</span>
          </div>
        </div>

        <!-- Personnel section -->
        <div class="detail-section">
          <div class="detail-section-title">Personnel ({{ detailEmployees.length }})</div>
          <div v-if="loadingEmployees" class="loading-text">Loading…</div>
          <div v-else class="personnel-table-wrap">
            <table class="personnel-table" v-if="detailEmployees.length > 0">
              <thead>
                <tr><th>Name</th><th>Username</th><th>Email</th><th>Role</th></tr>
              </thead>
              <tbody>
                <tr v-for="e in detailEmployees" :key="e.id">
                  <td>{{ e.personnelName }} {{ e.personnelSurname }}</td>
                  <td>{{ e.username }}</td>
                  <td>{{ e.email }}</td>
                  <td>
                    <span :class="['role-badge', e.userType === 'VENDOR_ADMIN' ? 'purple' : 'blue']">
                      {{ e.userType === 'VENDOR_ADMIN' ? 'Admin' : 'Developer' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-text">No personnel assigned to this vendor.</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { http } from "../../lib/http.js";

const vendors = ref([]);
const projects = ref([]);
const loading = ref(true);
const showModal = ref(false);
const saving = ref(false);
const formError = ref(null);
const form = ref({ vendorName: "" });

// Detail popup state
const detailVendor = ref(null);
const detailEmployees = ref([]);
const loadingEmployees = ref(false);

onMounted(async () => {
  try {
    const [vRes, pRes] = await Promise.all([http.get("/api/vendors"), http.get("/api/projects")]);
    vendors.value = vRes.data;
    projects.value = pRes.data;
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});

function projectsByVendor(vendorId) { return projects.value.filter(p => p.vendorId === vendorId); }
function projectCount(vendorId) { return projectsByVendor(vendorId).length; }

async function openDetail(vendor) {
  detailVendor.value = vendor;
  detailEmployees.value = [];
  loadingEmployees.value = true;
  try {
    const r = await http.get(`/api/users?vendorId=${vendor.id}`);
    detailEmployees.value = r.data;
  } catch (e) { console.error(e); }
  finally { loadingEmployees.value = false; }
}

function closeDetail() {
  detailVendor.value = null;
  detailEmployees.value = [];
}

async function createVendor() {
  if (!form.value.vendorName) { formError.value = "Vendor name is required."; return; }
  saving.value = true; formError.value = null;
  try {
    await http.post("/api/vendors", { vendorName: form.value.vendorName });
    const r = await http.get("/api/vendors");
    vendors.value = r.data;
    showModal.value = false;
    form.value = { vendorName: "" };
  } catch (e) { formError.value = e?.response?.data?.message ?? "Failed to add vendor."; }
  finally { saving.value = false; }
}
</script>

<style scoped>
.vendors-page { display:flex; flex-direction:column; gap:24px; }
.page-header { display:flex; align-items:center; justify-content:space-between; }
.page-header h2 { margin:0; font-size:20px; font-weight:800; color:#f3f7ff; }
.vendor-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(300px,1fr)); gap:16px; }
.vendor-card { background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:16px; padding:20px; display:flex; flex-direction:column; gap:14px; cursor:pointer; transition:border-color 0.2s,background 0.2s; position:relative; }
.vendor-card:hover { border-color:rgba(168,85,247,0.4); background:rgba(168,85,247,0.04); }
.vendor-avatar { width:48px; height:48px; border-radius:14px; background:linear-gradient(135deg,#6366f1,#a855f7); display:grid; place-items:center; font-size:20px; font-weight:800; color:#fff; }
.vendor-name { font-size:16px; font-weight:700; color:#e2eaff; }
.vendor-projects { font-size:12px; color:rgba(200,215,255,0.5); margin-top:3px; }
.project-list { display:flex; flex-wrap:wrap; gap:6px; }
.project-chip { padding:4px 12px; background:rgba(59,130,246,0.1); border:1px solid rgba(59,130,246,0.2); border-radius:999px; font-size:12px; color:#93c5fd; font-weight:500; }
.dbl-hint { position:absolute; bottom:10px; right:12px; font-size:10px; color:rgba(200,215,255,0.25); }

/* Detail modal */
.detail-modal { background:#0f1a2e; border:1px solid rgba(255,255,255,0.12); border-radius:20px; padding:28px; width:min(640px,95vw); display:flex; flex-direction:column; gap:20px; max-height:85vh; overflow-y:auto; }
.detail-header { display:flex; align-items:center; gap:16px; }
.detail-avatar { width:52px; height:52px; border-radius:14px; background:linear-gradient(135deg,#6366f1,#a855f7); display:grid; place-items:center; font-size:22px; font-weight:800; color:#fff; flex-shrink:0; }
.detail-header h3 { margin:0; font-size:18px; font-weight:800; color:#f3f7ff; flex:1; }
.detail-header p { margin:4px 0 0; font-size:13px; color:rgba(200,215,255,0.5); }
.detail-section { border-top:1px solid rgba(255,255,255,0.07); padding-top:16px; }
.detail-section-title { font-size:12px; font-weight:700; color:rgba(200,215,255,0.5); text-transform:uppercase; letter-spacing:0.08em; margin-bottom:12px; }
.detail-chips { display:flex; flex-wrap:wrap; gap:6px; }
.empty-inline { font-size:13px; color:rgba(200,215,255,0.3); }

/* Personnel table */
.personnel-table-wrap { overflow-x:auto; }
.personnel-table { width:100%; border-collapse:collapse; font-size:13px; }
.personnel-table th { text-align:left; padding:9px 12px; color:rgba(200,215,255,0.5); font-weight:600; border-bottom:1px solid rgba(255,255,255,0.07); white-space:nowrap; }
.personnel-table td { padding:9px 12px; color:#d1deff; border-bottom:1px solid rgba(255,255,255,0.04); }
.personnel-table tr:last-child td { border-bottom:none; }
.role-badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; }
.role-badge.blue { background:rgba(59,130,246,0.15); color:#93c5fd; }
.role-badge.purple { background:rgba(168,85,247,0.15); color:#d8b4fe; }

.primary-btn { padding:10px 20px; background:linear-gradient(135deg,#3b82f6,#6366f1); color:#fff; border:none; border-radius:10px; font-weight:700; font-size:14px; cursor:pointer; }
.primary-btn:disabled { opacity:0.5; cursor:not-allowed; }
.secondary-btn { padding:9px 16px; background:rgba(255,255,255,0.07); color:#d1deff; border:1px solid rgba(255,255,255,0.1); border-radius:10px; font-weight:600; font-size:14px; cursor:pointer; }
.close-btn { background:transparent; border:none; color:rgba(200,215,255,0.5); font-size:18px; cursor:pointer; margin-left:auto; }
.modal-backdrop { position:fixed; inset:0; background:rgba(0,0,0,0.6); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; z-index:100; }
.modal { background:#0f1a2e; border:1px solid rgba(255,255,255,0.12); border-radius:20px; padding:28px; width:min(420px,90vw); display:flex; flex-direction:column; gap:16px; }
.modal-header { display:flex; align-items:center; justify-content:space-between; }
.modal-header h3 { margin:0; font-size:17px; font-weight:800; color:#f3f7ff; }
.form-group { display:flex; flex-direction:column; gap:8px; }
.form-group label { font-size:13px; color:rgba(200,215,255,0.7); font-weight:600; }
.form-group input { background:rgba(255,255,255,0.06); border:1px solid rgba(255,255,255,0.12); border-radius:10px; padding:10px 14px; color:#e2eaff; font-size:14px; outline:none; }
.modal-actions { display:flex; gap:10px; justify-content:flex-end; }
.form-error { color:#f87171; font-size:13px; }
.loading-text,.empty-state,.empty-text { color:rgba(200,215,255,0.4); font-size:14px; text-align:center; padding:20px; }
</style>
