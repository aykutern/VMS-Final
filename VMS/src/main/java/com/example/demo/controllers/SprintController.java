package com.example.demo.controllers;

import com.example.demo.dto.request.CreateSprintRequest;
import com.example.demo.dto.request.UpdateSprintRequest;
import com.example.demo.dto.response.SprintResponse;
import com.example.demo.services.abstracts.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @Operation(summary = "Create sprint")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SprintResponse create(
            @Parameter(description = "Optional user header for auditing", example = "aykut.eren")
            @RequestHeader(value = "X-User", required = false) String xUser,
            @Valid @RequestBody CreateSprintRequest request
    ) {
        return sprintService.create(request);
    }

    @Operation(summary = "Get sprint by id")
    @GetMapping("/{id}")
    public SprintResponse getById(@PathVariable Integer id) {
        return sprintService.getById(id);
    }

    @Operation(summary = "List sprints (optional projectId filter)")
    @GetMapping
    public List<SprintResponse> getAll(@RequestParam(required = false) Integer projectId) {
        return sprintService.getAll(projectId);
    }

    @Operation(summary = "Update sprint")
    @PutMapping("/{id}")
    public SprintResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateSprintRequest request
    ) {
        return sprintService.update(id, request);
    }

    @Operation(summary = "Delete sprint")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        sprintService.delete(id);
    }
}
