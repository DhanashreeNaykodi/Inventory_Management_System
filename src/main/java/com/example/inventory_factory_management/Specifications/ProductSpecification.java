package com.example.inventory_factory_management.Specifications;

import com.example.inventory_factory_management.entity.product;
import com.example.inventory_factory_management.constants.account_status;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<product> withFilters(String search, Long categoryId, account_status status) {
        return (Root<product> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            // Search by product name
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = cb.like(cb.lower(root.get("name")), pattern);
                predicate = cb.and(predicate, searchPredicate);
            }

            // Filter by category
            if (categoryId != null) {
                Join<Object, Object> categoryJoin = root.join("category", JoinType.INNER);
                Predicate categoryPredicate = cb.equal(categoryJoin.get("id"), categoryId);
                predicate = cb.and(predicate, categoryPredicate);
            }

            // Filter by status
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            return predicate;
        };
    }
}