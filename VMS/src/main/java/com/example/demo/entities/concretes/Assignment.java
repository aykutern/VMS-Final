package com.example.demo.entities.concretes;

import com.example.demo.entities.abstracts.BaseEntity;
import com.example.demo.enums.AssignmentPriority;
import com.example.demo.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assignment")
public class Assignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Integer id;

    @Column(name = "assignment_name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private AssignmentPriority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AssignmentStatus status = AssignmentStatus.TODO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private Users assignee;

    @Column(name = "assigned_at")
    private LocalDate assignedAt;

    @Column(name = "completed_at")
    private LocalDate completedAt;

    @Column(name = "task_rank")
    private Integer rank = 1;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;
}
