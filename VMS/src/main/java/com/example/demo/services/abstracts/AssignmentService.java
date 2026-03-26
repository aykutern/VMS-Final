package com.example.demo.services.abstracts;

import com.example.demo.dto.request.CreateAssignmentRequest;
import com.example.demo.dto.request.UpdateAssignmentStatusRequest;
import com.example.demo.dto.response.AssignmentResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentService {
    AssignmentResponse create(CreateAssignmentRequest request);

    AssignmentResponse getById(Integer id);

    List<AssignmentResponse> getAll(Integer projectId, Integer sprintId, Integer vendorId, Integer assigneeId);

    AssignmentResponse updateStatus(Integer id, UpdateAssignmentStatusRequest request);

    void delete(Integer id);
}
