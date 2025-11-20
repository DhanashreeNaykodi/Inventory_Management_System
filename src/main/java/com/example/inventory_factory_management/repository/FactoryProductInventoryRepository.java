package com.example.inventory_factory_management.repository;


import com.example.inventory_factory_management.entity.Factory;
import com.example.inventory_factory_management.entity.FactoryProductInventory;
import com.example.inventory_factory_management.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactoryProductInventoryRepository extends JpaRepository<FactoryProductInventory, Long> {
    Optional<FactoryProductInventory> findByFactoryAndProduct(Factory factory, Product product);

    // Get factory-wise product counts with details
    @Query("SELECT f.factoryId, f.name as factoryName, p.id, p.name as productName, COALESCE(SUM(fpi.qty), 0) as productCount " +
            "FROM Factory f " +
            "CROSS JOIN Product p " +
            "LEFT JOIN FactoryProductInventory fpi ON f.factoryId = fpi.factory.factoryId AND p.id = fpi.product.id " +
            "GROUP BY f.factoryId, f.name, p.id, p.name")
    Page<Object[]> getFactoryProductCountsDetailed(Pageable pageable);
}
