package com.example.demo.repositories;

import com.example.demo.entities.concretes.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

    List<Assignment> findByIsActive(Integer isActive);

    List<Assignment> findByProject_IdAndIsActive(Integer projectId, Integer isActive);

    List<Assignment> findByProject_Id(Integer projectId);

    List<Assignment> findBySprint_IdAndIsActive(Integer sprintId, Integer isActive);

    List<Assignment> findByAssignee_IdAndIsActive(Integer assigneeId, Integer isActive);

}
