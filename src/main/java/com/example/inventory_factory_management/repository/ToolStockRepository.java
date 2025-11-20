package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.ToolStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToolStockRepository extends JpaRepository<ToolStock, Long> {
//    Optional<ToolStock> findByToolIdAndFactoryFactoryId(Long toolId, Long factoryId);

    Page<ToolStock> findByFactoryFactoryId(Long factoryId, Pageable pageable);


    Optional<ToolStock> findByTool_IdAndFactory_FactoryId(Long toolId, Long factoryId);

}
