package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.CentralOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentralOfficeRepository extends JpaRepository<CentralOffice, Long> {
    long count();
}