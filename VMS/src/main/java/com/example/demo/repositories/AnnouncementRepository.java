package com.example.demo.repositories;

import com.example.demo.entities.concretes.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    List<Announcement> findByIsActive(Integer isActive);

    List<Announcement> findByProject_IdAndIsActive(Integer projectId, Integer isActive);

}
