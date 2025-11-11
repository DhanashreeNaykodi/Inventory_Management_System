package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.centralOffice;
import com.example.inventory_factory_management.entity.user;
import com.example.inventory_factory_management.entity.users_centralOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCentralOfficeRepository extends JpaRepository<users_centralOffice, Long> {
    List<users_centralOffice> findByOffice(centralOffice office);
    Optional<users_centralOffice> findByUser(user user);
    boolean existsByUser(user user);

    boolean existsByUserAndOffice(user officer, centralOffice office);

    Optional<users_centralOffice> findByUserAndOffice(user user, centralOffice office);

}