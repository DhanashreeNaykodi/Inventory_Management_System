// BayRepository.java
package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.Bay;
import com.example.inventory_factory_management.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BayRepository extends JpaRepository<Bay, Long> {
    List<Bay> findByFactory(Factory factory);
    boolean existsByFactoryAndName(Factory factory, String name);

    List<Bay> findByFactoryFactoryId(Long factoryId);
    boolean existsByNameAndFactoryFactoryId(String name, Long factoryId);

}