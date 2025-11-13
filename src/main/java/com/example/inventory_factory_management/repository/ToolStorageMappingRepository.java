package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.ToolStorageMapping;
import com.example.inventory_factory_management.entity.Factory;
import com.example.inventory_factory_management.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ToolStorageMappingRepository extends JpaRepository<ToolStorageMapping, Long> {

    Optional<ToolStorageMapping> findByFactoryAndTool(Factory factory, Tool tool);

}
