package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface toolRepository extends JpaRepository<tool, Long> {
    Page<tool> findAll(Pageable pageable);
    Page<tool> findByCategoryId(Long categoryId, Pageable pageable);
}