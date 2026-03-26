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
  color: #f3f7ff;
}
.page-header p {
  color: rgba(200, 215, 255, 0.6);
  margin: 8px 0 0;
  font-size: 14px;
}
.loading-state, .error-state, .empty-state {
  text-align: center;
  padding: 40px;
  color: rgba(200, 215, 255, 0.5);
  background: rgba(255, 255, 255, 0.02);
  border-radius: 16px;
  border: 1px dashed rgba(255, 255, 255, 0.1);
}
.empty-state svg {
  width: 48px;
  height: 48px;
  fill: #22c55e;
  margin-bottom: 16px;
  opacity: 0.8;
}
.error-state {
  color: #fca5a5;
  border-color: rgba(248, 113, 113, 0.3);
  background: rgba(248, 113, 113, 0.05);
}
.reviews-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}
.review-card {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
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
  color: #e2eaff;
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
  background: rgba(168, 85, 247, 0.15);
  color: #d8b4fe;
}
.task-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  color: rgba(200, 215, 255, 0.7);
}
.task-details strong {
  color: rgba(200, 215, 255, 0.9);
}
.priority.high { color: #fca5a5; }
.priority.medium { color: #fcd34d; }
.priority.low { color: #93c5fd; }
.card-actions {
  display: flex;
  gap: 12px;
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
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
  background: rgba(34, 197, 94, 0.2);
  color: #86efac;
  border: 1px solid rgba(34, 197, 94, 0.3);
}
.btn-primary:hover:not(:disabled) {
  background: rgba(34, 197, 94, 0.3);
}
.btn-outline {
  background: transparent;
  color: #fca5a5;
  border: 1px solid rgba(239, 68, 68, 0.3);
}
.btn-outline:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.1);
}
.btn-danger {
  background: rgba(239, 68, 68, 0.2);
  color: #fca5a5;
  border-color: rgba(239, 68, 68, 0.3);
}
.btn-danger:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.3);
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}
.modal {
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  width: min(100%, 480px);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
}
.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.modal-header h3 {
  margin: 0;
  color: #f3f7ff;
  font-size: 18px;
}
.close-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.4);
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
}
.close-btn:hover {
  color: #fff;
}
.modal-body {
  padding: 24px;
  color: rgba(200, 215, 255, 0.8);
  font-size: 14px;
  line-height: 1.5;
}
.modal-body p {
  margin: 0 0 16px;
}
.modal-body textarea {
  width: 100%;
  background: rgba(5, 10, 20, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  padding: 12px;
  color: #f3f7ff;
  font-family: inherit;
  resize: vertical;
  min-height: 80px;
}
.modal-body textarea:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.15);
}
.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.modal-footer .btn {
  flex: none;
  padding: 10px 20px;
}
</style>
