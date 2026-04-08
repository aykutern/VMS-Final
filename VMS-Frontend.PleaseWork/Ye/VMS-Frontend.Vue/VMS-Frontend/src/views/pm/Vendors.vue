<template>
  <div class="vendors-page">
    <!-- Tab Bar + Action -->
    <div class="tab-bar">
      <button :class="['tab-btn', activeTab === 'vendors' ? 'active' : '']" @click="activeTab = 'vendors'">Vendors</button>
      <button :class="['tab-btn', activeTab === 'performance' ? 'active' : '']" @click="activeTab = 'performance'; loadPerformance()">Vendor Performance</button>
      <div class="tab-bar-spacer"></div>
      <button v-if="activeTab === 'vendors'" class="primary-btn" @click="showModal = true">+ Add Vendor</button>
    </div>

    <!-- ═══ VENDORS TAB ═══ -->
    <template v-if="activeTab === 'vendors'">

      <div v-if="loading" class="loading-text">Loading vendors…</div>
      <div v-else class="vendor-grid">
        <div v-for="v in vendors" :key="v.id" class="vendor-card"
          @dblclick="openDetail(v)"
          title="Double-click to view details"
        >
          <div class="vendor-card-top">
            <div class="vendor-avatar">{{ v.vendorName?.[0] ?? 'V' }}</div>
            <div class="vendor-info">
              <div class="vendor-name">{{ v.vendorName }}</div>
              <div class="vendor-projects">{{ projectCount(v.id) }} project(s)</div>
            </div>
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

      <!-- Vendor Detail Popup -->
      <div v-if="detailVendor" class="modal-backdrop" @click.self="closeDetail">
        <div class="detail-modal">
          <button class="detail-close-btn" @click="closeDetail">✕</button>

          <!-- Header: matches sprint detail style -->
          <div class="detail-header">
            <div class="detail-title-block">
              <h3>{{ detailVendor.vendorName }}</h3>
              <div class="detail-meta">
                <span class="meta-badge">Vendor</span>
                <span class="meta-text">{{ projectCount(detailVendor.id) }} project(s) assigned</span>
              </div>
            </div>
          </div>

          <!-- Projects: shown like a goal block -->
          <div class="detail-projects-block" v-if="projectsByVendor(detailVendor.id).length > 0">
            <span v-for="p in projectsByVendor(detailVendor.id)" :key="p.id" class="project-chip">{{ p.projectName }}</span>
          </div>
          <div class="detail-projects-block empty-inline" v-else>No projects assigned.</div>

          <!-- Personnel table: matches tasks table style -->
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
                    <td class="name-cell">{{ e.personnelName }} {{ e.personnelSurname }}</td>
                    <td>{{ e.username }}</td>
                    <td class="email-cell">{{ e.email }}</td>
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
    </template>

    <!-- ═══ VENDOR PERFORMANCE TAB ═══ -->
    <template v-if="activeTab === 'performance'">

      <div v-if="loadingPerf" class="loading-text">Loading performance data…</div>
      <div v-else>
        <table class="perf-table" v-if="perfData.length > 0">
          <thead>
            <tr>
              <th>Vendor</th>
              <th>Completed Sprints</th>
              <th>Total Sprints</th>
              <th>Completed Tasks</th>
              <th>Total Tasks</th>
              <th>Completion Rate</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in perfData" :key="v.vendorId">
              <td>
                <div class="perf-vendor">
                  <div class="perf-avatar">{{ v.vendorName?.[0] }}</div>
                  {{ v.vendorName }}
                </div>
              </td>
              <td>{{ v.completedSprints }}</td>
              <td>{{ v.totalSprints }}</td>
              <td>{{ v.completedTasks }}</td>
              <td>{{ v.totalTasks }}</td>
              <td>
                <div class="rate-bar-wrap">
                  <div class="rate-bar">
                    <div class="rate-fill" :style="{ width: completionRate(v) + '%' }"></div>
                  </div>
                  <span class="rate-label">{{ completionRate(v) }}%</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state">No performance data available.</div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { http } from "../../lib/http.js";

const activeTab = ref("vendors");

// Vendors tab state
const vendors = ref([]);
const projects = ref([]);
const loading = ref(true);
const showModal = ref(false);
const saving = ref(false);
const formError = ref(null);
const form = ref({ vendorName: "" });
const detailVendor = ref(null);
const detailEmployees = ref([]);
const loadingEmployees = ref(false);

