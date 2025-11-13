package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.constants.RequestStatus;
import com.example.inventory_factory_management.entity.CentralOfficeProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CentralOfficeProductRequestRepository extends JpaRepository<CentralOfficeProductRequest, Long> {

    // Find requests by chief officer - can also be done by securitycontext...
    Page<CentralOfficeProductRequest> findByRequestedByUserId(Long chiefOfficerId, Pageable pageable);

    // Find requests by status and chief officer
    Page<CentralOfficeProductRequest> findByRequestedByUserIdAndStatus(Long chiefOfficerId, RequestStatus status, Pageable pageable);

    // Find requests by status
    Page<CentralOfficeProductRequest> findByStatus(RequestStatus status, Pageable pageable);

    // Find requests by factory ID or name
    @Query("SELECT r FROM CentralOfficeProductRequest r WHERE " +
            "r.factory.factoryId = :factoryId OR LOWER(r.factory.name) LIKE LOWER(CONCAT('%', :factoryName, '%'))")
    Page<CentralOfficeProductRequest> findByFactoryIdOrName(@Param("factoryId") Long factoryId,
                                                            @Param("factoryName") String factoryName,
                                                            Pageable pageable);

    @Query("SELECT r FROM CentralOfficeProductRequest r WHERE r.factory.factoryId = :factoryId")
    Page<CentralOfficeProductRequest> findByFactoryId(@Param("factoryId") Long factoryId, Pageable pageable);

    @Query("SELECT r FROM CentralOfficeProductRequest r WHERE r.factory.factoryId = :factoryId AND r.status = :status")
    Page<CentralOfficeProductRequest> findByFactoryIdAndStatus(@Param("factoryId") Long factoryId,
                                                               @Param("status") RequestStatus status,
                                                               Pageable pageable);
    Page<CentralOfficeProductRequest> findByFactoryNameAndStatus(String factoryName, RequestStatus status, Pageable pageable);

    Page<CentralOfficeProductRequest> findByFactoryName(String factoryName, Pageable pageable);

    // gave error - coz not used maybe?
//    List<CentralOfficeProductRequest> findPendingRequestsForFactory(Long factoryId);

    // Find pending requests for a factory
//    @Query("SELECT r FROM centralOfficeProductRequest r WHERE r.factory.factoryId = :factoryId AND r.status = 'PENDING'")
//    List<centralOfficeProductRequest> findPendingRequestsForFactory(@Param("factoryId") Long factoryId);
}
