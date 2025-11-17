package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.DistributorInventory;
import com.example.inventory_factory_management.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DistributorInventoryRepository extends JpaRepository<DistributorInventory, Long> {
//    Optional<DistributorInventory> findByDistributorIdAndProduct(Long distributorId, Product product);
    Optional<DistributorInventory> findByDistributorIdAndProductId(Long distributorId, Long productId);

}
