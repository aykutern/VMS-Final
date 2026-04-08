<template>
  <div class="performance-page">

    <div v-if="loading" class="loading-text">Loading performance data…</div>
    <div v-else>
      <table class="perf-table" v-if="employees.length > 0">
        <thead>
          <tr>
            <th>Rank</th>
            <th>Employee</th>
            <th>Completed Tasks</th>
            <th>Total Tasks</th>
            <th>Completed Pts</th>
            <th>Total Pts</th>
            <th>Completion Rate</th>
            <th>Avg Completion (days)</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(e, idx) in rankedEmployees" :key="e.userId" :class="{ 'top-performer': idx === 0 && e.completedTasks > 0 }">
            <td class="rank-cell">
              <div class="rank-wrap">
                <span v-if="idx === 0 && e.completedTasks > 0" class="rank-crown" title="Top Performer">
                  <svg viewBox="0 0 24 24"><path d="M5 16L3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5zm2 3a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1v-1H7v1z"/></svg>
                </span>
                <span v-else class="rank-icon-placeholder"></span>
                <span class="rank-num" :class="{ 'rank-num--gold': idx === 0 && e.completedTasks > 0 }">{{ idx + 1 }}</span>
              </div>
            </td>
            <td>
              <div class="emp-name">
                {{ e.fullName }}
                <span v-if="idx === 0 && e.completedTasks > 0" class="top-badge">Top Performer</span>
              </div>
            </td>
            <td>
              <span class="completed-count">{{ e.completedTasks }}</span>
            </td>
            <td>{{ e.totalTasks }}</td>
            <td>
              <span class="completed-count">{{ e.completedPoints }}</span>
            </td>
            <td>{{ e.totalPoints }}</td>
            <td>
              <div class="rate-bar-wrap">
                <div class="rate-bar">
                  <div class="rate-fill" :style="{ width: completionRate(e) + '%' }"></div>
                </div>
                <span class="rate-label">{{ completionRate(e) }}%</span>
              </div>
            </td>
            <td>
              <span v-if="e.avgCompletionDays != null" :class="['avg-days', e.avgCompletionDays <= 3 ? 'fast' : e.avgCompletionDays <= 7 ? 'medium' : 'slow']">
                {{ e.avgCompletionDays }} days
              </span>
              <span v-else class="no-data">—</span>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state">No employees found.</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { http } from "../../lib/http.js";
import { getCurrentUser } from "../../services/authService.js";

const user = getCurrentUser();
const employees = ref([]);
const loading = ref(true);

// Sort by completedTasks desc, then avgCompletionDays asc (lower = more efficient)
const rankedEmployees = computed(() => {
  return [...employees.value].sort((a, b) => {
    // Sort by completed points desc first
    if (b.completedPoints !== a.completedPoints) return b.completedPoints - a.completedPoints;
    // Then by completed tasks
    if (b.completedTasks !== a.completedTasks) return b.completedTasks - a.completedTasks;
    // If same, lower avg days = more efficient = ranked higher
    const aAvg = a.avgCompletionDays ?? Infinity;
    const bAvg = b.avgCompletionDays ?? Infinity;
    return aAvg - bAvg;
  });
});

onMounted(async () => {
  try {
    const vendorId = user?.vendorId;
    if (vendorId) {
      const r = await http.get(`/api/dashboard/employee-performance?vendorId=${vendorId}`);
      employees.value = r.data;
    }
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});

function completionRate(e) {
  if (e.totalTasks === 0) return 0;
  return Math.round((e.completedTasks / e.totalTasks) * 100);
}
</script>

<style scoped>
.performance-page { display:flex; flex-direction:column; gap:24px; }

.perf-table { width:100%; border-collapse:collapse; font-size:13px; background:#ffffff; border:1px solid #e2e8f0; border-radius:12px; overflow:hidden; box-shadow:0 1px 3px rgba(0,0,0,0.06); }
.perf-table th { text-align:left; padding:14px 16px; color:#64748b; font-weight:600; border-bottom:1px solid #e2e8f0; white-space:nowrap; background:#f8fafc; font-size:11px; text-transform:uppercase; letter-spacing:0.05em; }
.perf-table td { padding:14px 16px; color:#1e293b; border-bottom:1px solid #f1f5f9; }
.perf-table tr:last-child td { border-bottom:none; }
.perf-table tr:hover td { background:#f8fafc; }

/* Top performer row highlight */
.top-performer td { background:#fffbeb !important; }

.rank-cell { width: 60px; }
.rank-wrap { display: flex; align-items: center; gap: 6px; }
.rank-crown { display: inline-flex; align-items: center; width: 16px; flex-shrink: 0; }
.rank-crown svg { width: 15px; height: 15px; fill: #d97706; }
.rank-icon-placeholder { display: inline-block; width: 16px; flex-shrink: 0; }
.rank-num { font-size: 14px; font-weight: 700; color: #94a3b8; }
.rank-num--gold { color: #d97706; }

.emp-name { display:flex; align-items:center; gap:8px; font-weight:600; color:#0f172a; }
.top-badge { padding:3px 10px; background:linear-gradient(135deg,#f59e0b,#d97706); border-radius:999px; font-size:10px; font-weight:700; color:#fff; text-transform:uppercase; letter-spacing:0.05em; }

.completed-count { font-size:16px; font-weight:800; color:#16a34a; }

.rate-bar-wrap { display:flex; align-items:center; gap:8px; }
.rate-bar { width:80px; height:6px; background:#e2e8f0; border-radius:999px; overflow:hidden; }
.rate-fill { height:100%; background:linear-gradient(90deg,#22c55e,#16a34a); border-radius:999px; transition:width 0.3s; }
.rate-label { font-size:12px; color:#16a34a; font-weight:600; }

.avg-days { padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; }
.avg-days.fast { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }
.avg-days.medium { background:#fffbeb; color:#d97706; border:1px solid #fde68a; }
.avg-days.slow { background:#fef2f2; color:#dc2626; border:1px solid #fecaca; }
.no-data { color:#94a3b8; }

.loading-text,.empty-state { color:#94a3b8; font-size:14px; text-align:center; padding:40px; }
</style>
