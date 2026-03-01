package com.example.demo.services.concretes;

import com.example.demo.dto.request.CreateAssignmentRequest;
import com.example.demo.dto.request.UpdateAssignmentStatusRequest;
import com.example.demo.dto.response.AssignmentResponse;
import com.example.demo.entities.concretes.Assignment;
import com.example.demo.entities.concretes.Project;
import com.example.demo.enums.AssignmentStatus;
import com.example.demo.repositories.AssignmentRepository;
import com.example.demo.repositories.ProjectRepository;
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
        // status default TODO is already in entity
        a.setAssignedAt(request.getAssignedAt() != null ? request.getAssignedAt() : LocalDate.now());
        // completedAt stays null

        // ensure active
        a.setIsActive(ACTIVE);

        return toResponse(assignmentRepository.save(a));
    }

    @Override
    public AssignmentResponse getById(Integer id) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + id));

        // hide soft-deleted
        if (a.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Assignment not found: " + id);
        }

        return toResponse(a);
    }

    @Override
    public List<AssignmentResponse> getAll(Integer projectId) {
        // only active assignments
        List<Assignment> list = (projectId == null)
                ? assignmentRepository.findByIsActive(ACTIVE)
                : assignmentRepository.findByProject_IdAndIsActive(projectId, ACTIVE);

        return list.stream().map(this::toResponse).toList();
    }

    @Override
    public AssignmentResponse updateStatus(Integer id, UpdateAssignmentStatusRequest request) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + id));

        // prevent updates on soft-deleted
        if (a.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Assignment not found: " + id);
        }

        AssignmentStatus newStatus = request.getStatus();
        AssignmentStatus oldStatus = a.getStatus();

        if (newStatus == null) {
            throw new IllegalArgumentException("status is required");
        }

        // allow no-op
        if (newStatus == oldStatus) {
            return toResponse(a);
        }

        // ✅ Transition rules: TODO -> IN_PROGRESS -> COMPLETED only
        if (!isAllowedTransition(oldStatus, newStatus)) {
            throw new IllegalArgumentException("Invalid status transition: " + oldStatus + " -> " + newStatus);
        }

        a.setStatus(newStatus);

        // Optional timestamps
        if (newStatus == AssignmentStatus.IN_PROGRESS && a.getAssignedAt() == null) {
            a.setAssignedAt(LocalDate.now());
        }

        // When moving to COMPLETED, set completed_at automatically
        if (newStatus == AssignmentStatus.COMPLETED && a.getCompletedAt() == null) {
            a.setCompletedAt(LocalDate.now());
        }

        return toResponse(assignmentRepository.save(a));
    }

    @Override
    public void delete(Integer id) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + id));

        // soft delete
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
