package com.example.demo.entities.concretes;

import com.example.demo.entities.abstracts.BaseEntity;
import com.example.demo.enums.SprintStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Sprint")
public class Sprint extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sprint_id")
    private Integer id;

    @Column(name = "sprint_name")
    private String sprintName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "goal", length = 1000)
    private String goal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SprintStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    /** Developers assigned to this sprint */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "sprint_members",
        joinColumns = @JoinColumn(name = "sprint_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<Users> members = new HashSet<>();
}
