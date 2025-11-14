package com.example.inventory_factory_management.specifications;

import com.example.inventory_factory_management.entity.User;
import com.example.inventory_factory_management.entity.UserFactory;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<User> withFilters(String search, String role, Long factoryId) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            // Search by username or email
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                );
                predicate = cb.and(predicate, searchPredicate);
            }

            // Filter by role
            if (role != null && !role.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("role").as(String.class), role));
            }

            // Filter by factory - handles multiple user types
            if (factoryId != null) {
                // FIXED: Changed "factoryMappings" to "userFactories" to match your entity field name
                Join<User, UserFactory> factoryJoin = root.join("userFactories", JoinType.LEFT);
                Predicate factoryPredicate = cb.equal(factoryJoin.get("factory").get("factoryId"), factoryId);

                // For managers - they can be in multiple factories
                Predicate managerFactoryPredicate = cb.and(
                        cb.equal(root.get("role"), "MANAGER"),
                        cb.equal(factoryJoin.get("factory").get("factoryId"), factoryId)
                );

                // Combine predicates
                predicate = cb.and(predicate, cb.or(factoryPredicate, managerFactoryPredicate));

                // Remove duplicates by distinct
                query.distinct(true);
            }

            return predicate;
        };
    }
}