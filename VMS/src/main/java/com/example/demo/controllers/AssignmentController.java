package com.example.demo.controllers;

import com.example.demo.dto.request.CreateAssignmentRequest;
import com.example.demo.dto.request.UpdateAssignmentStatusRequest;
import com.example.demo.dto.response.AssignmentResponse;
import com.example.demo.services.abstracts.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Operation(summary = "Create assignment")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse create(
            @Parameter(description = "Optional user header for auditing", example = "aykut.eren") @RequestHeader(value = "X-User", required = false) String xUser,
            @Valid @RequestBody CreateAssignmentRequest request) {
        return assignmentService.create(request);
    }

    @Operation(summary = "Get assignment by id")
    @GetMapping("/{id}")
    public AssignmentResponse getById(@PathVariable Integer id) {
        return assignmentService.getById(id);
    }

    @Operation(summary = "List assignments (optional projectId / sprintId / vendorId / assigneeId filter)")
    @GetMapping
    public List<AssignmentResponse> getAll(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) Integer sprintId,
            @RequestParam(required = false) Integer vendorId,
            @RequestParam(required = false) Integer assigneeId) {
        return assignmentService.getAll(projectId, sprintId, vendorId, assigneeId);
    }

    @Operation(summary = "Update assignment status")
    @PatchMapping("/{id}/status")
    public AssignmentResponse updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateAssignmentStatusRequest request) {
        return assignmentService.updateStatus(id, request);
    }

    @Operation(summary = "Delete assignment")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        assignmentService.delete(id);
    }
}