// Performance tab state
const perfData = ref([]);
const loadingPerf = ref(false);

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

async function loadPerformance() {
  if (perfData.value.length > 0) return; // already loaded
  loadingPerf.value = true;
  try {
    const r = await http.get("/api/dashboard/vendor-performance");
    perfData.value = r.data;
  } catch (e) { console.error(e); }
  finally { loadingPerf.value = false; }
}

function completionRate(v) {
  if (v.totalTasks === 0) return 0;
  return Math.round((v.completedTasks / v.totalTasks) * 100);
}
</script>

<style scoped>
.vendors-page { display:flex; flex-direction:column; gap:24px; }

/* Tab bar */
.tab-bar { display:flex; align-items:center; gap:6px; background:#f1f5f9; border:1px solid #e2e8f0; border-radius:10px; padding:4px; }
.tab-bar-spacer { flex:1; }
.tab-btn { padding:8px 20px; border:none; border-radius:8px; background:transparent; color:#64748b; font-size:13px; font-weight:600; cursor:pointer; transition:all 0.2s; }
.tab-btn.active { background:#2563eb; color:#fff; box-shadow:0 1px 3px rgba(37,99,235,0.3); }
.tab-btn:not(.active):hover { color:#1e293b; background:#e2e8f0; }

.page-header { display:flex; align-items:center; justify-content:space-between; }
.page-header h2 { margin:0; font-size:20px; font-weight:800; color:#0f172a; }
.vendor-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(300px,1fr)); gap:16px; }
.vendor-card { background:#ffffff; border:1px solid #e2e8f0; border-radius:12px; padding:20px; display:flex; flex-direction:column; gap:14px; cursor:pointer; transition:border-color 0.2s,box-shadow 0.2s; position:relative; box-shadow:0 1px 3px rgba(0,0,0,0.04); }
.vendor-card:hover { border-color:#a78bfa; box-shadow:0 4px 12px rgba(124,58,237,0.1); }
.vendor-card-top { display:flex; align-items:center; gap:14px; min-width:0; }
.vendor-info { min-width:0; flex:1; }
.vendor-avatar { width:48px; height:48px; border-radius:12px; background:linear-gradient(135deg,#6366f1,#a855f7); display:grid; place-items:center; font-size:20px; font-weight:800; color:#fff; flex-shrink:0; }
.vendor-name { font-size:16px; font-weight:700; color:#0f172a; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.vendor-projects { font-size:12px; color:#64748b; margin-top:3px; }
.project-list { display:flex; flex-wrap:wrap; gap:6px; }
.project-chip { padding:4px 12px; background:#eff6ff; border:1px solid #bfdbfe; border-radius:999px; font-size:12px; color:#2563eb; font-weight:500; }
.dbl-hint { position:absolute; bottom:10px; right:12px; font-size:10px; color:#cbd5e1; }

/* Detail modal — matches sprint detail style */
.detail-modal { background:#ffffff; border:1px solid #e2e8f0; border-radius:16px; padding:28px; width:min(680px,95vw); display:flex; flex-direction:column; gap:20px; max-height:85vh; overflow-y:auto; position:relative; box-shadow:0 20px 40px rgba(0,0,0,0.15); }
.detail-close-btn { position:absolute; top:16px; right:16px; background:transparent; border:none; color:#94a3b8; font-size:20px; cursor:pointer; line-height:1; z-index:1; }
.detail-close-btn:hover { color:#475569; }
.detail-header { padding-right:36px; }
.detail-title-block { flex:1; min-width:0; }
.detail-header h3 { margin:0; font-size:20px; font-weight:800; color:#0f172a; }
.detail-meta { display:flex; align-items:center; gap:10px; margin-top:6px; }
.meta-badge { display:inline-flex; align-items:center; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:700; text-transform:uppercase; letter-spacing:0.05em; background:linear-gradient(135deg,#6366f1,#a855f7); color:#fff; }
.meta-text { font-size:12px; color:#64748b; }
.detail-projects-block { display:flex; flex-wrap:wrap; gap:8px; padding:14px 16px; background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; }
.detail-projects-block.empty-inline { color:#94a3b8; font-size:13px; display:block; }
.detail-section { border-top:1px solid #f1f5f9; padding-top:16px; }
.detail-section-title { font-size:12px; font-weight:700; color:#64748b; text-transform:uppercase; letter-spacing:0.08em; margin-bottom:12px; }
.empty-inline { font-size:13px; color:#94a3b8; }

.personnel-table-wrap { overflow-x:auto; }
.personnel-table { width:100%; border-collapse:collapse; font-size:13px; }
.personnel-table th { text-align:left; padding:10px 12px; color:#64748b; font-weight:600; border-bottom:1px solid #e2e8f0; white-space:nowrap; font-size:11px; text-transform:uppercase; letter-spacing:0.05em; }
.personnel-table td { padding:10px 12px; color:#1e293b; border-bottom:1px solid #f1f5f9; }
.personnel-table tr:last-child td { border-bottom:none; }
.personnel-table tr:hover td { background:#f8fafc; }
.name-cell { font-weight:600; color:#0f172a; }
.email-cell { color:#64748b; font-size:12px; }
.role-badge { display:inline-block; padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; }
.role-badge.blue { background:#eff6ff; color:#2563eb; border:1px solid #bfdbfe; }
.role-badge.purple { background:#f5f3ff; color:#7c3aed; border:1px solid #ddd6fe; }

/* Performance table */
.perf-table { width:100%; border-collapse:collapse; font-size:13px; background:#ffffff; border:1px solid #e2e8f0; border-radius:12px; overflow:hidden; box-shadow:0 1px 3px rgba(0,0,0,0.06); }
.perf-table th { text-align:left; padding:14px 16px; color:#64748b; font-weight:600; border-bottom:1px solid #e2e8f0; white-space:nowrap; background:#f8fafc; }
.perf-table td { padding:14px 16px; color:#1e293b; border-bottom:1px solid #f1f5f9; }
.perf-table tr:last-child td { border-bottom:none; }
.perf-table tr:hover td { background:#f8fafc; }
.perf-vendor { display:flex; align-items:center; gap:10px; font-weight:600; color:#0f172a; }
.perf-avatar { width:32px; height:32px; border-radius:10px; background:linear-gradient(135deg,#6366f1,#a855f7); display:grid; place-items:center; font-size:14px; font-weight:800; color:#fff; flex-shrink:0; }
.rate-bar-wrap { display:flex; align-items:center; gap:8px; }
.rate-bar { width:80px; height:6px; background:#e2e8f0; border-radius:999px; overflow:hidden; }
.rate-fill { height:100%; background:linear-gradient(90deg,#22c55e,#16a34a); border-radius:999px; transition:width 0.3s; }
.rate-label { font-size:12px; color:#16a34a; font-weight:600; }

.primary-btn { padding:10px 20px; background:#2563eb; color:#fff; border:none; border-radius:8px; font-weight:700; font-size:14px; cursor:pointer; transition:background 0.15s; }
.primary-btn:hover { background:#1d4ed8; }
.primary-btn:disabled { opacity:0.5; cursor:not-allowed; }
.secondary-btn { padding:9px 16px; background:#f1f5f9; color:#475569; border:1px solid #e2e8f0; border-radius:8px; font-weight:600; font-size:14px; cursor:pointer; }
.secondary-btn:hover { background:#e2e8f0; }
.modal-backdrop { position:fixed; inset:0; background:rgba(15,23,42,0.5); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; z-index:100; }
.modal { background:#ffffff; border:1px solid #e2e8f0; border-radius:16px; padding:28px; width:min(420px,90vw); display:flex; flex-direction:column; gap:16px; box-shadow:0 20px 40px rgba(0,0,0,0.15); }
.modal-header { display:flex; align-items:center; justify-content:space-between; }
.modal-header h3 { margin:0; font-size:17px; font-weight:800; color:#0f172a; }
.form-group { display:flex; flex-direction:column; gap:8px; }
.form-group label { font-size:13px; color:#475569; font-weight:600; }
.form-group input { background:#f8fafc; border:1px solid #cbd5e1; border-radius:8px; padding:10px 14px; color:#0f172a; font-size:14px; outline:none; }
.form-group input:focus { border-color:#2563eb; box-shadow:0 0 0 3px rgba(37,99,235,0.1); }
.modal-actions { display:flex; gap:10px; justify-content:flex-end; }
.form-error { color:#dc2626; font-size:13px; }
.loading-text,.empty-state,.empty-text { color:#94a3b8; font-size:14px; text-align:center; padding:20px; }
</style>
