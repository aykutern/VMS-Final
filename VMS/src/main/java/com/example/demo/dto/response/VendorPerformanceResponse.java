package com.example.demo.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VendorPerformanceResponse {
    private Integer vendorId;
    private String vendorName;
    private int completedSprints;
    private int totalSprints;
    private int completedTasks;
    private int totalTasks;
    private int completedPoints;
    private int totalPoints;
}
