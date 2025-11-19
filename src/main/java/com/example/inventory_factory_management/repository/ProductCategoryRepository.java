package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>, JpaSpecificationExecutor<ProductCategory> {
    boolean existsByCategoryName(String categoryName);
    boolean existsByCategoryNameIgnoreCase(String categoryName);


    Page<ProductCategory> findAll(Pageable pageable);


    // Find categories by name containing (case-insensitive)
    Optional<ProductCategory> findByCategoryNameContainingIgnoreCase(String categoryName);

    // NEW: Find active category by exact name
    Optional<ProductCategory> findByCategoryNameAndStatus(String categoryName, AccountStatus status);


    // Get category-wise product counts with pagination
    @Query("SELECT pc.id, pc.categoryName, COUNT(p) as productCount " +
            "FROM ProductCategory pc LEFT JOIN pc.products p " +
            "GROUP BY pc.id, pc.categoryName")
    Page<Object[]> getCategoryWiseProductCounts(Pageable pageable);
}