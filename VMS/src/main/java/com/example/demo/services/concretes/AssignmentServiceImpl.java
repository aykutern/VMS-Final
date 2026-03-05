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
    public AssignmentResponse create(CreateAssignmentRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + request.getProjectId()));

        Assignment a = new Assignment();
        a.setProject(project);
        a.setName(request.getName());
        a.setPriority(request.getPriority());
        a.setAssignedAt(request.getAssignedAt() != null ? request.getAssignedAt() : LocalDate.now());

        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new EntityNotFoundException("Sprint not found: " + request.getSprintId()));
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
    public AssignmentResponse getById(Integer id) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + id));

        if (a.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Assignment not found: " + id);
        }

        return toResponse(a);
    }

    @Override
    public List<AssignmentResponse> getAll(Integer projectId, Integer sprintId) {
        List<Assignment> list;

        if (sprintId != null) {
            list = assignmentRepository.findBySprint_IdAndIsActive(sprintId, ACTIVE);
        } else if (projectId != null) {
            list = assignmentRepository.findByProject_IdAndIsActive(projectId, ACTIVE);
        } else {
            list = assignmentRepository.findByIsActive(ACTIVE);
        }

        return list.stream().map(this::toResponse).toList();
    }

    @Override
    public AssignmentResponse updateStatus(Integer id, UpdateAssignmentStatusRequest request) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + id));

        if (a.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Assignment not found: " + id);
        }

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
        return switch (from) {
            case TODO -> to == AssignmentStatus.IN_PROGRESS;
            case IN_PROGRESS -> to == AssignmentStatus.COMPLETED;
            case COMPLETED -> false;
        };
    }

    private AssignmentResponse toResponse(Assignment a) {
        AssignmentResponse r = new AssignmentResponse();
        r.setId(a.getId());
        r.setProjectId(a.getProject() != null ? a.getProject().getId() : null);
        r.setSprintId(a.getSprint() != null ? a.getSprint().getId() : null);
        r.setAssigneeId(a.getAssignee() != null ? a.getAssignee().getId() : null);
        r.setAssigneeName(a.getAssignee() != null
                ? a.getAssignee().getPersonnelName() + " " + a.getAssignee().getPersonnelSurname()
                : null);
        r.setName(a.getName());
        r.setPriority(a.getPriority());
        r.setStatus(a.getStatus());
        r.setAssignedAt(a.getAssignedAt());
        r.setCompletedAt(a.getCompletedAt());

        r.setCreatedAt(a.getCreatedAt());
        r.setUpdatedAt(a.getUpdatedAt());
        r.setCreatedBy(a.getCreatedBy());
        r.setUpdatedBy(a.getUpdatedBy());
        r.setIsActive(a.getIsActive());
        return r;
    }
}
