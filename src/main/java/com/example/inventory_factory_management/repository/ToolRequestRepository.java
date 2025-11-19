package com.example.inventory_factory_management.repository;


import com.example.inventory_factory_management.constants.Expensive;
import com.example.inventory_factory_management.constants.ToolOrProductRequestStatus;
//import com.example.inventory_factory_management.entity.ToolRequest;
import com.example.inventory_factory_management.entity.ToolRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface ToolRequestRepository extends JpaRepository<ToolRequest, Long> {


    @Query("SELECT tr FROM ToolRequest tr WHERE tr.status = :status AND tr.tool.isExpensive = :isExpensive")
    Page<ToolRequest> findByStatusAndToolIsExpensive(
            @Param("status") ToolOrProductRequestStatus status,
            @Param("isExpensive") Expensive isExpensive,
            Pageable pageable);


    Page<ToolRequest> findByWorkerUserId(Long userId, Pageable pageable);

    Optional<ToolRequest> findByIdAndWorkerUserId(Long requestId, Long userId);
}
