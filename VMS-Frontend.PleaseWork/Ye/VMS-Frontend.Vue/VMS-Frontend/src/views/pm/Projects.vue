<template>
  <div class="projects-page">
    <!-- Header -->
    <div class="page-header">
      <button class="primary-btn" @click="showModal = true">+ New Project</button>
    </div>

    <!-- Projects Grid -->
    <div v-if="loading" class="loading-text">Loading projects…</div>
    <div v-else class="project-grid">
      <div v-for="p in projects" :key="p.id" class="project-card" @click="$router.push('/pm/projects/' + p.id)">
        <div class="project-card-header">
          <div class="project-avatar">{{ p.projectName?.[0] ?? 'P' }}</div>
          <div class="project-info">
            <div class="project-name">{{ p.projectName }}</div>
            <div class="project-vendor">{{ p.vendorName ?? 'No vendor' }}</div>
          </div>
        </div>
        <div class="project-footer">
          <span class="badge active">Active</span>
          <span class="pm-name">PM: {{ p.projectManagerName ?? '—' }}</span>
        </div>
      </div>

      <div v-if="projects.length === 0" class="empty-state">
        <p>No projects yet. Create your first project!</p>
      </div>
    </div>

    <!-- Create Project Modal -->
    <div v-if="showModal" class="modal-backdrop" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>New Project</h3>
          <button class="close-btn" @click="showModal = false">✕</button>
        </div>
        <div class="form-group">
          <label>Project Name</label>
          <input v-model="form.projectName" placeholder="e.g. E-Commerce Platform" />
        </div>
        <div class="form-group">
          <label>Vendor</label>
          <select v-model="form.vendorId">
            <option value="">— Select vendor —</option>
            <option v-for="v in vendors" :key="v.id" :value="v.id">{{ v.vendorName }}</option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="secondary-btn" @click="showModal = false">Cancel</button>
          <button class="primary-btn" @click="createProject" :disabled="saving">
            {{ saving ? 'Creating…' : 'Create Project' }}
          </button>
        </div>
        <div v-if="formError" class="form-error">{{ formError }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { http } from "../../lib/http.js";
import { getCurrentUser } from "../../services/authService.js";

const projects = ref([]);
const vendors = ref([]);
const loading = ref(true);
const showModal = ref(false);
const saving = ref(false);
const formError = ref(null);
const user = getCurrentUser();

const form = ref({ projectName: "", vendorId: "" });

onMounted(async () => {
  try {
    const [pRes, vRes] = await Promise.all([http.get("/api/projects"), http.get("/api/vendors")]);
    projects.value = pRes.data;
    vendors.value = vRes.data;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
});

async function createProject() {
  if (!form.value.projectName) { formError.value = "Project name is required."; return; }
  saving.value = true;
  formError.value = null;
  try {
    await http.post("/api/projects", {
      projectName: form.value.projectName,
      vendorId: form.value.vendorId || null,
      projectManagerId: user?.id,
    });
    const res = await http.get("/api/projects");
    projects.value = res.data;
    showModal.value = false;
    form.value = { projectName: "", vendorId: "" };
  } catch (e) {
    formError.value = e?.response?.data?.message ?? "Failed to create project.";
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.projects-page { display: flex; flex-direction: column; gap: 24px; }

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-header h2 { margin: 0; font-size: 20px; font-weight: 800; color: #0f172a; }

.project-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }

.project-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.project-card:hover { border-color: #a78bfa; box-shadow: 0 4px 12px rgba(99,102,241,0.1); }

.project-card-header { display: flex; align-items: center; gap: 14px; min-width: 0; }

.project-avatar {
  width: 44px; height: 44px;
  border-radius: 10px;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  display: grid;
  place-items: center;
  font-size: 18px;
  font-weight: 800;
  color: #fff;
  flex-shrink: 0;
}

.project-info { min-width: 0; flex: 1; }
.project-name { font-size: 15px; font-weight: 700; color: #0f172a; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.project-vendor { font-size: 12px; color: #64748b; margin-top: 3px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.project-footer { display: flex; align-items: center; justify-content: space-between; }
.pm-name { font-size: 12px; color: #94a3b8; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 11px; font-weight: 600; }
.badge.active { background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }

.primary-btn {
  padding: 10px 20px;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.15s;
}
.primary-btn:hover { background: #1d4ed8; }
.primary-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.secondary-btn {
  padding: 10px 18px;
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
}
.secondary-btn:hover { background: #e2e8f0; }

.modal-backdrop {
  position: fixed; inset: 0;
  background: rgba(15,23,42,0.5);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  z-index: 100;
}
.modal {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 28px;
  width: min(460px, 90vw);
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
}
.modal-header { display: flex; align-items: center; justify-content: space-between; }
.modal-header h3 { margin: 0; font-size: 17px; font-weight: 800; color: #0f172a; }
.close-btn { background: transparent; border: none; color: #94a3b8; font-size: 18px; cursor: pointer; }
.close-btn:hover { color: #475569; }

.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 13px; color: #475569; font-weight: 600; }
.form-group input, .form-group select {
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 10px 14px;
  color: #0f172a;
  font-size: 14px;
  outline: none;
}
.form-group input:focus, .form-group select:focus { border-color: #2563eb; box-shadow: 0 0 0 3px rgba(37,99,235,0.1); }

.modal-actions { display: flex; gap: 10px; justify-content: flex-end; }

.form-error { color: #dc2626; font-size: 13px; }

.loading-text, .empty-state {
  color: #94a3b8;
  font-size: 14px;
  text-align: center;
  padding: 40px;
}
</style>
