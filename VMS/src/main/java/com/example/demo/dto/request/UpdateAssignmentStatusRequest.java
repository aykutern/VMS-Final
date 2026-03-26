package com.example.demo.dto.request;

import com.example.demo.enums.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateAssignmentStatusRequest {

    @NotNull
    private AssignmentStatus status;

    private String rejectionReason;
}
