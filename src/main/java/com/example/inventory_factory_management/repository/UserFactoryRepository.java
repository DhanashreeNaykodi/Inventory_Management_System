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


    List<UserFactory> findByFactory_FactoryId(Long factoryId);



    Optional<UserFactory> findByFactoryAndUserRole(Factory factory, Role role);

    boolean existsByUserAndUserRoleAndStatus(User user, Role role, AccountStatus status);

    List<UserFactory> findByUserAndStatus(User user, AccountStatus status);

    Optional<UserFactory> findByUserAndUserRoleAndStatus(User currentManager, Role role, AccountStatus accountStatus);
}