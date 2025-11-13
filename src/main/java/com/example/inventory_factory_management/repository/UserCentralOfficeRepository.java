package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.CentralOffice;
import com.example.inventory_factory_management.entity.User;
import com.example.inventory_factory_management.entity.UserCentralOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCentralOfficeRepository extends JpaRepository<UserCentralOffice, Long> {
    List<UserCentralOffice> findByOffice(CentralOffice office);
    Optional<UserCentralOffice> findByUser(User user);
    boolean existsByUser(User user);

    boolean existsByUserAndOffice(User officer, CentralOffice office);

    Optional<UserCentralOffice> findByUserAndOffice(User user, CentralOffice office);

}