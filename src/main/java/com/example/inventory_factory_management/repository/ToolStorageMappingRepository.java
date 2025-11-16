package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.ToolStorageMapping;
import com.example.inventory_factory_management.entity.Factory;
import com.example.inventory_factory_management.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ToolStorageMappingRepository extends JpaRepository<ToolStorageMapping, Long> {

    Optional<ToolStorageMapping> findByFactoryAndTool(Factory factory, Tool tool);

    List<ToolStorageMapping> findByFactoryFactoryIdAndToolId(Long factoryId, Long toolId);
    List<ToolStorageMapping> findByStorageAreaLocationCode(String locationCode);
    Optional<ToolStorageMapping> findByFactoryFactoryIdAndToolIdAndStorageAreaLocationCode(
            Long factoryId, Long toolId, String locationCode);
    List<ToolStorageMapping> findByFactoryFactoryId(Long factoryId);
    List<ToolStorageMapping> findByToolIdAndFactoryFactoryId(Long toolId, Long factoryId);

}
