package com.example.demo.dto.request;

import com.example.demo.enums.SprintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateSprintRequest {

    @NotNull
    private Integer projectId;

    @NotBlank
    private String sprintName;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String goal;

    @NotNull
    private SprintStatus status;
}
