package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.entity.factory;
import com.example.inventory_factory_management.entity.user;
import com.example.inventory_factory_management.entity.userFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface userFactoryRepository extends JpaRepository<userFactory, Long> {

    // FIXED: Corrected method signature - removed "AndRole" since it doesn't exist in your repo
    List<userFactory> findByUser(user user);

    // If you need to filter by role, add this method:
    @Query("SELECT uf FROM userFactory uf WHERE uf.user = :user AND uf.userRole = :role")
    List<userFactory> findByUserAndUserRole(@Param("user") user user, @Param("role") Role role);

    // Use @Query to be explicit
    @Query("SELECT uf FROM userFactory uf WHERE uf.factory = :factory")
    List<userFactory> findByFactory(@Param("factory") factory factory);

    // Or find by factory ID
    @Query("SELECT uf FROM userFactory uf WHERE uf.factory.factoryId = :factoryId")
    List<userFactory> findByFactoryId(@Param("factoryId") Long factoryId);

    @Query("SELECT uf FROM userFactory uf WHERE uf.factory = :factory AND uf.userRole = :role")
    Optional<userFactory> findByFactoryAndUserRole(@Param("factory") factory factory, @Param("role") Role role);

    @Query("SELECT uf FROM userFactory uf WHERE uf.factory.factoryId = :factoryId AND uf.status = 'ACTIVE'")
    List<userFactory> findActiveByFactoryId(@Param("factoryId") Long factoryId);

}