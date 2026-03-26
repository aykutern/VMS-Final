import { createRouter, createWebHistory } from "vue-router";
import { isAuthenticated, getCurrentUser } from "../services/authService.js";
import Login from "../views/Login.vue";
import AppLayout from "../components/AppLayout.vue";

// PM Views
import PMhome from "../views/PMhome.vue";
import PMProjects from "../views/pm/Projects.vue";
import PMProjectDetail from "../views/pm/ProjectDetail.vue";
import PMSprints from "../views/pm/Sprints.vue";
import PMAssignments from "../views/pm/Assignments.vue";
import PMVendors from "../views/pm/Vendors.vue";

// Vendor Admin Views
import VendorAdminHome from "../views/VendorAdminHome.vue";
import VendorProjects from "../views/vendor/VendorProjects.vue";
import VendorSprints from "../views/vendor/VendorSprints.vue";
import VendorAssignments from "../views/vendor/VendorAssignments.vue";
import VendorPersonnel from "../views/vendor/VendorPersonnel.vue";
import VendorPerformance from "../views/vendor/VendorPerformance.vue";
import VendorReviews from "../views/vendor/VendorReviews.vue";

// Personnel Views
import PersonnelHome from "../views/PersonnelHome.vue";
import PersonnelTasks from "../views/PersonnelTasks.vue";

// Shared Views
import Announcements from "../views/Announcements.vue";

const routes = [
  // Public
  { path: "/login", name: "login", component: Login },

  // Product Manager — rendered inside AppLayout
  {
    path: "/pm",
    component: AppLayout,
    redirect: "/pm/dashboard",
    meta: { requiresAuth: true, role: "MANAGER" },
    children: [
      { path: "dashboard", name: "pm-dashboard", component: PMhome },
      { path: "projects", name: "pm-projects", component: PMProjects },
      { path: "projects/:id", name: "pm-project-detail", component: PMProjectDetail },
      { path: "sprints", name: "pm-sprints", component: PMSprints },
      { path: "assignments", name: "pm-assignments", component: PMAssignments },
      { path: "reviews", name: "pm-reviews", component: VendorReviews },
      { path: "vendors", name: "pm-vendors", component: PMVendors },
      { path: "announcements", name: "pm-announcements", component: Announcements },
    ],
  },

  // Vendor Admin — rendered inside AppLayout
  {
    path: "/vendor",
    component: AppLayout,
    redirect: "/vendor/dashboard",
    meta: { requiresAuth: true, role: "VENDOR_ADMIN" },
    children: [
      { path: "dashboard", name: "vendor-dashboard", component: VendorAdminHome },
      { path: "projects", name: "vendor-projects", component: VendorProjects },
      { path: "sprints", name: "vendor-sprints", component: VendorSprints },
      { path: "assignments", name: "vendor-assignments", component: VendorAssignments },
      { path: "reviews", name: "vendor-reviews", component: VendorReviews },
      { path: "personnel", name: "vendor-personnel", component: VendorPersonnel },
      { path: "performance", name: "vendor-performance", component: VendorPerformance },
      { path: "announcements", name: "vendor-announcements", component: Announcements },
    ],
  },

  // Personnel — rendered inside AppLayout
  {
    path: "/personnel",
    component: AppLayout,
    redirect: "/personnel/dashboard",
    meta: { requiresAuth: true, role: "PERSONNEL" },
    children: [
      { path: "dashboard", name: "personnel-dashboard", component: PersonnelHome },
      { path: "tasks", name: "personnel-tasks", component: PersonnelTasks },
      { path: "announcements", name: "personnel-announcements", component: Announcements },
    ],
  },

  // Default
  { path: "/", redirect: "/login" },
  { path: "/:pathMatch(.*)*", redirect: "/login" },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Navigation guard
router.beforeEach((to, _from, next) => {
  if (!to.meta.requiresAuth) return next();
  if (!isAuthenticated()) return next("/login");

  const user = getCurrentUser();
  const userType = user?.userType;

  if (to.meta.role && to.meta.role !== userType) {
    if (userType === "MANAGER") return next("/pm/dashboard");
    if (userType === "VENDOR_ADMIN") return next("/vendor/dashboard");
    if (userType === "PERSONNEL") return next("/personnel");
    return next("/login");
  }

  next();
});

export default router;