package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProjectRequest {

    @NotBlank
    private String projectName;

    private Integer vendorId;

    private Integer projectManagerId;
}
