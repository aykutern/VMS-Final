package com.example.demo.repositories;

import com.example.demo.entities.concretes.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {

    List<Project> findByIsActive(Integer isActive);

    List<Project> findByVendor_IdAndIsActive(Integer vendorId, Integer isActive);

}
