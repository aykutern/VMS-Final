package com.example.demo.services.concretes;

import com.example.demo.dto.request.CreateAssignmentRequest;
import com.example.demo.dto.request.UpdateAssignmentStatusRequest;
import com.example.demo.dto.response.AssignmentResponse;
import com.example.demo.entities.concretes.Assignment;
import com.example.demo.entities.concretes.Project;
import com.example.demo.entities.concretes.Sprint;
import com.example.demo.entities.concretes.Users;
import com.example.demo.enums.AssignmentStatus;
import com.example.demo.repositories.AssignmentRepository;
import com.example.demo.repositories.ProjectRepository;
import com.example.demo.repositories.SprintRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.abstracts.AssignmentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;

    private static final int ACTIVE = 1;
    private static final int DELETED = 0;

    @Override
    @Transactional
    public AssignmentResponse create(CreateAssignmentRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + request.getProjectId()));

        Assignment a = new Assignment();
        a.setProject(project);
        a.setName(request.getName());
        a.setPriority(request.getPriority());
        a.setRank(request.getRank() != null ? request.getRank() : 1);
        a.setAssignedAt(request.getAssignedAt() != null ? request.getAssignedAt() : LocalDate.now());

        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new EntityNotFoundException("Sprint not found: " + request.getSprintId()));

            // Validate sprint capacity
            int currentLoad = assignmentRepository.findBySprint_IdAndIsActive(sprint.getId(), ACTIVE)
                    .stream().mapToInt(t -> t.getRank() != null ? t.getRank() : 1).sum();
            int newRank = request.getRank() != null ? request.getRank() : 1;
            if (currentLoad + newRank > sprint.getMaxCapacity()) {
                throw new IllegalArgumentException(
                    "Sprint capacity exceeded. Current load: " + currentLoad
                    + ", task rank: " + newRank
                    + ", max capacity: " + sprint.getMaxCapacity());
            }

            a.setSprint(sprint);
        }

        if (request.getAssigneeId() != null) {
            Users assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.getAssigneeId()));
            a.setAssignee(assignee);
        }

        a.setIsActive(ACTIVE);
        return toResponse(assignmentRepository.save(a));
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getById(Integer id) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + id));

        if (a.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Assignment not found: " + id);
        }

        return toResponse(a);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAll(Integer projectId, Integer sprintId, Integer vendorId, Integer assigneeId) {
        List<Assignment> list;

        if (assigneeId != null) {
            list = assignmentRepository.findByAssignee_IdAndIsActive(assigneeId, ACTIVE);
        } else if (sprintId != null) {
            list = assignmentRepository.findBySprint_IdAndIsActive(sprintId, ACTIVE);
        } else if (projectId != null) {
            list = assignmentRepository.findByProject_IdAndIsActive(projectId, ACTIVE);
        } else if (vendorId != null) {
            list = assignmentRepository.findByProject_Vendor_IdAndIsActive(vendorId, ACTIVE);
        } else {
            list = assignmentRepository.findByIsActive(ACTIVE);
        }

        return list.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public AssignmentResponse updateStatus(Integer id, UpdateAssignmentStatusRequest request) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + id));

        if (a.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Assignment not found: " + id);
        }

        // --- Role Check ---
        String currentUsername = com.example.demo.config.CurrentUser.get();
        if (currentUsername != null) {
            Users currentUser = userRepository.findByUsername(currentUsername)
                    .orElse(null);
            
            if (currentUser != null && currentUser.getUserType() == com.example.demo.enums.UserType.MANAGER) {
                // Product Managers cannot approve or reject tasks (move to COMPLETED or back to IN_PROGRESS from IN_REVIEW)
                // They also shouldn't be the ones triggering the review process, but we focus on the "Reviewer" role.
                if (request.getStatus() == AssignmentStatus.COMPLETED || a.getStatus() == AssignmentStatus.IN_REVIEW) {
                    throw new IllegalArgumentException("Product Managers are not authorized to process task reviews.");
                }
            }
        }
        // ------------------

        AssignmentStatus newStatus = request.getStatus();
        AssignmentStatus oldStatus = a.getStatus();

        if (newStatus == null) {
            throw new IllegalArgumentException("status is required");
        }

        if (newStatus == oldStatus) {
            return toResponse(a);
        }

        if (!isAllowedTransition(oldStatus, newStatus)) {
            throw new IllegalArgumentException("Invalid status transition: " + oldStatus + " -> " + newStatus);
        }

        a.setStatus(newStatus);

        if (request.getRejectionReason() != null) {
            a.setRejectionReason(request.getRejectionReason());
        }

        if (newStatus == AssignmentStatus.IN_PROGRESS && a.getAssignedAt() == null) {
            a.setAssignedAt(LocalDate.now());
        }

        if (newStatus == AssignmentStatus.COMPLETED && a.getCompletedAt() == null) {
            a.setCompletedAt(LocalDate.now());
        }

        return toResponse(assignmentRepository.save(a));
    }

    @Override
    public void delete(Integer id) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + id));

        if (a.getIsActive() == ACTIVE) {
            a.setIsActive(DELETED);
            assignmentRepository.save(a);
        }
    }

    private boolean isAllowedTransition(AssignmentStatus from, AssignmentStatus to) {
        // Allow any direction — forward and backward
        return from != to;
    }

    private AssignmentResponse toResponse(Assignment a) {
        AssignmentResponse r = new AssignmentResponse();
        r.setId(a.getId());
        r.setProjectId(a.getProject() != null ? a.getProject().getId() : null);
        r.setProjectName(a.getProject() != null ? a.getProject().getProjectName() : null);
        r.setSprintId(a.getSprint() != null ? a.getSprint().getId() : null);
        r.setAssigneeId(a.getAssignee() != null ? a.getAssignee().getId() : null);
        r.setAssigneeName(a.getAssignee() != null
                ? a.getAssignee().getPersonnelName() + " " + a.getAssignee().getPersonnelSurname()
                : null);
        r.setName(a.getName());
        r.setPriority(a.getPriority());
        r.setStatus(a.getStatus());
        r.setRank(a.getRank());
        r.setAssignedAt(a.getAssignedAt());
        r.setCompletedAt(a.getCompletedAt());
        r.setRejectionReason(a.getRejectionReason());

        r.setCreatedAt(a.getCreatedAt());
        r.setUpdatedAt(a.getUpdatedAt());
        r.setCreatedBy(a.getCreatedBy());
        r.setUpdatedBy(a.getUpdatedBy());
        r.setIsActive(a.getIsActive());
        return r;
    }
}
