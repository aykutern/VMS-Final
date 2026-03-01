package com.example.demo.services.concretes;

import com.example.demo.dto.request.CreateSprintRequest;
import com.example.demo.dto.request.UpdateSprintRequest;
import com.example.demo.dto.response.SprintResponse;
import com.example.demo.entities.concretes.Project;
import com.example.demo.entities.concretes.Sprint;
import com.example.demo.repositories.ProjectRepository;
import com.example.demo.repositories.SprintRepository;
import com.example.demo.services.abstracts.SprintService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;

    private static final int ACTIVE = 1;
    private static final int DELETED = 0;

    @Override
    public SprintResponse create(CreateSprintRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + request.getProjectId()));

        Sprint sprint = new Sprint();
        sprint.setProject(project);
        sprint.setSprintName(request.getSprintName());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setGoal(request.getGoal());
        sprint.setStatus(request.getStatus());
        sprint.setIsActive(ACTIVE);

        return toResponse(sprintRepository.save(sprint));
    }

    @Override
    public SprintResponse getById(Integer id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found: " + id));

        if (sprint.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Sprint not found: " + id);
        }

        return toResponse(sprint);
    }

    @Override
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
    public void delete(Integer id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found: " + id));

        if (sprint.getIsActive() == ACTIVE) {
            sprint.setIsActive(DELETED);
            sprintRepository.save(sprint);
        }
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
        response.setCreatedAt(sprint.getCreatedAt());
        response.setUpdatedAt(sprint.getUpdatedAt());
        response.setCreatedBy(sprint.getCreatedBy());
        response.setUpdatedBy(sprint.getUpdatedBy());
        response.setIsActive(sprint.getIsActive());
        return response;
    }
}
