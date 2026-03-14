package com.example.demo.repositories;

import com.example.demo.entities.concretes.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.enums.SprintStatus;
import java.util.List;
import java.util.Optional;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Integer> {

    List<Sprint> findByIsActive(Integer isActive);

    List<Sprint> findByProject_IdAndIsActive(Integer projectId, Integer isActive);

    Optional<Sprint> findByProject_IdAndStatusAndIsActive(Integer projectId, SprintStatus status, Integer isActive);

    /** Used to enforce: one developer can only be in one ACTIVE sprint at a time */
    boolean existsByMembers_IdAndStatusAndIsActiveAndIdNot(Integer userId, SprintStatus status, Integer isActive, Integer excludeSprintId);
}
