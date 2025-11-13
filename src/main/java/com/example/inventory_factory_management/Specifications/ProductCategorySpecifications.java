package com.example.inventory_factory_management.Specifications;

import com.example.inventory_factory_management.entity.ProductCategory;
import com.example.inventory_factory_management.constants.AccountStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class ProductCategorySpecifications {

    public static Specification<ProductCategory> withFilters(String search, AccountStatus status) {
        return (Root<ProductCategory> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            // Search by category name
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = cb.like(cb.lower(root.get("categoryName")), pattern);
                predicate = cb.and(predicate, searchPredicate);
            }

            // Filter by status
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            return predicate;
        };
    }
}