package com.example.inventory_factory_management.repository;

import com.example.inventory_factory_management.entity.OrderItem;
import org.hibernate.query.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
