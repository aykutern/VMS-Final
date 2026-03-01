package com.example.demo.services.concretes;

import com.example.demo.dto.request.CreateProjectRequest;
import com.example.demo.dto.request.UpdateProjectRequest;
import com.example.demo.dto.response.ProjectResponse;
import com.example.demo.entities.concretes.Project;
import com.example.demo.entities.concretes.Vendor;
import com.example.demo.entities.concretes.Users;
import com.example.demo.repositories.ProjectRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.VendorRepository;
import com.example.demo.services.abstracts.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    private static final int ACTIVE = 1;
    private static final int DELETED = 0;

    @Override
    public ProjectResponse create(CreateProjectRequest request) {
        Project project = new Project();
        project.setProjectName(request.getProjectName());

        if (request.getVendorId() != null) {
            Vendor vendor = vendorRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new EntityNotFoundException("Vendor not found: " + request.getVendorId()));
            project.setVendor(vendor);
        }

        if (request.getProjectManagerId() != null) {
            Users pm = userRepository.findById(request.getProjectManagerId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "User not found: " + request.getProjectManagerId()));
            project.setProjectManager(pm);
        }

        project.setIsActive(ACTIVE);

        return toResponse(projectRepository.save(project));
    }

    @Override
    public ProjectResponse getById(Integer id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));

        if (project.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Project not found: " + id);
        }

        return toResponse(project);
    }

    @Override
    public List<ProjectResponse> getAll(Integer vendorId) {
        List<Project> list = (vendorId == null)
                ? projectRepository.findByIsActive(ACTIVE)
                : projectRepository.findByVendor_IdAndIsActive(vendorId, ACTIVE);

        return list.stream().map(this::toResponse).toList();
    }

    @Override
    public ProjectResponse update(Integer id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));

        if (project.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Project not found: " + id);
        }

        project.setProjectName(request.getProjectName());

        if (request.getVendorId() != null) {
            Vendor vendor = vendorRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new EntityNotFoundException("Vendor not found: " + request.getVendorId()));
            project.setVendor(vendor);
        } else {
            project.setVendor(null);
        }

        if (request.getProjectManagerId() != null) {
            Users pm = userRepository.findById(request.getProjectManagerId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "User not found: " + request.getProjectManagerId()));
            project.setProjectManager(pm);
        } else {
            project.setProjectManager(null);
        }

        return toResponse(projectRepository.save(project));
    }

    @Override
    public void delete(Integer id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));

        if (project.getIsActive() == ACTIVE) {
            project.setIsActive(DELETED);
            projectRepository.save(project);
        }
    }

    private ProjectResponse toResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setProjectName(project.getProjectName());

        if (project.getVendor() != null) {
            response.setVendorId(project.getVendor().getId());
            response.setVendorName(project.getVendor().getVendorName());
        }

        if (project.getProjectManager() != null) {
            response.setProjectManagerId(project.getProjectManager().getId());
            response.setProjectManagerName(
                    project.getProjectManager().getPersonnelName() + " " +
                            project.getProjectManager().getPersonnelSurname());
        }

        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        response.setCreatedBy(project.getCreatedBy());
        response.setUpdatedBy(project.getUpdatedBy());
        response.setIsActive(project.getIsActive());
        return response;
    }
}
