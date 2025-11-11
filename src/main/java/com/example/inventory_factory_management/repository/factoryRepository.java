package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.factory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface factoryRepository extends JpaRepository<factory, Long> {
    boolean existsByName(String name);
    List<factory> findByStatus(com.example.inventory_factory_management.constants.account_status status);
    Page<factory> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<factory> findByCityAndStatus(String city, com.example.inventory_factory_management.constants.account_status status, Pageable pageable);
}