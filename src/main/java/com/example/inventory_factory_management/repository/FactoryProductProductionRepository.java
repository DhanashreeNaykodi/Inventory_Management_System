package com.example.inventory_factory_management.repository;


import com.example.inventory_factory_management.entity.Factory;
import com.example.inventory_factory_management.entity.FactoryProductProduction;
import com.example.inventory_factory_management.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface FactoryProductProductionRepository extends JpaRepository<FactoryProductProduction, Long> {
    Optional<FactoryProductProduction> findByFactoryAndProduct(Factory factory, Product product);
}
