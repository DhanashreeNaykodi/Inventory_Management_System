package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.CentralOfficeInventory;
import com.example.inventory_factory_management.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CentralOfficeInventoryRepository extends JpaRepository<CentralOfficeInventory, Long> {
    Optional<CentralOfficeInventory> findByProduct(Product product);
    Optional<CentralOfficeInventory> findByProductId(Long productId);
    boolean existsByProduct(Product product);
}