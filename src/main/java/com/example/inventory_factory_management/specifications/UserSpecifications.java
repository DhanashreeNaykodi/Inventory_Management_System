package com.example.inventory_factory_management.specifications;

import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.entity.User;
import com.example.inventory_factory_management.entity.UserFactory;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

//    public static Specification<User> withFilters(String search, String role, Long factoryId) {
//        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
//            Predicate predicate = cb.conjunction();
//
//            if (search != null && !search.isBlank()) {
//                String pattern = "%" + search.toLowerCase() + "%";
//                Predicate searchPredicate = cb.or(
//                        cb.like(cb.lower(root.get("username")), pattern),
//                        cb.like(cb.lower(root.get("email")), pattern)
//                );
//                predicate = cb.and(predicate, searchPredicate);
//            }
//
//            if (role != null && !role.isBlank()) {
//                predicate = cb.and(predicate, cb.equal(root.get("role").as(String.class), role));
//            }
//
//            if (factoryId != null) {
//                Join<User, UserFactory> factoryJoin = root.join("userFactories", JoinType.LEFT);
//                Predicate factoryPredicate = cb.equal(factoryJoin.get("factory").get("factoryId"), factoryId);
//
//                Predicate managerFactoryPredicate = cb.and(
//                        cb.equal(root.get("role"), "MANAGER"),
//                        cb.equal(factoryJoin.get("factory").get("factoryId"), factoryId)
//                );
//
//                predicate = cb.and(predicate, cb.or(factoryPredicate, managerFactoryPredicate));
//
//                // Remove duplicates by distinct
//                query.distinct(true);
//            }
//
//            return predicate;
//        };
//    }

    public static Specification<User> withFilters(String search, String role, Long factoryId) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            // Search filter
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicate = cb.and(predicate,
                        cb.or(
                                cb.like(cb.lower(root.get("username")), pattern),
                                cb.like(cb.lower(root.get("email")), pattern)
                        )
                );
            }

            // Role filter
            if (role != null && !role.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("role").as(String.class), role));
            }

            // Factory filter ONLY IF factoryId is provided
            if (factoryId != null) {
                Join<User, UserFactory> uf = root.join("userFactories", JoinType.LEFT);

                predicate = cb.and(predicate,
                        cb.equal(uf.get("factory").get("factoryId"), factoryId)
                );

                query.distinct(true);
            }

            return predicate;
        };
    }

    public static Specification<User> withFilters(String search, String role, Long factoryId, AccountStatus status) {
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

            if (factoryId != null) {
                Join<User, UserFactory> factoryJoin = root.join("userFactories", JoinType.LEFT);
                Predicate factoryPredicate = cb.equal(factoryJoin.get("factory").get("factoryId"), factoryId);

                Predicate managerFactoryPredicate = cb.and(
                        cb.equal(root.get("role"), "MANAGER"),
                        cb.equal(factoryJoin.get("factory").get("factoryId"), factoryId)
                );

                predicate = cb.and(predicate, cb.or(factoryPredicate, managerFactoryPredicate));
                query.distinct(true);
            }

            // NEW: Filter by status
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            return predicate;
        };
    }

//    public static Specification<User> availableManagers() {
//        return (root, query, cb) -> {
//            Join<User, UserFactory> factoryJoin = root.join("userFactories", JoinType.LEFT);
//            return cb.and(
//                    cb.equal(root.get("role"), Role.MANAGER),
//                    cb.equal(root.get("status"), AccountStatus.ACTIVE),
//                    cb.or(
//                            cb.isNull(factoryJoin.get("factory").get("factoryId")),
//                            cb.notEqual(factoryJoin.get("factory").get("status"), AccountStatus.ACTIVE)
//                    )
//            );
//        };
//    }
public static Specification<User> availableManagers() {
    return (root, query, cb) -> {

        query.distinct(true);

        // LEFT JOIN userFactories
        Join<User, UserFactory> userFactoryJoin = root.join("userFactories", JoinType.LEFT);

        return cb.and(
                cb.equal(root.get("role"), Role.MANAGER),
                cb.equal(root.get("status"), AccountStatus.ACTIVE),

                // Manager is available when they have NO FACTORY assigned
                cb.isNull(userFactoryJoin.get("factory"))
        );
    };
}


}