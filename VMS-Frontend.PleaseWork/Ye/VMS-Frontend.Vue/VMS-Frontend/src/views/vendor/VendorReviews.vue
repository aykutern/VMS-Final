<template>
  <div class="reviews-page">
    <div class="page-header">
      <h2>Task Reviews</h2>
      <p>Review tasks submitted by personnel before they are marked as completed.</p>
    </div>

    <div v-if="loading" class="loading-state">Loading reviews...</div>
    <div v-else-if="error" class="error-state">{{ error }}</div>
    <div v-else class="reviews-content">
      <div v-if="reviews.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
        <p>No tasks currently in review.</p>
      </div>
      
      <div class="reviews-grid" v-else>
        <div v-for="task in reviews" :key="task.id" class="review-card">
          <div class="card-header">
            <h3>{{ task.name }}</h3>
            <span class="badge purple">In Review</span>
          </div>
          <div class="task-details">
            <p><strong>Project:</strong> {{ task.projectName }}</p>
            <p><strong>Assignee:</strong> {{ task.assigneeName || 'Unassigned' }}</p>
            <p><strong>Priority:</strong> <span :class="['priority', task.priority.toLowerCase()]">{{ task.priority }}</span></p>
            <p><strong>Rank:</strong> ★ {{ task.rank }}</p>
          </div>
          <div class="card-actions">
            <button class="btn btn-outline" @click="rejectTask(task)" :disabled="actionLoading === task.id">Reject</button>
            <button class="btn btn-primary" @click="approveTask(task)" :disabled="actionLoading === task.id">Approve</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Rejection Modal -->
    <div v-if="rejectingTask" class="modal-overlay" @click.self="cancelReject">
      <div class="modal">
        <div class="modal-header">
          <h3>Reject Task</h3>
          <button class="close-btn" @click="cancelReject">✕</button>
        </div>
        <div class="modal-body">
          <p>Please provide a reason for rejecting the task "<strong>{{ rejectingTask.name }}</strong>":</p>
          <textarea v-model="rejectionReasonInput" placeholder="Enter reason here..." rows="4"></textarea>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="cancelReject">Cancel</button>
          <button class="btn btn-primary btn-danger" @click="confirmReject" :disabled="!rejectionReasonInput.trim() || submittingReject">
            {{ submittingReject ? 'Confirming...' : 'Reject Task' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { http } from "../../lib/http.js";
import { getCurrentUser } from "../../services/authService.js";

const reviews = ref([]);
const loading = ref(true);
const error = ref(null);
const actionLoading = ref(null);

const rejectingTask = ref(null);
const rejectionReasonInput = ref("");
const submittingReject = ref(false);

const user = getCurrentUser();

async function fetchReviews() {
  loading.value = true;
  error.value = null;
  try {
    let url = "/api/assignments";
    if (user.userType === "VENDOR_ADMIN" && user.vendorId) {
      url += `?vendorId=${user.vendorId}`;
    }
    const { data } = await http.get(url);
    reviews.value = data.filter(t => t.status === "IN_REVIEW");
  } catch (err) {
    console.error("Failed to load reviews:", err);
    error.value = "Failed to load tasks for review.";
  } finally {
    loading.value = false;
  }
}

async function approveTask(task) {
  actionLoading.value = task.id;
  try {
    await http.patch(`/api/assignments/${task.id}/status`, { status: "COMPLETED" });
    reviews.value = reviews.value.filter(t => t.id !== task.id);
  } catch (err) {
    console.error("Failed to approve task", err);
    alert("Error approving task.");
  } finally {
    actionLoading.value = null;
  }
}

function rejectTask(task) {
  rejectingTask.value = task;
  rejectionReasonInput.value = "";
}

function cancelReject() {
  rejectingTask.value = null;
  rejectionReasonInput.value = "";
}

async function confirmReject() {
  if (!rejectionReasonInput.value.trim()) return;
  submittingReject.value = true;
  const task = rejectingTask.value;
  try {
    await http.patch(`/api/assignments/${task.id}/status`, { status: "IN_PROGRESS", rejectionReason: rejectionReasonInput.value.trim() });
    reviews.value = reviews.value.filter(t => t.id !== task.id);
    cancelReject();
  } catch (err) {
    console.error("Failed to reject task", err);
    alert("Error rejecting task.");
  } finally {
    submittingReject.value = false;
  }
}

onMounted(() => {
  fetchReviews();
});
</script>

<style scoped>
.reviews-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
}
.page-header p {
  color: #64748b;
  margin: 8px 0 0;
  font-size: 14px;
}
.loading-state, .error-state, .empty-state {
  text-align: center;
  padding: 40px;
  color: #94a3b8;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px dashed #e2e8f0;
}
.empty-state svg {
  width: 48px;
  height: 48px;
  fill: #22c55e;
  margin-bottom: 16px;
  opacity: 0.7;
}
.error-state {
  color: #dc2626;
  border-color: #fecaca;
  background: #fef2f2;
}
.reviews-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}
.review-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.4;
}
.badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  white-space: nowrap;
}
.badge.purple {
  background: #f5f3ff;
  color: #7c3aed;
  border: 1px solid #ddd6fe;
}
.task-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  color: #475569;
}
.task-details strong {
  color: #1e293b;
}
.priority.high { color: #dc2626; }
.priority.medium { color: #d97706; }
.priority.low { color: #2563eb; }
.card-actions {
  display: flex;
  gap: 12px;
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}
.btn {
  flex: 1;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-primary {
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}
.btn-primary:hover:not(:disabled) {
  background: #dcfce7;
}
.btn-outline {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}
.btn-outline:hover:not(:disabled) {
  background: #fee2e2;
}
.btn-danger {
  background: #fef2f2;
  color: #dc2626;
  border-color: #fecaca;
}
.btn-danger:hover:not(:disabled) {
  background: #fee2e2;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}
.modal {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  width: min(100%, 480px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
}
.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.modal-header h3 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
}
.close-btn {
  background: transparent;
  border: none;
  color: #94a3b8;
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
}
.close-btn:hover {
  color: #475569;
}
.modal-body {
  padding: 24px;
  color: #475569;
  font-size: 14px;
  line-height: 1.5;
}
.modal-body p {
  margin: 0 0 16px;
}
.modal-body textarea {
  width: 100%;
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 12px;
  color: #0f172a;
  font-family: inherit;
  resize: vertical;
  min-height: 80px;
  outline: none;
}
.modal-body textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}
.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.modal-footer .btn {
  flex: none;
  padding: 10px 20px;
}
</style>
