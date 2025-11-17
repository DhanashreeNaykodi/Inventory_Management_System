//package com.example.inventory_factory_management.repository;
//
//
//import com.example.inventory_factory_management.constants.Expensive;
//import com.example.inventory_factory_management.constants.ToolOrProductRequestStatus;
////import com.example.inventory_factory_management.entity.ToolRequest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface ToolRequestRepository extends JpaRepository<ToolRequest, Long> {
//
//    Page<ToolRequest> findByWorkerId(Long workerId, Pageable pageable);
//
//    @Query("SELECT wtr FROM WorkerToolRequest wtr WHERE wtr.status = :status AND wtr.tool.isExpensive = :isExpensive")
//    Page<ToolRequest> findByStatusAndToolIsExpensive(
//            @Param("status") ToolOrProductRequestStatus status,
//            @Param("isExpensive") Expensive isExpensive,
//            Pageable pageable);
//
//    List<ToolRequest> findByStatusAndAutoReturnDateBefore(
//            ToolOrProductRequestStatus status, LocalDateTime date);
//
//    Optional<ToolRequest> findByIdAndWorkerId(Long id, Long workerId);
//}
