package com.example.demo.services.abstracts;

import com.example.demo.dto.request.CreateProjectRequest;
import com.example.demo.dto.request.UpdateProjectRequest;
import com.example.demo.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {
    ProjectResponse create(CreateProjectRequest request);
    ProjectResponse getById(Integer id);
    List<ProjectResponse> getAll(Integer vendorId);
    ProjectResponse update(Integer id, UpdateProjectRequest request);
    void delete(Integer id);
}
