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
    List<Factory> findByStatus(AccountStatus status);
    Page<Factory> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Factory> findByCityAndStatus(String city, AccountStatus status, Pageable pageable);
//    Optional<factory> findByNameContainingIgnoreCaseNoPageable(String factoryName);


//    // FIXED: Replace the invalid method with this
    @Query("SELECT f FROM Factory f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :factoryName, '%'))")
    List<Factory> findByNameContainingIgnoreCase(@Param("factoryName") String factoryName);

    // OR if you need a single result, use this:
    @Query("SELECT f FROM Factory f WHERE LOWER(f.name) = LOWER(:factoryName)")
    Optional<Factory> findByNameIgnoreCase(@Param("factoryName") String factoryName);

    Optional<Factory> findByName(String factoryName);
}