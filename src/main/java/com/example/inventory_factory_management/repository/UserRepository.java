package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.entity.User;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    Page<User> findByRole(Role role, Pageable pageable);



    List<User> findByUserFactories_Factory_FactoryId(Long factoryId);


    boolean existsByEmail(String centralOfficerEmail);

    // Find name by exact name and role
    Optional<User> findByUsernameAndRole(String username, Role role);




    List<User> findByUsernameContainingIgnoreCaseAndRole(String username, Role role);




    Optional<User> findByPhone(@Pattern(regexp = "^(\\+91|0)?[6-9]\\d{9}$",
            message = "Enter a valid Indian mobile number") Long phone);
}