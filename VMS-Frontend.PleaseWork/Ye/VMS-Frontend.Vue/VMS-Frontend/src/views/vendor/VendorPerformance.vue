<template>
  <div class="performance-page">
    <div class="page-header">
      <h2>Employee Performance</h2>
    </div>

    <div v-if="loading" class="loading-text">Loading performance data…</div>
    <div v-else>
      <table class="perf-table" v-if="employees.length > 0">
        <thead>
          <tr>
            <th>Rank</th>
            <th>Employee</th>
            <th>Completed Tasks</th>
            <th>Total Tasks</th>
            <th>Completion Rate</th>
            <th>Avg Completion (days)</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(e, idx) in rankedEmployees" :key="e.userId" :class="{ 'top-performer': idx === 0 && e.completedTasks > 0 }">
            <td>
              <span class="rank-badge" v-if="idx === 0 && e.completedTasks > 0">⭐ 1</span>
              <span v-else class="rank-num">{{ idx + 1 }}</span>
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
    if (b.completedTasks !== a.completedTasks) return b.completedTasks - a.completedTasks;
    // If same completed tasks, lower avg days = more efficient = ranked higher
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
.page-header { display:flex; align-items:center; justify-content:space-between; }
.page-header h2 { margin:0; font-size:20px; font-weight:800; color:#f3f7ff; }

.perf-table { width:100%; border-collapse:collapse; font-size:13px; background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.08); border-radius:16px; overflow:hidden; }
.perf-table th { text-align:left; padding:14px 16px; color:rgba(200,215,255,0.5); font-weight:600; border-bottom:1px solid rgba(255,255,255,0.07); white-space:nowrap; background:rgba(255,255,255,0.02); }
.perf-table td { padding:14px 16px; color:#d1deff; border-bottom:1px solid rgba(255,255,255,0.04); }
.perf-table tr:last-child td { border-bottom:none; }
.perf-table tr:hover td { background:rgba(255,255,255,0.03); }

/* Top performer row highlight */
.top-performer td { background:rgba(251,191,36,0.04) !important; }

.rank-badge { display:inline-flex; align-items:center; gap:4px; padding:4px 10px; background:linear-gradient(135deg,rgba(251,191,36,0.2),rgba(245,158,11,0.15)); border:1px solid rgba(251,191,36,0.3); border-radius:999px; font-size:12px; font-weight:700; color:#fde68a; }
.rank-num { font-size:14px; font-weight:700; color:rgba(200,215,255,0.5); }

.emp-name { display:flex; align-items:center; gap:8px; font-weight:600; color:#e2eaff; }
.top-badge { padding:3px 10px; background:linear-gradient(135deg,#f59e0b,#d97706); border-radius:999px; font-size:10px; font-weight:700; color:#fff; text-transform:uppercase; letter-spacing:0.05em; }

.completed-count { font-size:16px; font-weight:800; color:#86efac; }

.rate-bar-wrap { display:flex; align-items:center; gap:8px; }
.rate-bar { width:80px; height:6px; background:rgba(255,255,255,0.08); border-radius:999px; overflow:hidden; }
.rate-fill { height:100%; background:linear-gradient(90deg,#22c55e,#16a34a); border-radius:999px; transition:width 0.3s; }
.rate-label { font-size:12px; color:#86efac; font-weight:600; }

.avg-days { padding:3px 10px; border-radius:999px; font-size:11px; font-weight:600; }
.avg-days.fast { background:rgba(34,197,94,0.15); color:#86efac; }
.avg-days.medium { background:rgba(251,191,36,0.15); color:#fde68a; }
.avg-days.slow { background:rgba(239,68,68,0.15); color:#fca5a5; }
.no-data { color:rgba(200,215,255,0.3); }

.loading-text,.empty-state { color:rgba(200,215,255,0.4); font-size:14px; text-align:center; padding:40px; }
</style>
