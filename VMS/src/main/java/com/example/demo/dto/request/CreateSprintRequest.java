package com.example.demo.dto.request;

import com.example.demo.enums.SprintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CreateSprintRequest {

    @NotNull
    private Integer projectId;

    @NotBlank
    private String sprintName;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String goal;

    private SprintStatus status = SprintStatus.PLANNED;

    /** Optional list of user IDs (PERSONNEL) to assign as sprint members */
    private List<Integer> memberIds;
}
