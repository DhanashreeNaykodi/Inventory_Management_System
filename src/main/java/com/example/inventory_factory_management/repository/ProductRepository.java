package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.entity.Product;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);


    List<Product> findByStatus(AccountStatus status);
    Page<Product> findByStatus(AccountStatus status, Pageable pageable);

    boolean existsByNameIgnoreCaseAndIdNot(@NotBlank(message = "Product name cannot be blank") String name, Long id);
}