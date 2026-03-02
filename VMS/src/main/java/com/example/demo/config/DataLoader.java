package com.example.demo.config;

import com.example.demo.entities.concretes.*;
import com.example.demo.enums.*;
import com.example.demo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
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

    @Override
    public void run(String... args) {
        // Run seed only if DB is empty
        if (userRepository.count() > 0) {
            return;
        }

        // ── Vendors ──────────────────────────────────────────────────────────
        Vendor techCorp = new Vendor();
        techCorp.setVendorName("TechCorp Solutions");
        techCorp.setIsActive(1);
        techCorp = vendorRepository.save(techCorp);

        Vendor softSolutions = new Vendor();
        softSolutions.setVendorName("SoftSolutions Inc.");
        softSolutions.setIsActive(1);
        softSolutions = vendorRepository.save(softSolutions);

        // ── Product Managers ──────────────────────────────────────────────────
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
        // PM User (Product Manager role)
        Users pmUser = new Users();
        pmUser.setUsername("pm_user");
        pmUser.setPassword("Password1");
        pmUser.setEmail("pm@vms.com");
        pmUser.setPersonnelName("Alice");
        pmUser.setPersonnelSurname("Johnson");
        pmUser.setUserType(UserType.MANAGER);
        pmUser.setIsActive(1);
        pmUser = userRepository.save(pmUser);

        // Vendor Admin User (manages TechCorp)
        Users vendorAdmin = new Users();
        vendorAdmin.setUsername("vendor_admin");
        vendorAdmin.setPassword("Password1");
        vendorAdmin.setEmail("vendor.admin@techcorp.com");
        vendorAdmin.setPersonnelName("Carlos");
        vendorAdmin.setPersonnelSurname("Rivera");
        vendorAdmin.setUserType(UserType.VENDOR_ADMIN);
        vendorAdmin.setVendor(techCorp);
        vendorAdmin.setIsActive(1);
        vendorAdmin = userRepository.save(vendorAdmin);

        // Personnel User (developer at TechCorp)
        Users developer = new Users();
        developer.setUsername("tech_dev");
        developer.setPassword("Password1");
        developer.setEmail("dev@techcorp.com");
        developer.setPersonnelName("Diana");
        developer.setPersonnelSurname("Chen");
        developer.setUserType(UserType.PERSONNEL);
        developer.setVendor(techCorp);
        developer.setIsActive(1);
        developer = userRepository.save(developer);

        // Personnel User 2 (developer at SoftSolutions)
        Users developer2 = new Users();
        developer2.setUsername("soft_dev");
        developer2.setPassword("Password1");
        developer2.setEmail("dev@softsolutions.com");
        developer2.setPersonnelName("Ethan");
        developer2.setPersonnelSurname("Park");
        developer2.setUserType(UserType.PERSONNEL);
        developer2.setVendor(softSolutions);
        developer2.setIsActive(1);
        developer2 = userRepository.save(developer2);

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

        // ── Announcements ─────────────────────────────────────────────────────
        Announcement ann1 = new Announcement();
        ann1.setTitle("Kick-off Meeting Scheduled");
        ann1.setContent(
                "The kick-off meeting for the E-Commerce Platform Redesign project is scheduled for March 10th. All team members are expected to attend. Please review the project brief before the meeting.");
        ann1.setProject(projectA);
        ann1.setIsActive(1);
        announcementRepository.save(ann1);

        Announcement ann2 = new Announcement();
        ann2.setTitle("New Design Assets Available");
        ann2.setContent(
                "The UI/UX design assets for the first sprint have been uploaded to the shared drive. Developers can now start referencing them for implementation.");
        ann2.setProject(projectA);
        ann2.setIsActive(1);
        announcementRepository.save(ann2);

        Announcement ann3 = new Announcement();
        ann3.setTitle("HR System Requirements Finalized");
        ann3.setContent(
                "The business requirements document for the HR Management System has been finalized and approved by stakeholders. Development can proceed as planned.");
        ann3.setProject(projectB);
        ann3.setIsActive(1);
        announcementRepository.save(ann3);

        // ── Sprints ───────────────────────────────────────────────────────────
        Sprint sprintA1 = new Sprint();
        sprintA1.setSprintName("Sprint 1 — Foundation");
        sprintA1.setStartDate(LocalDate.of(2026, 3, 1));
        sprintA1.setEndDate(LocalDate.of(2026, 3, 14));
        sprintA1.setGoal("Set up project infrastructure, design system, and core API scaffolding.");
        sprintA1.setStatus(SprintStatus.ACTIVE);
        sprintA1.setProject(projectA);
        sprintA1.setIsActive(1);
        sprintA1 = sprintRepository.save(sprintA1);

        Sprint sprintA2 = new Sprint();
        sprintA2.setSprintName("Sprint 2 — Core Features");
        sprintA2.setStartDate(LocalDate.of(2026, 3, 15));
        sprintA2.setEndDate(LocalDate.of(2026, 3, 28));
        sprintA2.setGoal("Implement product catalog, cart system, and user authentication.");
        sprintA2.setStatus(SprintStatus.PLANNED);
        sprintA2.setProject(projectA);
        sprintA2.setIsActive(1);
        sprintRepository.save(sprintA2);

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

        // ── Assignments ───────────────────────────────────────────────────────
        Assignment task1 = new Assignment();
        task1.setName("Setup CI/CD pipeline");
        task1.setPriority(AssignmentPriority.HIGH);
        task1.setStatus(AssignmentStatus.COMPLETED);
        task1.setProject(projectA);
        task1.setAssignedAt(LocalDate.of(2026, 3, 1));
        task1.setCompletedAt(LocalDate.of(2026, 3, 5));
        task1.setIsActive(1);
        assignmentRepository.save(task1);

        Assignment task2 = new Assignment();
        task2.setName("Implement product listing API");
        task2.setPriority(AssignmentPriority.HIGH);
        task2.setStatus(AssignmentStatus.IN_PROGRESS);
        task2.setProject(projectA);
        task2.setAssignedAt(LocalDate.of(2026, 3, 6));
        task2.setIsActive(1);
        assignmentRepository.save(task2);

        Assignment task3 = new Assignment();
        task3.setName("Design homepage wireframes");
        task3.setPriority(AssignmentPriority.MEDIUM);
        task3.setStatus(AssignmentStatus.TODO);
        task3.setProject(projectA);
        task3.setAssignedAt(LocalDate.of(2026, 3, 8));
        task3.setIsActive(1);
        assignmentRepository.save(task3);

        Assignment task4 = new Assignment();
        task4.setName("Define Employee entity and DB schema");
        task4.setPriority(AssignmentPriority.HIGH);
        task4.setStatus(AssignmentStatus.IN_PROGRESS);
        task4.setProject(projectB);
        task4.setAssignedAt(LocalDate.of(2026, 3, 3));
        task4.setIsActive(1);
        assignmentRepository.save(task4);

        Assignment task5 = new Assignment();
        task5.setName("Build leave request REST endpoint");
        task5.setPriority(AssignmentPriority.MEDIUM);
        task5.setStatus(AssignmentStatus.TODO);
        task5.setProject(projectB);
        task5.setAssignedAt(LocalDate.of(2026, 3, 10));
        task5.setIsActive(1);
        assignmentRepository.save(task5);

        Assignment task6 = new Assignment();
        task6.setName("Write unit tests for payroll module");
        task6.setPriority(AssignmentPriority.LOW);
        task6.setStatus(AssignmentStatus.TODO);
        task6.setProject(projectB);
        task6.setAssignedAt(LocalDate.of(2026, 3, 12));
        task6.setIsActive(1);
        assignmentRepository.save(task6);

        System.out.println("✅  VMS seed data loaded successfully.");
    }
}
