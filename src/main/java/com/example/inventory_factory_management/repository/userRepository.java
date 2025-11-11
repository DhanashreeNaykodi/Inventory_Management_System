package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.entity.user;
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
public interface userRepository extends JpaRepository<user, Long>, JpaSpecificationExecutor<user> {
    Optional<user> findByEmail(String email);
    List<user> findByRole(Role role);
    Page<user> findByRole(Role role, Pageable pageable);


    //coz user entity does not have direct field of factory id...
    // Method to find users by factory using a JOIN query
    @Query("SELECT u FROM user u JOIN u.userFactories uf WHERE uf.factory.factoryId = :factoryId")
    List<user> findUsersByFactoryId(@Param("factoryId") Long factoryId);

    // Method to find users by factory and role
    @Query("SELECT u FROM user u JOIN u.userFactories uf WHERE uf.factory.factoryId = :factoryId AND u.role = :role")
    List<user> findUsersByFactoryIdAndRole(@Param("factoryId") Long factoryId, @Param("role") Role role);

    boolean existsByEmail(String centralOfficerEmail);

    // Find central officer by exact name and role
    Optional<user> findByUsernameAndRole(String username, Role role);

    List<user> findByUsernameContainingIgnoreCaseAndRole(String username, Role role);
}