package com.example.demo.controllers;

import com.example.demo.dto.response.EmployeePerformanceResponse;
import com.example.demo.dto.response.VendorPerformanceResponse;
import com.example.demo.entities.concretes.*;
import com.example.demo.enums.AssignmentStatus;
import com.example.demo.enums.SprintStatus;
import com.example.demo.repositories.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AssignmentRepository assignmentRepository;
    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    // ───────── Task Stats (Pie Chart data) ─────────────────────────────────

    @Operation(summary = "Get task statistics for dashboard pie chart")
    @GetMapping("/stats")
    public Map<String, Object> getStats(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer vendorId,
            @RequestParam(required = false) Integer projectId) {
        List<Assignment> assignments;

        if (userId != null) {
            // Personnel: own tasks
            assignments = assignmentRepository.findByAssignee_IdAndIsActive(userId, 1);
        } else if (vendorId != null) {
            // Vendor Admin: all tasks in vendor's projects
            List<Project> projects = projectRepository.findByVendor_IdAndIsActive(vendorId, 1);
            List<Integer> projectIds = projects.stream().map(Project::getId).toList();
            assignments = new ArrayList<>();
            for (Integer pid : projectIds) {
                assignments.addAll(assignmentRepository.findByProject_IdAndIsActive(pid, 1));
            }
        } else if (projectId != null) {
            assignments = assignmentRepository.findByProject_IdAndIsActive(projectId, 1);
        } else {
            // PM: all tasks
            assignments = assignmentRepository.findByIsActive(1);
        }

        long todo = assignments.stream().filter(a -> a.getStatus() == AssignmentStatus.TODO).count();
        long inProgress = assignments.stream().filter(a -> a.getStatus() == AssignmentStatus.IN_PROGRESS).count();
        long completed = assignments.stream().filter(a -> a.getStatus() == AssignmentStatus.COMPLETED).count();

        // Find active sprint end date for business days calculation
        LocalDate sprintEndDate = null;
        List<Sprint> activeSprints;

        if (vendorId != null) {
            List<Project> projects = projectRepository.findByVendor_IdAndIsActive(vendorId, 1);
            activeSprints = new ArrayList<>();
            for (Project p : projects) {
                sprintRepository.findByProject_IdAndStatusAndIsActive(p.getId(), SprintStatus.ACTIVE, 1)
                        .ifPresent(activeSprints::add);
            }
        } else {
            activeSprints = sprintRepository.findByIsActive(1).stream()
                    .filter(s -> s.getStatus() == SprintStatus.ACTIVE)
                    .toList();
        }

        if (!activeSprints.isEmpty()) {
            // Pick the soonest end date among active sprints
            sprintEndDate = activeSprints.stream()
                    .map(Sprint::getEndDate)
                    .filter(Objects::nonNull)
                    .min(LocalDate::compareTo)
                    .orElse(null);
        }

        int businessDaysLeft = sprintEndDate != null ? countBusinessDays(LocalDate.now(), sprintEndDate) : -1;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todoCount", todo);
        result.put("inProgressCount", inProgress);
        result.put("completedCount", completed);
        result.put("sprintEndDate", sprintEndDate);
        result.put("businessDaysLeft", businessDaysLeft);

        return result;
    }

    // ───────── Vendor Performance (for PM) ─────────────────────────────────

    @Operation(summary = "Get vendor performance stats for PM")
    @GetMapping("/vendor-performance")
    public List<VendorPerformanceResponse> getVendorPerformance() {
        List<Vendor> vendors = vendorRepository.findByIsActive(1);
        List<VendorPerformanceResponse> result = new ArrayList<>();

        for (Vendor vendor : vendors) {
            List<Project> projects = projectRepository.findByVendor_IdAndIsActive(vendor.getId(), 1);
            List<Integer> projectIds = projects.stream().map(Project::getId).toList();

            // Sprints
            List<Sprint> sprints = new ArrayList<>();
            for (Integer pid : projectIds) {
                sprints.addAll(sprintRepository.findByProject_IdAndIsActive(pid, 1));
            }
            int totalSprints = sprints.size();
            int completedSprints = (int) sprints.stream().filter(s -> s.getStatus() == SprintStatus.COMPLETED).count();

            // Tasks
            List<Assignment> tasks = new ArrayList<>();
            for (Integer pid : projectIds) {
                tasks.addAll(assignmentRepository.findByProject_IdAndIsActive(pid, 1));
            }
            int totalTasks = tasks.size();
            int completedTasks = (int) tasks.stream().filter(a -> a.getStatus() == AssignmentStatus.COMPLETED).count();
            int totalPoints = tasks.stream().mapToInt(a -> a.getRank() != null ? a.getRank() : 1).sum();
            int completedPoints = tasks.stream()
                    .filter(a -> a.getStatus() == AssignmentStatus.COMPLETED)
                    .mapToInt(a -> a.getRank() != null ? a.getRank() : 1).sum();

            VendorPerformanceResponse vp = new VendorPerformanceResponse();
            vp.setVendorId(vendor.getId());
            vp.setVendorName(vendor.getVendorName());
            vp.setCompletedSprints(completedSprints);
            vp.setTotalSprints(totalSprints);
            vp.setCompletedTasks(completedTasks);
            vp.setTotalTasks(totalTasks);
            vp.setCompletedPoints(completedPoints);
            vp.setTotalPoints(totalPoints);
            result.add(vp);
        }

        return result;
    }

    // ───────── Employee Performance (for Vendor Admin) ─────────────────────

    @Operation(summary = "Get employee performance stats for vendor admin")
    @GetMapping("/employee-performance")
    public List<EmployeePerformanceResponse> getEmployeePerformance(@RequestParam Integer vendorId) {
        List<Users> employees = userRepository.findByVendor_IdAndIsActive(vendorId, 1);
        List<EmployeePerformanceResponse> result = new ArrayList<>();

        for (Users emp : employees) {
            List<Assignment> tasks = assignmentRepository.findByAssignee_IdAndIsActive(emp.getId(), 1);
            int total = tasks.size();
            int completed = (int) tasks.stream().filter(a -> a.getStatus() == AssignmentStatus.COMPLETED).count();
            int totalPoints = tasks.stream().mapToInt(a -> a.getRank() != null ? a.getRank() : 1).sum();
            int completedPoints = tasks.stream()
                    .filter(a -> a.getStatus() == AssignmentStatus.COMPLETED)
                    .mapToInt(a -> a.getRank() != null ? a.getRank() : 1).sum();

            // Avg completion days = average of (completedAt - assignedAt) in business days
            Double avgDays = null;
            List<Assignment> doneTasks = tasks.stream()
                    .filter(a -> a.getStatus() == AssignmentStatus.COMPLETED && a.getAssignedAt() != null
                            && a.getCompletedAt() != null)
                    .toList();
            if (!doneTasks.isEmpty()) {
                double sum = doneTasks.stream()
                        .mapToLong(a -> ChronoUnit.DAYS.between(a.getAssignedAt(), a.getCompletedAt()))
                        .sum();
                avgDays = Math.round((sum / doneTasks.size()) * 10.0) / 10.0;
            }

            EmployeePerformanceResponse ep = new EmployeePerformanceResponse();
            ep.setUserId(emp.getId());
            ep.setFullName(emp.getPersonnelName() + " " + emp.getPersonnelSurname());
            ep.setCompletedTasks(completed);
            ep.setTotalTasks(total);
            ep.setCompletedPoints(completedPoints);
            ep.setTotalPoints(totalPoints);
            ep.setAvgCompletionDays(avgDays);
            result.add(ep);
        }

        return result;
    }

    // ───────── Helper ──────────────────────────────────────────────────────

    private int countBusinessDays(LocalDate start, LocalDate end) {
        if (start.isAfter(end))
            return 0;
        int count = 0;
        LocalDate d = start;
        while (!d.isAfter(end)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
            d = d.plusDays(1);
        }
        return count;
    }
}
