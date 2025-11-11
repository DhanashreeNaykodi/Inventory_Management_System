package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.productCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface productCategoryRepository extends JpaRepository<productCategory, Long>, JpaSpecificationExecutor<productCategory> {
    boolean existsByCategoryName(String categoryName);
    Page<productCategory> findAll(Pageable pageable);

    // NEW: Find category by exact name
    Optional<productCategory> findByCategoryName(String categoryName);

    // NEW: Find categories by name containing (case-insensitive)
    List<productCategory> findByCategoryNameContainingIgnoreCase(String categoryName);

    // NEW: Find active category by exact name
    Optional<productCategory> findByCategoryNameAndStatus(String categoryName, com.example.inventory_factory_management.constants.account_status status);
}