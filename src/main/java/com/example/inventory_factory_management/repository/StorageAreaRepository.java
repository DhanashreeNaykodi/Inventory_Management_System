package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.StorageArea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StorageAreaRepository extends JpaRepository<StorageArea, Long> {

    Optional<StorageArea> findByLocationCode(String locationCode);

    Page<StorageArea> findByFactoryFactoryId(Long factoryId, Pageable pageable);

}
