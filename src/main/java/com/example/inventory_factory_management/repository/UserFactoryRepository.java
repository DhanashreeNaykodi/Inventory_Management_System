package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.entity.Factory;
import com.example.inventory_factory_management.entity.User;
import com.example.inventory_factory_management.entity.UserFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserFactoryRepository extends JpaRepository<UserFactory, Long> {

    // FIXED: Corrected method signature - removed "AndRole" since it doesn't exist in your repo
    List<UserFactory> findByUser(User user);

    // If you need to filter by role, add this method:
    @Query("SELECT uf FROM UserFactory uf WHERE uf.user = :user AND uf.userRole = :role")
    List<UserFactory> findByUserAndUserRole(@Param("user") User user, @Param("role") Role role);

    // Use @Query to be explicit
    @Query("SELECT uf FROM UserFactory uf WHERE uf.factory = :factory")
    List<UserFactory> findByFactory(@Param("factory") Factory factory);

    // Or find by factory ID
    @Query("SELECT uf FROM UserFactory uf WHERE uf.factory.factoryId = :factoryId")
    List<UserFactory> findByFactoryId(@Param("factoryId") Long factoryId);

    @Query("SELECT uf FROM UserFactory uf WHERE uf.factory = :factory AND uf.userRole = :role")
    Optional<UserFactory> findByFactoryAndUserRole(@Param("factory") Factory factory, @Param("role") Role role);

    @Query("SELECT uf FROM UserFactory uf WHERE uf.factory.factoryId = :factoryId AND uf.status = 'ACTIVE'")
    List<UserFactory> findActiveByFactoryId(@Param("factoryId") Long factoryId);

    boolean existsByUserAndUserRoleAndStatus(User user, Role role, AccountStatus status);

    List<UserFactory> findByUserAndStatus(User user, AccountStatus status);

    Optional<UserFactory> findByUserAndUserRoleAndStatus(User currentManager, Role role, AccountStatus accountStatus);
}