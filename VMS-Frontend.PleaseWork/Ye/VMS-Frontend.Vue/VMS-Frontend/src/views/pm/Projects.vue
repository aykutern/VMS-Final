<template>
  <div class="projects-page">
    <!-- Header -->
    <div class="page-header">
      <h2>Projects</h2>
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
.page-header h2 { margin: 0; font-size: 20px; font-weight: 800; color: #f3f7ff; }

.project-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }

.project-card {
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.project-card:hover { border-color: rgba(99,102,241,0.4); background: rgba(255,255,255,0.06); }

.project-card-header { display: flex; align-items: center; gap: 12px; }

.project-avatar {
  width: 44px; height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  display: grid;
  place-items: center;
  font-size: 18px;
  font-weight: 800;
  color: #fff;
  flex-shrink: 0;
}

.project-name { font-size: 15px; font-weight: 700; color: #e2eaff; }
.project-vendor { font-size: 12px; color: rgba(200,215,255,0.55); margin-top: 3px; }

.project-footer { display: flex; align-items: center; justify-content: space-between; }
.pm-name { font-size: 12px; color: rgba(200,215,255,0.4); }

.badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 11px; font-weight: 600; }
.badge.active { background: rgba(34,197,94,0.15); color: #86efac; }

.primary-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.15s;
}
.primary-btn:hover { opacity: 0.9; }
.primary-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.secondary-btn {
  padding: 10px 18px;
  background: rgba(255,255,255,0.07);
  color: #d1deff;
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 10px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
}

.modal-backdrop {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.6);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  z-index: 100;
}
.modal {
  background: #0f1a2e;
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 20px;
  padding: 28px;
  width: min(460px, 90vw);
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.modal-header { display: flex; align-items: center; justify-content: space-between; }
.modal-header h3 { margin: 0; font-size: 17px; font-weight: 800; color: #f3f7ff; }
.close-btn { background: transparent; border: none; color: rgba(200,215,255,0.5); font-size: 18px; cursor: pointer; }

.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 13px; color: rgba(200,215,255,0.7); font-weight: 600; }
.form-group input, .form-group select {
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 10px;
  padding: 10px 14px;
  color: #e2eaff;
  font-size: 14px;
  outline: none;
}
.form-group input:focus, .form-group select:focus { border-color: rgba(99,102,241,0.5); }
.form-group select option { background: #0f1a2e; }

.modal-actions { display: flex; gap: 10px; justify-content: flex-end; }

.form-error { color: #f87171; font-size: 13px; }

.loading-text, .empty-state {
  color: rgba(200,215,255,0.4);
  font-size: 14px;
  text-align: center;
  padding: 40px;
}
</style>
