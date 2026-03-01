package com.example.demo.dto.response;

import com.example.demo.enums.SprintStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SprintResponse {

    private Integer id;
    private Integer projectId;
    private String projectName;
    private String sprintName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String goal;
    private SprintStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private int isActive;
}
