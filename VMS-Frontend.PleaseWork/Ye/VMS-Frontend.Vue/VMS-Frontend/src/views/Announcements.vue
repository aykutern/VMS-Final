<template>
  <div class="ann-page">

    <!-- Filters -->
    <div class="filters-bar">
      <div class="search-wrap">
        <svg viewBox="0 0 24 24"><path d="M15.5 14h-.79l-.28-.27A6.47 6.47 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
        <input v-model="search" placeholder="Search announcements…" />
      </div>
      <select v-model="filterProject">
        <option value="">All Projects</option>
        <option v-for="p in projectOptions" :key="p.id" :value="p.id">{{ p.name }}</option>
      </select>
    </div>

    <div v-if="loading" class="loading-text">Loading announcements…</div>

    <div v-else-if="filtered.length === 0" class="empty-state">
      <div class="empty-icon">📢</div>
      <div class="empty-label">No announcements found</div>
      <div class="empty-hint">Try adjusting your filters</div>
    </div>

    <div v-else class="ann-list">
      <div v-for="a in filtered" :key="a.id" class="ann-card">
        <div class="ann-top">
          <div class="ann-meta">
            <span class="ann-project">{{ a.projectName ?? 'General' }}</span>
            <span class="ann-dot">·</span>
            <span class="ann-date">{{ formatDate(a.createdAt) }}</span>
          </div>
          <span class="ann-tag">Announcement</span>
        </div>
        <div class="ann-title">{{ a.title }}</div>
        <div class="ann-body">{{ a.content }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { http } from "../lib/http.js";
import { getCurrentUser } from "../services/authService.js";

const user = getCurrentUser();
const announcements = ref([]);
const loading = ref(true);
const search = ref("");
const filterProject = ref("");

onMounted(async () => {
  try {
    const vendorId = user?.vendorId;
    // Fetch all announcements
    const r = await http.get("/api/announcements");
    let all = r.data;

    // For vendor/personnel users, filter to only their vendor's projects
    if (vendorId) {
      const pRes = await http.get(`/api/projects?vendorId=${vendorId}`);
      const allowedIds = new Set(pRes.data.map(p => p.id));
      all = all.filter(a => allowedIds.has(a.projectId));
    }

    // Sort newest first by createdAt
    announcements.value = [...all].sort((a, b) =>
      new Date(b.createdAt) - new Date(a.createdAt)
    );
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});

const projectOptions = computed(() => {
  const map = new Map();
  announcements.value.forEach(a => {
    if (a.projectId && !map.has(a.projectId)) {
      map.set(a.projectId, { id: a.projectId, name: a.projectName ?? `Project #${a.projectId}` });
    }
  });
  return [...map.values()];
});

const filtered = computed(() => {
  const q = search.value.trim().toLowerCase();
  return announcements.value.filter(a => {
    if (filterProject.value && a.projectId !== filterProject.value) return false;
    if (q && !a.title.toLowerCase().includes(q) && !a.content?.toLowerCase().includes(q)) return false;
    return true;
  });
});

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("en-GB", {
    day: "numeric", month: "short", year: "numeric",
    hour: "2-digit", minute: "2-digit"
  });
}
</script>

<style scoped>
.ann-page { display: flex; flex-direction: column; gap: 24px; }

.page-header { display: flex; align-items: center; gap: 14px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 800; color: #0f172a; }
.ann-count { font-size: 12px; color: #64748b; background: #f1f5f9; padding: 4px 12px; border-radius: 999px; border: 1px solid #e2e8f0; }

.filters-bar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.search-wrap { position: relative; flex: 1; min-width: 220px; }
.search-wrap svg { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); width: 16px; height: 16px; fill: #94a3b8; pointer-events: none; }
.search-wrap input { width: 100%; background: #ffffff; border: 1px solid #cbd5e1; border-radius: 8px; padding: 9px 14px 9px 38px; color: #0f172a; font-size: 13px; outline: none; box-sizing: border-box; }
.search-wrap input:focus { border-color: #2563eb; box-shadow: 0 0 0 3px rgba(37,99,235,0.1); }
.search-wrap input::placeholder { color: #94a3b8; }
.filters-bar select { background: #ffffff; border: 1px solid #cbd5e1; border-radius: 8px; padding: 9px 14px; color: #1e293b; font-size: 13px; outline: none; cursor: pointer; }

.ann-list { display: flex; flex-direction: column; gap: 14px; }

.ann-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px 22px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.ann-card:hover { border-color: #a78bfa; box-shadow: 0 4px 12px rgba(99,102,241,0.08); }

.ann-top { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.ann-meta { display: flex; align-items: center; gap: 6px; font-size: 12px; }
.ann-project { color: #2563eb; font-weight: 600; }
.ann-dot { color: #cbd5e1; }
.ann-date { color: #94a3b8; }
.ann-tag { font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.08em; color: #7c3aed; background: #f5f3ff; border: 1px solid #ddd6fe; padding: 2px 8px; border-radius: 999px; }

.ann-title { font-size: 15px; font-weight: 700; color: #0f172a; line-height: 1.4; }
.ann-body { font-size: 13px; color: #475569; line-height: 1.65; }

/* Empty state */
.empty-state { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 60px 20px; }
.empty-icon { font-size: 40px; opacity: 0.5; }
.empty-label { font-size: 15px; font-weight: 600; color: #64748b; }
.empty-hint { font-size: 13px; color: #94a3b8; }

.loading-text { color: #94a3b8; font-size: 14px; text-align: center; padding: 60px; }
</style>
