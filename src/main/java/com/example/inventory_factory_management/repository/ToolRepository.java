package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.Tool;
import com.example.inventory_factory_management.entity.ToolCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Long>, JpaSpecificationExecutor<Tool> {
//    Page<tool> findAll(Pageable pageable);
//    Page<tool> findByCategoryId(Long categoryId, Pageable pageable);

    List<Tool> findByCategory(ToolCategory category);
    boolean existsByNameIgnoreCase(String name);
    Optional<Tool> findByNameIgnoreCase(String name);
}