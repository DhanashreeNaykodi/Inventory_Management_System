package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.constants.RequestStatus;
import com.example.inventory_factory_management.entity.CentralOfficeProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface CentralOfficeProductRequestRepository extends JpaRepository<CentralOfficeProductRequest, Long> {

    Page<CentralOfficeProductRequest> findByRequestedByUserId(Long chiefOfficerId, Pageable pageable);

    // Find requests by status and chief officer
    Page<CentralOfficeProductRequest> findByRequestedByUserIdAndStatus(Long chiefOfficerId, RequestStatus status, Pageable pageable);

    // Find requests by status
    Page<CentralOfficeProductRequest> findByStatus(RequestStatus status, Pageable pageable);


    Page<CentralOfficeProductRequest> findByFactory_FactoryId(Long factoryId, Pageable pageable);


    Page<CentralOfficeProductRequest> findByFactory_FactoryIdAndStatus(Long factoryId, RequestStatus status, Pageable pageable);

}
