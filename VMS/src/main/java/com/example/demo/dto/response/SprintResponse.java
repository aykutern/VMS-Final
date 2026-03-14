package com.example.demo.dto.response;

import com.example.demo.enums.SprintStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    /** Assigned developers */
    private List<MemberDto> members;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private int isActive;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MemberDto {
        private Integer id;
        private String fullName;

        public MemberDto(Integer id, String fullName) {
            this.id = id;
            this.fullName = fullName;
        }
    }
}
