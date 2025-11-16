package com.example.inventory_factory_management.repository;


import com.example.inventory_factory_management.entity.Factory;
import com.example.inventory_factory_management.entity.StorageArea;
import com.example.inventory_factory_management.entity.ToolStorageMapping;
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

    List<StorageArea> findByFactory(Factory factory);
    boolean existsByFactoryAndRowNumAndColNumAndStack(
            Factory factory,
            Integer rowNum,
            Integer colNum,
            Integer stack
    );

    boolean existsByFactoryAndRowNum(Factory factory, Integer row);
    boolean existsByFactoryAndRowNumAndColNum(Factory factory, Integer row, Integer col);

    boolean existsByRowNumAndColNumAndStack(Integer rowNum, Integer colNum, Integer stack);
    Optional<StorageArea> findByLocationCode(String locationCode);
    List<StorageArea> findByFactoryFactoryId(Long factoryId);
    List<StorageArea> findByFactoryFactoryIdOrderByRowNumAscColNumAscStackAscBucketAsc(Long factoryId);

    Page<StorageArea> findByFactoryFactoryId(Long factoryId, Pageable pageable);

    @Query("SELECT sa.locationCode FROM StorageArea sa WHERE sa.factory.factoryId = :factoryId ORDER BY sa.rowNum, sa.colNum, sa.stack, sa.bucket")
    List<String> findLocationCodesByFactoryId(@Param("factoryId") Long factoryId);
}
