package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.CentralOfficeInventory;
import com.example.inventory_factory_management.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CentralOfficeInventoryRepository extends JpaRepository<CentralOfficeInventory, Long>
        ,JpaSpecificationExecutor<CentralOfficeInventory> {
    Optional<CentralOfficeInventory> findByProduct(Product product);   //shouldn't be this
    Optional<CentralOfficeInventory> findByProductId(Long productId);  //should be this
    boolean existsByProduct(Product product);
}