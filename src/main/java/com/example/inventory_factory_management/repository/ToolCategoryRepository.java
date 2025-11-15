package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.ToolCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToolCategoryRepository extends JpaRepository<ToolCategory, Long>, JpaSpecificationExecutor<ToolCategory> {

    boolean existsByNameIgnoreCase(String name);
    Optional<ToolCategory> findByName(String name);
    Optional<ToolCategory> findByNameAndIdNot(String name, Long id);

}
