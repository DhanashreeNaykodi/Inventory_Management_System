package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.toolCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface toolCategoryRepository extends JpaRepository<toolCategory, Long> {
}
