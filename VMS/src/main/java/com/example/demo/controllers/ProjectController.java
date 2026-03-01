package com.example.demo.controllers;

import com.example.demo.dto.request.CreateProjectRequest;
import com.example.demo.dto.request.UpdateProjectRequest;
import com.example.demo.dto.response.ProjectResponse;
import com.example.demo.services.abstracts.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Create project")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(
            @Parameter(description = "Optional user header for auditing", example = "aykut.eren")
            @RequestHeader(value = "X-User", required = false) String xUser,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        return projectService.create(request);
    }

    @Operation(summary = "Get project by id")
    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable Integer id) {
        return projectService.getById(id);
    }

    @Operation(summary = "List projects (optional vendorId filter)")
    @GetMapping
    public List<ProjectResponse> getAll(@RequestParam(required = false) Integer vendorId) {
        return projectService.getAll(vendorId);
    }

    @Operation(summary = "Update project")
    @PutMapping("/{id}")
    public ProjectResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return projectService.update(id, request);
    }

    @Operation(summary = "Delete project")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        projectService.delete(id);
    }
}
