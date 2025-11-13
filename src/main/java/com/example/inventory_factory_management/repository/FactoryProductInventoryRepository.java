package com.example.inventory_factory_management.repository;


import com.example.inventory_factory_management.entity.Factory;
import com.example.inventory_factory_management.entity.FactoryProductInventory;
import com.example.inventory_factory_management.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FactoryProductInventoryRepository extends JpaRepository<FactoryProductInventory, Long> {
    Optional<FactoryProductInventory> findByFactoryAndProduct(Factory factory, Product product);
}
