// BayRepository.java
package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.bay;
import com.example.inventory_factory_management.entity.factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BayRepository extends JpaRepository<bay, Long> {
    List<bay> findByFactory(factory factory);
    boolean existsByFactoryAndName(factory factory, String name);

    List<bay> findByFactoryFactoryId(Long factoryId);
}