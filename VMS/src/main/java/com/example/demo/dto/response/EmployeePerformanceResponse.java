package com.example.demo.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeePerformanceResponse {
    private Integer userId;
    private String fullName;
    private int completedTasks;
    private int totalTasks;
    private int completedPoints;
    private int totalPoints;
    private Double avgCompletionDays;
}
