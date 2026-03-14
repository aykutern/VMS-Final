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
                productManagerRepository.save(pm1);

                ProductManager pm2 = new ProductManager();
                pm2.setName("Bob Williams");
                pm2.setTitle(PersonnelTitle.PRODUCT_OWNER);
                pm2.setIsActive(1);
                productManagerRepository.save(pm2);

                // ── Users ─────────────────────────────────────────────────────────────
                // PM
                Users pmUser = new Users();
                pmUser.setUsername("pm_user");
                pmUser.setPassword(passwordEncoder.encode("Password1"));
                pmUser.setEmail("pm@vms.com");
                pmUser.setPersonnelName("Alice");
                pmUser.setPersonnelSurname("Johnson");
                pmUser.setUserType(UserType.MANAGER);
                pmUser.setIsActive(1);
                pmUser = userRepository.save(pmUser);

                // TechCorp admin
                Users vendorAdmin = new Users();
                vendorAdmin.setUsername("vendor_admin");
                vendorAdmin.setPassword(passwordEncoder.encode("Password1"));
                vendorAdmin.setEmail("vendor.admin@techcorp.com");
                vendorAdmin.setPersonnelName("Carlos");
                vendorAdmin.setPersonnelSurname("Rivera");
                vendorAdmin.setUserType(UserType.VENDOR_ADMIN);
                vendorAdmin.setVendor(techCorp);
                vendorAdmin.setIsActive(1);
                vendorAdmin = userRepository.save(vendorAdmin);

                // TechCorp developer 1
                Users developer = new Users();
                developer.setUsername("tech_dev");
                developer.setPassword(passwordEncoder.encode("Password1"));
                developer.setEmail("dev@techcorp.com");
                developer.setPersonnelName("Diana");
                developer.setPersonnelSurname("Chen");
                developer.setUserType(UserType.PERSONNEL);
                developer.setVendor(techCorp);
                developer.setIsActive(1);
                developer = userRepository.save(developer);

                // TechCorp developer 2
                Users developer3 = new Users();
                developer3.setUsername("tech_dev2");
                developer3.setPassword(passwordEncoder.encode("Password1"));
                developer3.setEmail("dev2@techcorp.com");
                developer3.setPersonnelName("James");
                developer3.setPersonnelSurname("Kim");
                developer3.setUserType(UserType.PERSONNEL);
                developer3.setVendor(techCorp);
                developer3.setIsActive(1);
                developer3 = userRepository.save(developer3);

                // SoftSolutions admin
                Users vendorAdmin2 = new Users();
                vendorAdmin2.setUsername("soft_admin");
                vendorAdmin2.setPassword(passwordEncoder.encode("Password1"));
                vendorAdmin2.setEmail("admin@softsolutions.com");
                vendorAdmin2.setPersonnelName("Maria");
                vendorAdmin2.setPersonnelSurname("Lopez");
                vendorAdmin2.setUserType(UserType.VENDOR_ADMIN);
                vendorAdmin2.setVendor(softSolutions);
                vendorAdmin2.setIsActive(1);
                vendorAdmin2 = userRepository.save(vendorAdmin2);

                // SoftSolutions developer
                Users developer2 = new Users();
                developer2.setUsername("soft_dev");
                developer2.setPassword(passwordEncoder.encode("Password1"));
                developer2.setEmail("dev@softsolutions.com");
                developer2.setPersonnelName("Ethan");
                developer2.setPersonnelSurname("Park");
                developer2.setUserType(UserType.PERSONNEL);
                developer2.setVendor(softSolutions);
                developer2.setIsActive(1);
                developer2 = userRepository.save(developer2);

                // InnovaLabs admin
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

                // InnovaLabs developer
                Users innovaDev = new Users();
                innovaDev.setUsername("innova_dev");
                innovaDev.setPassword(passwordEncoder.encode("Password1"));
                innovaDev.setEmail("dev@innovalabs.com");
                innovaDev.setPersonnelName("Noah");
                innovaDev.setPersonnelSurname("Taylor");
                innovaDev.setUserType(UserType.PERSONNEL);
                innovaDev.setVendor(innovaLabs);
                innovaDev.setIsActive(1);
                innovaDev = userRepository.save(innovaDev);

                // ── Projects ──────────────────────────────────────────────────────────
                Project projectA = new Project();
                projectA.setProjectName("E-Commerce Platform Redesign");
                projectA.setVendor(techCorp);
                projectA.setProjectManager(pmUser);
                projectA.setIsActive(1);
                projectA = projectRepository.save(projectA);

                Project projectB = new Project();
                projectB.setProjectName("HR Management System");
                projectB.setVendor(softSolutions);
                projectB.setProjectManager(pmUser);
                projectB.setIsActive(1);
                projectB = projectRepository.save(projectB);

                Project projectC = new Project();
                projectC.setProjectName("Mobile Analytics Dashboard");
                projectC.setVendor(innovaLabs);
                projectC.setProjectManager(pmUser);
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

                // ── Sprints ───────────────────────────────────────────────────────────
                // Project A sprints
                Sprint sprintA1 = new Sprint();
                sprintA1.setSprintName("Sprint 1 — Foundation");
                sprintA1.setStartDate(LocalDate.of(2026, 3, 1));
                sprintA1.setEndDate(LocalDate.of(2026, 3, 14));
                sprintA1.setGoal("Set up project infrastructure, design system, and core API scaffolding.");
                sprintA1.setStatus(SprintStatus.COMPLETED);
                sprintA1.setProject(projectA);
                sprintA1.setIsActive(1);
                sprintA1 = sprintRepository.save(sprintA1);

                Sprint sprintA2 = new Sprint();
                sprintA2.setSprintName("Sprint 2 — Core Features");
                sprintA2.setStartDate(LocalDate.of(2026, 3, 15));
                sprintA2.setEndDate(LocalDate.of(2026, 3, 28));
                sprintA2.setGoal("Implement product catalog, cart system, and user authentication.");
                sprintA2.setStatus(SprintStatus.ACTIVE);
                sprintA2.setProject(projectA);
                sprintA2.setIsActive(1);
                sprintA2 = sprintRepository.save(sprintA2);

                Sprint sprintA3 = new Sprint();
                sprintA3.setSprintName("Sprint 3 — Payments & Checkout");
                sprintA3.setStartDate(LocalDate.of(2026, 3, 29));
                sprintA3.setEndDate(LocalDate.of(2026, 4, 11));
                sprintA3.setGoal("Integrate payment gateway, order management, and checkout flow.");
                sprintA3.setStatus(SprintStatus.PLANNED);
                sprintA3.setProject(projectA);
                sprintA3.setIsActive(1);
                sprintRepository.save(sprintA3);

                // Project B sprints
                Sprint sprintB1 = new Sprint();
                sprintB1.setSprintName("Sprint 1 — Data Model");
                sprintB1.setStartDate(LocalDate.of(2026, 3, 3));
                sprintB1.setEndDate(LocalDate.of(2026, 3, 17));
                sprintB1.setGoal("Design and implement the employee and department data models.");
                sprintB1.setStatus(SprintStatus.ACTIVE);
                sprintB1.setProject(projectB);
                sprintB1.setIsActive(1);
                sprintB1 = sprintRepository.save(sprintB1);

                Sprint sprintB2 = new Sprint();
                sprintB2.setSprintName("Sprint 2 — Reporting");
                sprintB2.setStartDate(LocalDate.of(2026, 3, 18));
                sprintB2.setEndDate(LocalDate.of(2026, 3, 31));
                sprintB2.setGoal("Build payroll reports and leave management features.");
                sprintB2.setStatus(SprintStatus.PLANNED);
                sprintB2.setProject(projectB);
                sprintB2.setIsActive(1);
                sprintRepository.save(sprintB2);

                // Project C sprints
                Sprint sprintC1 = new Sprint();
                sprintC1.setSprintName("Sprint 1 — SDK Integration");
                sprintC1.setStartDate(LocalDate.of(2026, 3, 5));
                sprintC1.setEndDate(LocalDate.of(2026, 3, 19));
                sprintC1.setGoal("Integrate Amplitude SDK and set up event tracking pipeline.");
                sprintC1.setStatus(SprintStatus.ACTIVE);
                sprintC1.setProject(projectC);
                sprintC1.setIsActive(1);
                sprintC1 = sprintRepository.save(sprintC1);

                Sprint sprintC2 = new Sprint();
                sprintC2.setSprintName("Sprint 2 — Dashboard UI");
                sprintC2.setStartDate(LocalDate.of(2026, 3, 20));
                sprintC2.setEndDate(LocalDate.of(2026, 4, 3));
                sprintC2.setGoal("Build the interactive analytics dashboard with charts and filters.");
                sprintC2.setStatus(SprintStatus.PLANNED);
                sprintC2.setProject(projectC);
                sprintC2.setIsActive(1);
                sprintRepository.save(sprintC2);

                // ── Assignments — Project A (TechCorp) ───────────────────────────────
                save(assignmentRepository, "Setup CI/CD pipeline", AssignmentPriority.HIGH, AssignmentStatus.COMPLETED,
                                projectA, sprintA1, developer, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5));
                save(assignmentRepository, "Configure Vite + Vue 3 project", AssignmentPriority.MEDIUM,
                                AssignmentStatus.COMPLETED, projectA, sprintA1, developer3, LocalDate.of(2026, 3, 2),
                                LocalDate.of(2026, 3, 6));
                save(assignmentRepository, "Design token system (colors/fonts)", AssignmentPriority.MEDIUM,
                                AssignmentStatus.COMPLETED, projectA, sprintA1, developer, LocalDate.of(2026, 3, 3),
                                LocalDate.of(2026, 3, 8));
                save(assignmentRepository, "Setup Spring Boot skeleton API", AssignmentPriority.HIGH,
                                AssignmentStatus.COMPLETED, projectA, sprintA1, developer3, LocalDate.of(2026, 3, 4),
                                LocalDate.of(2026, 3, 10));

                save(assignmentRepository, "Implement product listing API", AssignmentPriority.HIGH,
                                AssignmentStatus.IN_PROGRESS, projectA, sprintA2, developer, LocalDate.of(2026, 3, 15),
                                null);
                save(assignmentRepository, "Build product detail page", AssignmentPriority.HIGH,
                                AssignmentStatus.IN_PROGRESS, projectA, sprintA2, developer3, LocalDate.of(2026, 3, 15),
                                null);
                save(assignmentRepository, "Implement cart & quantity controls", AssignmentPriority.HIGH,
                                AssignmentStatus.TODO, projectA, sprintA2, developer, LocalDate.of(2026, 3, 16), null);
                save(assignmentRepository, "Design homepage wireframes", AssignmentPriority.MEDIUM,
                                AssignmentStatus.TODO, projectA, sprintA2, developer3, LocalDate.of(2026, 3, 16), null);
                save(assignmentRepository, "User authentication — JWT flow", AssignmentPriority.HIGH,
                                AssignmentStatus.TODO, projectA, sprintA2, developer, LocalDate.of(2026, 3, 17), null);
                save(assignmentRepository, "Write integration tests — product API", AssignmentPriority.LOW,
                                AssignmentStatus.TODO, projectA, sprintA2, developer3, LocalDate.of(2026, 3, 18), null);

                // ── Assignments — Project B (SoftSolutions) ──────────────────────────
                save(assignmentRepository, "Define Employee entity & DB schema", AssignmentPriority.HIGH,
                                AssignmentStatus.IN_PROGRESS, projectB, sprintB1, developer2, LocalDate.of(2026, 3, 3),
                                null);
                save(assignmentRepository, "Build department management API", AssignmentPriority.HIGH,
                                AssignmentStatus.IN_PROGRESS, projectB, sprintB1, developer2, LocalDate.of(2026, 3, 5),
                                null);
                save(assignmentRepository, "Create employee list screen", AssignmentPriority.MEDIUM,
                                AssignmentStatus.TODO, projectB, sprintB1, developer2, LocalDate.of(2026, 3, 8), null);
                save(assignmentRepository, "Build leave request REST endpoint", AssignmentPriority.MEDIUM,
                                AssignmentStatus.TODO, projectB, sprintB1, developer2, LocalDate.of(2026, 3, 10), null);
                save(assignmentRepository, "Write unit tests for payroll module", AssignmentPriority.LOW,
                                AssignmentStatus.TODO, projectB, sprintB1, developer2, LocalDate.of(2026, 3, 12), null);

                // ── Assignments — Project C (InnovaLabs) ─────────────────────────────
                save(assignmentRepository, "Integrate Amplitude event SDK", AssignmentPriority.HIGH,
                                AssignmentStatus.IN_PROGRESS, projectC, sprintC1, innovaDev, LocalDate.of(2026, 3, 5),
                                null);
                save(assignmentRepository, "Define event taxonomy & naming", AssignmentPriority.MEDIUM,
                                AssignmentStatus.IN_PROGRESS, projectC, sprintC1, innovaDev, LocalDate.of(2026, 3, 6),
                                null);
                save(assignmentRepository, "Setup data pipeline to warehouse", AssignmentPriority.HIGH,
                                AssignmentStatus.TODO, projectC, sprintC1, innovaDev, LocalDate.of(2026, 3, 8), null);
                save(assignmentRepository, "Write tracking test coverage", AssignmentPriority.LOW,
                                AssignmentStatus.TODO, projectC, sprintC1, innovaDev, LocalDate.of(2026, 3, 10), null);

                System.out.println("✅  VMS seed data loaded successfully.");
        }

        private void save(
                        AssignmentRepository repo,
                        String name, AssignmentPriority priority, AssignmentStatus status,
                        Project project, Sprint sprint, Users assignee,
                        LocalDate assignedAt, LocalDate completedAt) {
                Assignment a = new Assignment();
                a.setName(name);
                a.setPriority(priority);
                a.setStatus(status);
                a.setProject(project);
                a.setSprint(sprint);
                a.setAssignee(assignee);
                a.setAssignedAt(assignedAt);
                a.setCompletedAt(completedAt);
                a.setIsActive(1);
                repo.save(a);
        }
}
