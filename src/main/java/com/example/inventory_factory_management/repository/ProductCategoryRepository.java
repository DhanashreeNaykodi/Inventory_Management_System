package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>, JpaSpecificationExecutor<ProductCategory> {
    boolean existsByCategoryName(String categoryName);
    Page<ProductCategory> findAll(Pageable pageable);

    // NEW: Find category by exact name
    Optional<ProductCategory> findByCategoryName(String categoryName);

    // NEW: Find categories by name containing (case-insensitive)
    List<ProductCategory> findByCategoryNameContainingIgnoreCase(String categoryName);

    // NEW: Find active category by exact name
    Optional<ProductCategory> findByCategoryNameAndStatus(String categoryName, AccountStatus status);
}