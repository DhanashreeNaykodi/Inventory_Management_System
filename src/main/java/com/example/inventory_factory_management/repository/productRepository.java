package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface productRepository extends JpaRepository<product, Long>, JpaSpecificationExecutor<product> {
    boolean existsByName(String name);

    // Add this method for category-based operations
    List<product> findByCategoryId(Long categoryId);

    // For soft delete queries
//    List<product> findByStatus(account_status status);


    List<product> findByStatus(com.example.inventory_factory_management.constants.account_status status);
    Page<product> findByStatus(com.example.inventory_factory_management.constants.account_status status, Pageable pageable);
}