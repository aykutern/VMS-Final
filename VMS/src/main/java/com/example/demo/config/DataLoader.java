package com.example.demo.config;

import com.example.demo.entities.concretes.*;
import com.example.demo.enums.*;
import com.example.demo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

        private final VendorRepository vendorRepository;
        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;
        private final AnnouncementRepository announcementRepository;
        private final SprintRepository sprintRepository;
        private final AssignmentRepository assignmentRepository;
        private final ProductManagerRepository productManagerRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) {
                // Always drop and re-seed so hashed passwords stay in sync
                assignmentRepository.deleteAll();
                announcementRepository.deleteAll();
                sprintRepository.deleteAll();
                projectRepository.deleteAll();
                productManagerRepository.deleteAll();
                userRepository.deleteAll();
                vendorRepository.deleteAll();

                // ── Vendors ──────────────────────────────────────────────────────────
                Vendor techCorp = new Vendor();
                techCorp.setVendorName("TechCorp Solutions");
                techCorp.setIsActive(1);
                techCorp = vendorRepository.save(techCorp);

                Vendor softSolutions = new Vendor();
                softSolutions.setVendorName("SoftSolutions Inc.");
                softSolutions.setIsActive(1);
                softSolutions = vendorRepository.save(softSolutions);

                Vendor innovaLabs = new Vendor();
                innovaLabs.setVendorName("InnovaLabs");
                innovaLabs.setIsActive(1);
                innovaLabs = vendorRepository.save(innovaLabs);

                // ── Product Managers ─────────────────────────────────────────────────
                ProductManager pm1 = new ProductManager();
                pm1.setName("Alice Johnson");
                pm1.setTitle(PersonnelTitle.MANAGER);
                pm1.setIsActive(1);
                pm1 = productManagerRepository.save(pm1);

                ProductManager pm2 = new ProductManager();
                pm2.setName("Bob Williams");
                pm2.setTitle(PersonnelTitle.PRODUCT_OWNER);
                pm2.setIsActive(1);
                pm2 = productManagerRepository.save(pm2);

                // ── Users ─────────────────────────────────────────────────────────────

                // -- Product Manager login
                Users pmUser = new Users();
                pmUser.setUsername("pm_admin");
                pmUser.setPassword(passwordEncoder.encode("Password1"));
                pmUser.setEmail("pm@vms.com");
                pmUser.setPersonnelName("Alice");
                pmUser.setPersonnelSurname("Johnson");
                pmUser.setUserType(UserType.MANAGER);
                pmUser.setIsActive(1);
                pmUser = userRepository.save(pmUser);

                // ── TechCorp Users ────────────────────────────────────────────────────

                Users techAdmin = new Users();
                techAdmin.setUsername("tech_admin");
                techAdmin.setPassword(passwordEncoder.encode("Password1"));
                techAdmin.setEmail("admin@techcorp.com");
                techAdmin.setPersonnelName("Carlos");
                techAdmin.setPersonnelSurname("Rivera");
                techAdmin.setUserType(UserType.VENDOR_ADMIN);
                techAdmin.setVendor(techCorp);
                techAdmin.setIsActive(1);
                techAdmin = userRepository.save(techAdmin);

                Users techDev1 = new Users();
                techDev1.setUsername("tech_dev1");
                techDev1.setPassword(passwordEncoder.encode("Password1"));
                techDev1.setEmail("diana.chen@techcorp.com");
                techDev1.setPersonnelName("Diana");
                techDev1.setPersonnelSurname("Chen");
                techDev1.setUserType(UserType.PERSONNEL);
                techDev1.setVendor(techCorp);
                techDev1.setIsActive(1);
                techDev1 = userRepository.save(techDev1);

                Users techDev2 = new Users();
                techDev2.setUsername("tech_dev2");
                techDev2.setPassword(passwordEncoder.encode("Password1"));
                techDev2.setEmail("james.kim@techcorp.com");
                techDev2.setPersonnelName("James");
                techDev2.setPersonnelSurname("Kim");
                techDev2.setUserType(UserType.PERSONNEL);
                techDev2.setVendor(techCorp);
                techDev2.setIsActive(1);
                techDev2 = userRepository.save(techDev2);

                Users techDev3 = new Users();
                techDev3.setUsername("tech_dev3");
                techDev3.setPassword(passwordEncoder.encode("Password1"));
                techDev3.setEmail("priya.sharma@techcorp.com");
                techDev3.setPersonnelName("Priya");
                techDev3.setPersonnelSurname("Sharma");
                techDev3.setUserType(UserType.PERSONNEL);
                techDev3.setVendor(techCorp);
                techDev3.setIsActive(1);
                techDev3 = userRepository.save(techDev3);

                Users techDev4 = new Users();
                techDev4.setUsername("tech_dev4");
                techDev4.setPassword(passwordEncoder.encode("Password1"));
                techDev4.setEmail("liam.foster@techcorp.com");
                techDev4.setPersonnelName("Liam");
                techDev4.setPersonnelSurname("Foster");
                techDev4.setUserType(UserType.PERSONNEL);
                techDev4.setVendor(techCorp);
                techDev4.setIsActive(1);
                techDev4 = userRepository.save(techDev4);

                Users techDev5 = new Users();
                techDev5.setUsername("tech_dev5");
                techDev5.setPassword(passwordEncoder.encode("Password1"));
                techDev5.setEmail("yuna.park@techcorp.com");
                techDev5.setPersonnelName("Yuna");
                techDev5.setPersonnelSurname("Park");
                techDev5.setUserType(UserType.PERSONNEL);
                techDev5.setVendor(techCorp);
                techDev5.setIsActive(1);
                techDev5 = userRepository.save(techDev5);

                // ── SoftSolutions Users ───────────────────────────────────────────────

                Users softAdmin = new Users();
                softAdmin.setUsername("soft_admin");
                softAdmin.setPassword(passwordEncoder.encode("Password1"));
                softAdmin.setEmail("admin@softsolutions.com");
                softAdmin.setPersonnelName("Maria");
                softAdmin.setPersonnelSurname("Lopez");
                softAdmin.setUserType(UserType.VENDOR_ADMIN);
                softAdmin.setVendor(softSolutions);
                softAdmin.setIsActive(1);
                softAdmin = userRepository.save(softAdmin);

                Users softDev1 = new Users();
                softDev1.setUsername("soft_dev1");
                softDev1.setPassword(passwordEncoder.encode("Password1"));
                softDev1.setEmail("ethan.park@softsolutions.com");
                softDev1.setPersonnelName("Ethan");
                softDev1.setPersonnelSurname("Park");
                softDev1.setUserType(UserType.PERSONNEL);
                softDev1.setVendor(softSolutions);
                softDev1.setIsActive(1);
                softDev1 = userRepository.save(softDev1);

                Users softDev2 = new Users();
                softDev2.setUsername("soft_dev2");
                softDev2.setPassword(passwordEncoder.encode("Password1"));
                softDev2.setEmail("sara.white@softsolutions.com");
                softDev2.setPersonnelName("Sara");
                softDev2.setPersonnelSurname("White");
                softDev2.setUserType(UserType.PERSONNEL);
                softDev2.setVendor(softSolutions);
                softDev2.setIsActive(1);
                softDev2 = userRepository.save(softDev2);

                Users softDev3 = new Users();
                softDev3.setUsername("soft_dev3");
                softDev3.setPassword(passwordEncoder.encode("Password1"));
                softDev3.setEmail("ivan.petrov@softsolutions.com");
                softDev3.setPersonnelName("Ivan");
                softDev3.setPersonnelSurname("Petrov");
                softDev3.setUserType(UserType.PERSONNEL);
                softDev3.setVendor(softSolutions);
                softDev3.setIsActive(1);
                softDev3 = userRepository.save(softDev3);

                Users softDev4 = new Users();
                softDev4.setUsername("soft_dev4");
                softDev4.setPassword(passwordEncoder.encode("Password1"));
                softDev4.setEmail("amy.nguyen@softsolutions.com");
                softDev4.setPersonnelName("Amy");
                softDev4.setPersonnelSurname("Nguyen");
                softDev4.setUserType(UserType.PERSONNEL);
                softDev4.setVendor(softSolutions);
                softDev4.setIsActive(1);
                softDev4 = userRepository.save(softDev4);

                // ── InnovaLabs Users ──────────────────────────────────────────────────

                Users innovaAdmin = new Users();
                innovaAdmin.setUsername("innova_admin");
                innovaAdmin.setPassword(passwordEncoder.encode("Password1"));
                innovaAdmin.setEmail("admin@innovalabs.com");
                innovaAdmin.setPersonnelName("Sophia");
                innovaAdmin.setPersonnelSurname("Lee");
                innovaAdmin.setUserType(UserType.VENDOR_ADMIN);
                innovaAdmin.setVendor(innovaLabs);
                innovaAdmin.setIsActive(1);
                innovaAdmin = userRepository.save(innovaAdmin);

                Users innovaDev1 = new Users();
                innovaDev1.setUsername("innova_dev1");
                innovaDev1.setPassword(passwordEncoder.encode("Password1"));
                innovaDev1.setEmail("noah.taylor@innovalabs.com");
                innovaDev1.setPersonnelName("Noah");
                innovaDev1.setPersonnelSurname("Taylor");
                innovaDev1.setUserType(UserType.PERSONNEL);
                innovaDev1.setVendor(innovaLabs);
                innovaDev1.setIsActive(1);
                innovaDev1 = userRepository.save(innovaDev1);

                Users innovaDev2 = new Users();
                innovaDev2.setUsername("innova_dev2");
                innovaDev2.setPassword(passwordEncoder.encode("Password1"));
                innovaDev2.setEmail("chloe.martin@innovalabs.com");
                innovaDev2.setPersonnelName("Chloe");
                innovaDev2.setPersonnelSurname("Martin");
                innovaDev2.setUserType(UserType.PERSONNEL);
                innovaDev2.setVendor(innovaLabs);
                innovaDev2.setIsActive(1);
                innovaDev2 = userRepository.save(innovaDev2);

                Users innovaDev3 = new Users();
                innovaDev3.setUsername("innova_dev3");
                innovaDev3.setPassword(passwordEncoder.encode("Password1"));
                innovaDev3.setEmail("dan.okafor@innovalabs.com");
                innovaDev3.setPersonnelName("Daniel");
                innovaDev3.setPersonnelSurname("Okafor");
                innovaDev3.setUserType(UserType.PERSONNEL);
                innovaDev3.setVendor(innovaLabs);
                innovaDev3.setIsActive(1);
                innovaDev3 = userRepository.save(innovaDev3);

                Users innovaDev4 = new Users();
                innovaDev4.setUsername("innova_dev4");
                innovaDev4.setPassword(passwordEncoder.encode("Password1"));
                innovaDev4.setEmail("mia.russo@innovalabs.com");
                innovaDev4.setPersonnelName("Mia");
                innovaDev4.setPersonnelSurname("Russo");
                innovaDev4.setUserType(UserType.PERSONNEL);
                innovaDev4.setVendor(innovaLabs);
                innovaDev4.setIsActive(1);
                innovaDev4 = userRepository.save(innovaDev4);

                // ── Projects ──────────────────────────────────────────────────────────
                Project projectA = new Project();
                projectA.setProjectName("E-Commerce Platform Redesign");
                projectA.setVendor(techCorp);
                projectA.setProjectManager(pm1);
                projectA.setIsActive(1);
                projectA = projectRepository.save(projectA);

                Project projectB = new Project();
                projectB.setProjectName("HR Management System");
                projectB.setVendor(softSolutions);
                projectB.setProjectManager(pm1);
                projectB.setIsActive(1);
                projectB = projectRepository.save(projectB);

                Project projectC = new Project();
                projectC.setProjectName("Mobile Analytics Dashboard");
                projectC.setVendor(innovaLabs);
                projectC.setProjectManager(pm2);
                projectC.setIsActive(1);
                projectC = projectRepository.save(projectC);

                // ── Announcements ─────────────────────────────────────────────────────
                Announcement ann1 = new Announcement();
                ann1.setTitle("Kick-off Meeting Scheduled");
                ann1.setContent("The kick-off for E-Commerce Platform Redesign is March 10th. All team members must attend. Please review the project brief beforehand.");
                ann1.setProject(projectA);
                ann1.setIsActive(1);
                announcementRepository.save(ann1);

                Announcement ann2 = new Announcement();
                ann2.setTitle("New Design Assets Available");
                ann2.setContent("UI/UX design assets for Sprint 1 are now on the shared drive. Developers can start referencing them for implementation.");
                ann2.setProject(projectA);
                ann2.setIsActive(1);
                announcementRepository.save(ann2);

                Announcement ann3 = new Announcement();
                ann3.setTitle("Sprint 2 Goal Updated");
                ann3.setContent("Cart and checkout flows have been added to Sprint 2 scope. Please update your time estimates accordingly.");
                ann3.setProject(projectA);
                ann3.setIsActive(1);
                announcementRepository.save(ann3);

                Announcement ann4 = new Announcement();
                ann4.setTitle("HR System Requirements Finalized");
                ann4.setContent("The BRD for the HR Management System has been approved. Development can proceed as planned.");
                ann4.setProject(projectB);
                ann4.setIsActive(1);
                announcementRepository.save(ann4);

                Announcement ann5 = new Announcement();
                ann5.setTitle("API Gateway Decision");
                ann5.setContent("We will use Kong as the API gateway for the HR system. Configuration docs will be shared by end of week.");
                ann5.setProject(projectB);
                ann5.setIsActive(1);
                announcementRepository.save(ann5);

                Announcement ann6 = new Announcement();
                ann6.setTitle("Analytics SDK Selected");
                ann6.setContent("After evaluation, we are going with Amplitude for the Mobile Analytics Dashboard. Integration docs have been shared in Confluence.");
                ann6.setProject(projectC);
                ann6.setIsActive(1);
                announcementRepository.save(ann6);

                Announcement ann7 = new Announcement();
                ann7.setTitle("Code Review Policy Updated");
                ann7.setContent("Effective immediately, all tasks must go through the IN_REVIEW state before being marked complete. Vendor admins are responsible for approvals.");
                ann7.setProject(projectA);
                ann7.setIsActive(1);
                announcementRepository.save(ann7);

                Announcement ann8 = new Announcement();
                ann8.setTitle("Sprint 3 Kickoff — TechCorp");
                ann8.setContent("Payment gateway integration starts April 9th. Stripe has been chosen as our payment provider. Liam and Yuna will lead the integration.");
                ann8.setProject(projectA);
                ann8.setIsActive(1);
                announcementRepository.save(ann8);

                // ── Sprints ───────────────────────────────────────────────────────────

                // Project A — TechCorp (3 sprints)
                Sprint sprintA1 = new Sprint();
                sprintA1.setSprintName("Sprint 1 — Foundation");
                sprintA1.setStartDate(LocalDate.of(2026, 3, 1));
                sprintA1.setEndDate(LocalDate.of(2026, 3, 14));
                sprintA1.setGoal("Set up project infrastructure, design system, and core API scaffolding.");
                sprintA1.setStatus(SprintStatus.COMPLETED);
                sprintA1.setProject(projectA);
                sprintA1.setMaxCapacity(10);
                sprintA1.setIsActive(1);
                sprintA1.getMembers().add(techDev1);
                sprintA1.getMembers().add(techDev2);
                sprintA1.getMembers().add(techDev3);
                sprintA1 = sprintRepository.save(sprintA1);

                Sprint sprintA2 = new Sprint();
                sprintA2.setSprintName("Sprint 2 — Core Features");
                sprintA2.setStartDate(LocalDate.of(2026, 3, 15));
                sprintA2.setEndDate(LocalDate.of(2026, 3, 28));
                sprintA2.setGoal("Implement product catalog, cart system, and user authentication.");
                sprintA2.setStatus(SprintStatus.COMPLETED);
                sprintA2.setProject(projectA);
                sprintA2.setMaxCapacity(10);
                sprintA2.setIsActive(1);
                sprintA2.getMembers().add(techDev1);
                sprintA2.getMembers().add(techDev2);
                sprintA2.getMembers().add(techDev4);
                sprintA2 = sprintRepository.save(sprintA2);

                Sprint sprintA3 = new Sprint();
                sprintA3.setSprintName("Sprint 3 — Payments & Checkout");
                sprintA3.setStartDate(LocalDate.of(2026, 3, 29));
                sprintA3.setEndDate(LocalDate.of(2026, 4, 11));
                sprintA3.setGoal("Integrate payment gateway, order management, and checkout flow.");
                sprintA3.setStatus(SprintStatus.ACTIVE);
                sprintA3.setProject(projectA);
                sprintA3.setMaxCapacity(10);
                sprintA3.setIsActive(1);
                sprintA3.getMembers().add(techDev3);
                sprintA3.getMembers().add(techDev4);
                sprintA3.getMembers().add(techDev5);
                sprintA3 = sprintRepository.save(sprintA3);

                Sprint sprintA4 = new Sprint();
                sprintA4.setSprintName("Sprint 4 — Testing & Launch");
                sprintA4.setStartDate(LocalDate.of(2026, 4, 12));
                sprintA4.setEndDate(LocalDate.of(2026, 4, 25));
                sprintA4.setGoal("End-to-end testing, performance optimisation, and production deployment.");
                sprintA4.setStatus(SprintStatus.PLANNED);
                sprintA4.setProject(projectA);
                sprintA4.setMaxCapacity(10);
                sprintA4.setIsActive(1);
                sprintA4.getMembers().add(techDev1);
                sprintA4.getMembers().add(techDev5);
                sprintA4 = sprintRepository.save(sprintA4);

                // Project B — SoftSolutions (3 sprints)
                Sprint sprintB1 = new Sprint();
                sprintB1.setSprintName("Sprint 1 — Data Model");
                sprintB1.setStartDate(LocalDate.of(2026, 3, 3));
                sprintB1.setEndDate(LocalDate.of(2026, 3, 16));
                sprintB1.setGoal("Design and implement the employee and department data models.");
                sprintB1.setStatus(SprintStatus.COMPLETED);
                sprintB1.setProject(projectB);
                sprintB1.setMaxCapacity(10);
                sprintB1.setIsActive(1);
                sprintB1.getMembers().add(softDev1);
                sprintB1.getMembers().add(softDev2);
                sprintB1 = sprintRepository.save(sprintB1);

                Sprint sprintB2 = new Sprint();
                sprintB2.setSprintName("Sprint 2 — Reporting");
                sprintB2.setStartDate(LocalDate.of(2026, 3, 17));
                sprintB2.setEndDate(LocalDate.of(2026, 3, 30));
                sprintB2.setGoal("Build payroll reports and leave management features.");
                sprintB2.setStatus(SprintStatus.COMPLETED);
                sprintB2.setProject(projectB);
                sprintB2.setMaxCapacity(10);
                sprintB2.setIsActive(1);
                sprintB2.getMembers().add(softDev2);
                sprintB2.getMembers().add(softDev3);
                sprintB2 = sprintRepository.save(sprintB2);

                Sprint sprintB3 = new Sprint();
                sprintB3.setSprintName("Sprint 3 — Integrations");
                sprintB3.setStartDate(LocalDate.of(2026, 3, 31));
                sprintB3.setEndDate(LocalDate.of(2026, 4, 13));
                sprintB3.setGoal("Integrate payroll provider, SSO, and notification system.");
                sprintB3.setStatus(SprintStatus.ACTIVE);
                sprintB3.setProject(projectB);
                sprintB3.setMaxCapacity(10);
                sprintB3.setIsActive(1);
                sprintB3.getMembers().add(softDev1);
                sprintB3.getMembers().add(softDev3);
                sprintB3.getMembers().add(softDev4);
                sprintB3 = sprintRepository.save(sprintB3);

                Sprint sprintB4 = new Sprint();
                sprintB4.setSprintName("Sprint 4 — UAT & Go-Live");
                sprintB4.setStartDate(LocalDate.of(2026, 4, 14));
                sprintB4.setEndDate(LocalDate.of(2026, 4, 27));
                sprintB4.setGoal("User acceptance testing, bug fixes, and go-live preparation.");
                sprintB4.setStatus(SprintStatus.PLANNED);
                sprintB4.setProject(projectB);
                sprintB4.setMaxCapacity(10);
                sprintB4.setIsActive(1);
                sprintB4.getMembers().add(softDev1);
                sprintB4.getMembers().add(softDev4);
                sprintB4 = sprintRepository.save(sprintB4);

                // Project C — InnovaLabs (3 sprints)
                Sprint sprintC1 = new Sprint();
                sprintC1.setSprintName("Sprint 1 — SDK Integration");
                sprintC1.setStartDate(LocalDate.of(2026, 3, 5));
                sprintC1.setEndDate(LocalDate.of(2026, 3, 18));
                sprintC1.setGoal("Integrate Amplitude SDK and set up event tracking pipeline.");
                sprintC1.setStatus(SprintStatus.COMPLETED);
                sprintC1.setProject(projectC);
                sprintC1.setMaxCapacity(10);
                sprintC1.setIsActive(1);
                sprintC1.getMembers().add(innovaDev1);
                sprintC1.getMembers().add(innovaDev2);
                sprintC1 = sprintRepository.save(sprintC1);

                Sprint sprintC2 = new Sprint();
                sprintC2.setSprintName("Sprint 2 — Dashboard UI");
                sprintC2.setStartDate(LocalDate.of(2026, 3, 19));
                sprintC2.setEndDate(LocalDate.of(2026, 4, 1));
                sprintC2.setGoal("Build the interactive analytics dashboard with charts and filters.");
                sprintC2.setStatus(SprintStatus.COMPLETED);
                sprintC2.setProject(projectC);
                sprintC2.setMaxCapacity(10);
                sprintC2.setIsActive(1);
                sprintC2.getMembers().add(innovaDev2);
                sprintC2.getMembers().add(innovaDev3);
                sprintC2 = sprintRepository.save(sprintC2);

                Sprint sprintC3 = new Sprint();
                sprintC3.setSprintName("Sprint 3 — Real-Time & Alerts");
                sprintC3.setStartDate(LocalDate.of(2026, 4, 2));
                sprintC3.setEndDate(LocalDate.of(2026, 4, 15));
                sprintC3.setGoal("Add real-time data streaming and threshold-based alert system.");
                sprintC3.setStatus(SprintStatus.ACTIVE);
                sprintC3.setProject(projectC);
                sprintC3.setMaxCapacity(10);
                sprintC3.setIsActive(1);
                sprintC3.getMembers().add(innovaDev1);
                sprintC3.getMembers().add(innovaDev3);
                sprintC3.getMembers().add(innovaDev4);
                sprintC3 = sprintRepository.save(sprintC3);

                Sprint sprintC4 = new Sprint();
                sprintC4.setSprintName("Sprint 4 — Performance & Docs");
                sprintC4.setStartDate(LocalDate.of(2026, 4, 16));
                sprintC4.setEndDate(LocalDate.of(2026, 4, 29));
                sprintC4.setGoal("Dashboard performance tuning, API documentation, and final QA.");
                sprintC4.setStatus(SprintStatus.PLANNED);
                sprintC4.setProject(projectC);
                sprintC4.setMaxCapacity(10);
                sprintC4.setIsActive(1);
                sprintC4.getMembers().add(innovaDev2);
                sprintC4.getMembers().add(innovaDev4);
                sprintC4 = sprintRepository.save(sprintC4);

                // ══════════════════════════════════════════════════════════════════════
                // ── Assignments ───────────────────────────────────────────────────────
                // ══════════════════════════════════════════════════════════════════════

                // ── Project A / Sprint 1 (COMPLETED) — total rank = 8 ─────────────────
                save(assignmentRepository, "Setup CI/CD pipeline", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectA, sprintA1, techDev1,
                                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5), 2);
                save(assignmentRepository, "Configure Vite + Vue 3 project", AssignmentPriority.MEDIUM,
                                AssignmentStatus.COMPLETED, projectA, sprintA1, techDev2,
                                LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 6), 1);
                save(assignmentRepository, "Design token system (colors & fonts)", AssignmentPriority.MEDIUM,
                                AssignmentStatus.COMPLETED, projectA, sprintA1, techDev3,
                                LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 8), 2);
                save(assignmentRepository, "Setup Spring Boot skeleton API", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectA, sprintA1, techDev1,
                                LocalDate.of(2026, 3, 4), LocalDate.of(2026, 3, 10), 3);

                // ── Project A / Sprint 2 (COMPLETED) — total rank = 9 ─────────────────
                save(assignmentRepository, "Implement product listing API", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectA, sprintA2, techDev1,
                                LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 20), 3);
                save(assignmentRepository, "Build product detail page", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectA, sprintA2, techDev2,
                                LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 21), 2);
                save(assignmentRepository, "Implement cart & quantity controls", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectA, sprintA2, techDev4,
                                LocalDate.of(2026, 3, 16), LocalDate.of(2026, 3, 23), 2);
                save(assignmentRepository, "User authentication — JWT flow", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectA, sprintA2, techDev1,
                                LocalDate.of(2026, 3, 17), LocalDate.of(2026, 3, 25), 1);
                save(assignmentRepository, "Write integration tests — product API", AssignmentPriority.LOW,
                                AssignmentStatus.COMPLETED, projectA, sprintA2, techDev2,
                                LocalDate.of(2026, 3, 18), LocalDate.of(2026, 3, 26), 1);

                // ── Project A / Sprint 3 (ACTIVE) — total rank = 9 ────────────────────
                save(assignmentRepository, "Integrate Stripe payment gateway", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectA, sprintA3, techDev3,
                                LocalDate.of(2026, 3, 29), LocalDate.of(2026, 4, 3), 3);
                save(assignmentRepository, "Build order confirmation & receipt page", AssignmentPriority.HIGH,
                                AssignmentStatus.IN_REVIEW, projectA, sprintA3, techDev4,
                                LocalDate.of(2026, 3, 29), null, 2);
                save(assignmentRepository, "Implement checkout address form validation", AssignmentPriority.MEDIUM,
                                AssignmentStatus.IN_REVIEW, projectA, sprintA3, techDev5,
                                LocalDate.of(2026, 3, 30), null, 2);
                save(assignmentRepository, "Add order management admin panel", AssignmentPriority.MEDIUM,
                                AssignmentStatus.IN_PROGRESS, projectA, sprintA3, techDev3,
                                LocalDate.of(2026, 4, 1), null, 2);

                // ── Project B / Sprint 1 (COMPLETED) — total rank = 8 ─────────────────
                save(assignmentRepository, "Define Employee entity & DB schema", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectB, sprintB1, softDev1,
                                LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 8), 3);
                save(assignmentRepository, "Build department management API", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectB, sprintB1, softDev2,
                                LocalDate.of(2026, 3, 5), LocalDate.of(2026, 3, 11), 2);
                save(assignmentRepository, "Create employee list screen", AssignmentPriority.MEDIUM,
                                AssignmentStatus.COMPLETED, projectB, sprintB1, softDev1,
                                LocalDate.of(2026, 3, 8), LocalDate.of(2026, 3, 14), 2);
                save(assignmentRepository, "Write unit tests for employee module", AssignmentPriority.LOW,
                                AssignmentStatus.COMPLETED, projectB, sprintB1, softDev2,
                                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 15), 1);

                // ── Project B / Sprint 2 (COMPLETED) — total rank = 8 ─────────────────
                save(assignmentRepository, "Build leave request REST endpoint", AssignmentPriority.MEDIUM,
                                AssignmentStatus.COMPLETED, projectB, sprintB2, softDev2,
                                LocalDate.of(2026, 3, 17), LocalDate.of(2026, 3, 22), 2);
                save(assignmentRepository, "Implement payroll calculation engine", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectB, sprintB2, softDev3,
                                LocalDate.of(2026, 3, 17), LocalDate.of(2026, 3, 25), 3);
                save(assignmentRepository, "Generate monthly payroll report UI", AssignmentPriority.MEDIUM,
                                AssignmentStatus.COMPLETED, projectB, sprintB2, softDev2,
                                LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 28), 2);
                save(assignmentRepository, "Write integration tests for payroll", AssignmentPriority.LOW,
                                AssignmentStatus.COMPLETED, projectB, sprintB2, softDev3,
                                LocalDate.of(2026, 3, 22), LocalDate.of(2026, 3, 29), 1);

                // ── Project B / Sprint 3 (ACTIVE) — total rank = 8 ────────────────────
                save(assignmentRepository, "Integrate SSO with Azure AD", AssignmentPriority.HIGH,
                                AssignmentStatus.IN_REVIEW, projectB, sprintB3, softDev1,
                                LocalDate.of(2026, 3, 31), null, 3);
                save(assignmentRepository, "Setup Kong API gateway configuration", AssignmentPriority.HIGH,
                                AssignmentStatus.IN_PROGRESS, projectB, sprintB3, softDev3,
                                LocalDate.of(2026, 3, 31), null, 2);
                save(assignmentRepository, "Build email notification service", AssignmentPriority.MEDIUM,
                                AssignmentStatus.IN_PROGRESS, projectB, sprintB3, softDev4,
                                LocalDate.of(2026, 4, 1), null, 2);
                save(assignmentRepository, "Write API integration tests", AssignmentPriority.LOW,
                                AssignmentStatus.TODO, projectB, sprintB3, softDev1,
                                LocalDate.of(2026, 4, 3), null, 1);

                // ── Project C / Sprint 1 (COMPLETED) — total rank = 8 ─────────────────
                save(assignmentRepository, "Integrate Amplitude event SDK", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectC, sprintC1, innovaDev1,
                                LocalDate.of(2026, 3, 5), LocalDate.of(2026, 3, 10), 3);
                save(assignmentRepository, "Define event taxonomy & naming conventions", AssignmentPriority.MEDIUM,
                                AssignmentStatus.COMPLETED, projectC, sprintC1, innovaDev2,
                                LocalDate.of(2026, 3, 6), LocalDate.of(2026, 3, 12), 2);
                save(assignmentRepository, "Setup data pipeline to warehouse", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectC, sprintC1, innovaDev1,
                                LocalDate.of(2026, 3, 8), LocalDate.of(2026, 3, 15), 2);
                save(assignmentRepository, "Write SDK tracking test coverage", AssignmentPriority.LOW,
                                AssignmentStatus.COMPLETED, projectC, sprintC1, innovaDev2,
                                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 17), 1);

                // ── Project C / Sprint 2 (COMPLETED) — total rank = 9 ─────────────────
                save(assignmentRepository, "Build chart component library (D3.js)", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectC, sprintC2, innovaDev2,
                                LocalDate.of(2026, 3, 19), LocalDate.of(2026, 3, 25), 3);
                save(assignmentRepository, "Implement dashboard filter sidebar", AssignmentPriority.MEDIUM,
                                AssignmentStatus.COMPLETED, projectC, sprintC2, innovaDev3,
                                LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 27), 2);
                save(assignmentRepository, "Add date-range picker component", AssignmentPriority.MEDIUM,
                                AssignmentStatus.COMPLETED, projectC, sprintC2, innovaDev2,
                                LocalDate.of(2026, 3, 22), LocalDate.of(2026, 3, 29), 2);
                save(assignmentRepository, "Implement CSV export functionality", AssignmentPriority.LOW,
                                AssignmentStatus.COMPLETED, projectC, sprintC2, innovaDev3,
                                LocalDate.of(2026, 3, 24), LocalDate.of(2026, 3, 31), 1);
                save(assignmentRepository, "Write dashboard UI unit tests", AssignmentPriority.LOW,
                                AssignmentStatus.COMPLETED, projectC, sprintC2, innovaDev2,
                                LocalDate.of(2026, 3, 26), LocalDate.of(2026, 4, 1), 1);

                // ── Project C / Sprint 3 (ACTIVE) — total rank = 9 ────────────────────
                save(assignmentRepository, "Implement WebSocket real-time data feed", AssignmentPriority.HIGH,
                                AssignmentStatus.IN_REVIEW, projectC, sprintC3, innovaDev1,
                                LocalDate.of(2026, 4, 2), null, 3);
                save(assignmentRepository, "Build threshold-based alert engine", AssignmentPriority.HIGH,
                                AssignmentStatus.IN_REVIEW, projectC, sprintC3, innovaDev3,
                                LocalDate.of(2026, 4, 2), null, 3);
                save(assignmentRepository, "Design alert notification UI panel", AssignmentPriority.MEDIUM,
                                AssignmentStatus.IN_PROGRESS, projectC, sprintC3, innovaDev4,
                                LocalDate.of(2026, 4, 3), null, 2);
                save(assignmentRepository, "Write real-time event tests", AssignmentPriority.LOW,
                                AssignmentStatus.TODO, projectC, sprintC3, innovaDev1,
                                LocalDate.of(2026, 4, 5), null, 1);

                System.out.println(
                                "✅  VMS seed data loaded successfully — 3 vendors, 13 developers, 12 sprints, 36 tasks.");
        }

        private void save(
                        AssignmentRepository repo,
                        String name, AssignmentPriority priority, AssignmentStatus status,
                        Project project, Sprint sprint, Users assignee,
                        LocalDate assignedAt, LocalDate completedAt, int rank) {
                Assignment a = new Assignment();
                a.setName(name);
                a.setPriority(priority);
                a.setStatus(status);
                a.setProject(project);
                a.setSprint(sprint);
                a.setAssignee(assignee);
                a.setAssignedAt(assignedAt);
                a.setCompletedAt(completedAt);
                a.setRank(rank);
                a.setIsActive(1);
                repo.save(a);
        }
}
