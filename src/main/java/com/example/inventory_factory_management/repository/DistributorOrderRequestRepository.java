package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.constants.OrderStatus;
import com.example.inventory_factory_management.entity.DistributorOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DistributorOrderRequestRepository extends JpaRepository<DistributorOrderRequest, Long>,
        JpaSpecificationExecutor<DistributorOrderRequest> {

//    List<DistributorOrderRequest> findByStatusOrderByOrderDateDesc(OrderStatus orderStatus);
//    Page<DistributorOrderRequest> findByStatus(OrderStatus status, Pageable pageable);
//    Page<DistributorOrderRequest> findByDistributorId(Long distributorId, Pageable pageable);
//    Page<DistributorOrderRequest> findByDistributorIdAndStatus(Long distributorId, OrderStatus status, Pageable pageable);
}
