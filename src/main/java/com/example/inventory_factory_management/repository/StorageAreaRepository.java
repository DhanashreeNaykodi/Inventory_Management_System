package com.example.inventory_factory_management.repository;


import com.example.inventory_factory_management.entity.factory;
import com.example.inventory_factory_management.entity.storageArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageAreaRepository extends JpaRepository<storageArea, Long> {

    List<storageArea> findByFactory(factory factory);
    boolean existsByFactoryAndRowNumAndColNumAndStack(
            factory factory,
            Integer rowNum,
            Integer colNum,
            Integer stack
    );
}
