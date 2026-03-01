package com.example.demo.controllers;

import com.example.demo.dto.request.CreateAnnouncementRequest;
import com.example.demo.dto.request.UpdateAnnouncementRequest;
import com.example.demo.dto.response.AnnouncementResponse;
import com.example.demo.services.abstracts.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "Create announcement")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnnouncementResponse create(
            @Parameter(description = "Optional user header for auditing", example = "aykut.eren")
            @RequestHeader(value = "X-User", required = false) String xUser,
            @Valid @RequestBody CreateAnnouncementRequest request
    ) {
        return announcementService.create(request);
    }

    @Operation(summary = "Get announcement by id")
    @GetMapping("/{id}")
    public AnnouncementResponse getById(@PathVariable Integer id) {
        return announcementService.getById(id);
    }

    @Operation(summary = "List announcements (optional projectId filter)")
    @GetMapping
    public List<AnnouncementResponse> getAll(@RequestParam(required = false) Integer projectId) {
        return announcementService.getAll(projectId);
    }

    @Operation(summary = "Update announcement")
    @PutMapping("/{id}")
    public AnnouncementResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateAnnouncementRequest request
    ) {
        return announcementService.update(id, request);
    }

    @Operation(summary = "Delete announcement")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        announcementService.delete(id);
    }
}
