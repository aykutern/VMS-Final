package com.example.demo.dto.request;

import com.example.demo.enums.AssignmentPriority;
import com.example.demo.enums.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CreateAssignmentRequest {

    @NotNull
    private Integer projectId;

    @NotNull
    private String name;

    @NotNull
    private AssignmentPriority priority;

    private LocalDate assignedAt;
}


