package com.example.demo.repositories;

import com.example.demo.entities.concretes.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Integer> {

    List<Sprint> findByIsActive(Integer isActive);

    List<Sprint> findByProject_IdAndIsActive(Integer projectId, Integer isActive);

}
