package com.example.demo.services.abstracts;

import com.example.demo.dto.request.CreateSprintRequest;
import com.example.demo.dto.request.UpdateSprintRequest;
import com.example.demo.dto.response.SprintResponse;

import java.util.List;

public interface SprintService {

    SprintResponse create(CreateSprintRequest request);

    SprintResponse getById(Integer id);

    List<SprintResponse> getAll(Integer projectId);

    SprintResponse update(Integer id, UpdateSprintRequest request);

    void delete(Integer id);
}
