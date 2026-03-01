package com.example.demo.repositories;

import com.example.demo.entities.concretes.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {

    List<Users> findByIsActive(Integer isActive);

    List<Users> findByVendor_IdAndIsActive(Integer vendorId, Integer isActive);

    Optional<Users> findByUsername(String username);

    Optional<Users> findByEmail(String email);

}
