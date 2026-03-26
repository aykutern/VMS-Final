package com.example.demo.services.concretes;

import com.example.demo.dto.request.CreateSprintRequest;
import com.example.demo.dto.request.UpdateSprintRequest;
import com.example.demo.dto.response.SprintResponse;
import com.example.demo.entities.concretes.Project;
import com.example.demo.entities.concretes.Sprint;
import com.example.demo.entities.concretes.Users;
import com.example.demo.enums.SprintStatus;
import com.example.demo.repositories.AssignmentRepository;
import com.example.demo.repositories.ProjectRepository;
import com.example.demo.repositories.SprintRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.abstracts.SprintService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;

    private static final int ACTIVE = 1;
    private static final int DELETED = 0;

    @Override
    @Transactional
    public SprintResponse create(CreateSprintRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + request.getProjectId()));

        Sprint sprint = new Sprint();
        sprint.setProject(project);
        sprint.setSprintName(request.getSprintName());
        sprint.setStartDate(request.getStartDate());
        // Auto-calculate endDate: startDate + 13 days = 2 weeks (14 days inclusive)
        sprint.setEndDate(request.getStartDate().plusDays(13));
        sprint.setGoal(request.getGoal());
        sprint.setStatus(request.getStatus());
        sprint.setMaxCapacity(10);
        sprint.setIsActive(ACTIVE);

        // Assign members (validate each isn't already in an active sprint)
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            Set<Users> members = resolveAndValidateMembers(request.getMemberIds(), request.getStatus(), -1);
            sprint.setMembers(members);
        }

        return toResponse(sprintRepository.save(sprint));
    }

    @Override
    @Transactional(readOnly = true)
    public SprintResponse getById(Integer id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found: " + id));

        if (sprint.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Sprint not found: " + id);
        }

        return toResponse(sprint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintResponse> getAll(Integer projectId) {
        List<Sprint> sprints;

        if (projectId != null) {
            sprints = sprintRepository.findByProject_IdAndIsActive(projectId, ACTIVE);
        } else {
            sprints = sprintRepository.findByIsActive(ACTIVE);
        }

        return sprints.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SprintResponse update(Integer id, UpdateSprintRequest request) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found: " + id));

        if (sprint.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Sprint not found: " + id);
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + request.getProjectId()));

        sprint.setProject(project);
        sprint.setSprintName(request.getSprintName());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setGoal(request.getGoal());
        sprint.setStatus(request.getStatus());

        return toResponse(sprintRepository.save(sprint));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found: " + id));

        if (sprint.getIsActive() == ACTIVE) {
            sprint.setIsActive(DELETED);
            sprintRepository.save(sprint);
        }
    }

    /**
     * Resolves user IDs to Users and validates that none are already
     * in a different ACTIVE sprint (enforces 1 active sprint per developer).
     */
    private Set<Users> resolveAndValidateMembers(List<Integer> memberIds, SprintStatus newStatus, Integer currentSprintId) {
        Set<Users> members = new HashSet<>();
        for (Integer uid : memberIds) {
            Users user = userRepository.findById(uid)
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + uid));

            // Only enforce when the sprint being created/updated is ACTIVE
            if (newStatus == SprintStatus.ACTIVE) {
                boolean conflict = sprintRepository.existsByMembers_IdAndStatusAndIsActiveAndIdNot(
                        uid, SprintStatus.ACTIVE, ACTIVE, currentSprintId == -1 ? Integer.MAX_VALUE : currentSprintId);
                if (conflict) {
                    throw new IllegalArgumentException(
                        user.getPersonnelName() + " " + user.getPersonnelSurname()
                        + " is already assigned to another active sprint.");
                }
            }
            members.add(user);
        }
        return members;
    }

    private SprintResponse toResponse(Sprint sprint) {
        SprintResponse response = new SprintResponse();
        response.setId(sprint.getId());
        if (sprint.getProject() != null) {
            response.setProjectId(sprint.getProject().getId());
            response.setProjectName(sprint.getProject().getProjectName());
        }
        response.setSprintName(sprint.getSprintName());
        response.setStartDate(sprint.getStartDate());
        response.setEndDate(sprint.getEndDate());
        response.setGoal(sprint.getGoal());
        response.setStatus(sprint.getStatus());
        response.setMaxCapacity(sprint.getMaxCapacity());

        // Compute current load (sum of task ranks in this sprint)
        int currentLoad = assignmentRepository.findBySprint_IdAndIsActive(sprint.getId(), ACTIVE)
                .stream().mapToInt(a -> a.getRank() != null ? a.getRank() : 1).sum();
        response.setCurrentLoad(currentLoad);

        // Map members
        if (sprint.getMembers() != null) {
            response.setMembers(sprint.getMembers().stream()
                .map(u -> new SprintResponse.MemberDto(
                    u.getId(),
                    u.getPersonnelName() + " " + u.getPersonnelSurname()
                ))
                .collect(Collectors.toList()));
        }

        response.setCreatedAt(sprint.getCreatedAt());
        response.setUpdatedAt(sprint.getUpdatedAt());
        response.setCreatedBy(sprint.getCreatedBy());
        response.setUpdatedBy(sprint.getUpdatedBy());
        response.setIsActive(sprint.getIsActive());
        return response;
    }
}
