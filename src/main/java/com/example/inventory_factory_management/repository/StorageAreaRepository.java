package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.StorageArea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StorageAreaRepository extends JpaRepository<StorageArea, Long> {

    Optional<StorageArea> findByLocationCode(String locationCode);

    Page<StorageArea> findByFactoryFactoryId(Long factoryId, Pageable pageable);

    @Query("SELECT sa.locationCode FROM StorageArea sa WHERE sa.factory.factoryId = :factoryId ORDER BY sa.rowNum, sa.colNum, sa.stack, sa.bucket")
    List<String> findLocationCodesByFactoryId(@Param("factoryId") Long factoryId);
}
