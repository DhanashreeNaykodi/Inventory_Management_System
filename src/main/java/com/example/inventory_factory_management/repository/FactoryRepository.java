package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.entity.Factory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactoryRepository extends JpaRepository<Factory, Long> {
    boolean existsByName(String name);
    boolean existsByNameContainingIgnoreCase(String name);
    List<Factory> findByStatus(AccountStatus status);
    Page<Factory> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Factory> findByCityAndStatus(String city, AccountStatus status, Pageable pageable);


    Optional<Factory> findByName(String factoryName);

    List<Factory> findByNameContainingIgnoreCase(String factoryName);

}