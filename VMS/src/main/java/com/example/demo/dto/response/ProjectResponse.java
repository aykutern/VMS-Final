package com.example.demo.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ProjectResponse {

    private Integer id;
    private String projectName;
    private Integer vendorId;
    private String vendorName;
    private Integer projectManagerId;
    private String projectManagerName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private int isActive;
}
