package com.example.demo.repositories;

import com.example.demo.entities.concretes.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Integer> {

    List<Vendor> findByIsActive(Integer isActive);

}
