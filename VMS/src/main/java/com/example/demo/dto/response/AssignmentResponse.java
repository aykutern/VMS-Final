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
    private String projectName;
    private Integer sprintId;
    private Integer assigneeId;
    private String assigneeName;

    private String name;
    private AssignmentPriority priority;
    private AssignmentStatus status;
    private Integer rank;

    private LocalDate assignedAt;
    private LocalDate completedAt;
    
    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private int isActive;
}
