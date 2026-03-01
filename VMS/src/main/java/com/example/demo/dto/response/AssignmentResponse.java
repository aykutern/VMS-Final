package com.example.demo.dto.response;

import com.example.demo.enums.AssignmentPriority;
import com.example.demo.enums.AssignmentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AssignmentResponse {

    private Integer id;
    private Integer projectId;

    private String name;
    private AssignmentPriority priority;
    private AssignmentStatus status;

    private LocalDate assignedAt;
    private LocalDate completedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private int isActive;
}
