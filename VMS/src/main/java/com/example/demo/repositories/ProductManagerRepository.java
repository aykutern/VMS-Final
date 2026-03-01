package com.example.demo.repositories;

import com.example.demo.entities.concretes.ProductManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductManagerRepository extends JpaRepository<ProductManager, Integer> {

    List<ProductManager> findByIsActive(Integer isActive);

}
