package com.example.inventory_factory_management.Specifications;

import com.example.inventory_factory_management.entity.productCategory;
import com.example.inventory_factory_management.constants.account_status;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class ProductCategorySpecifications {

    public static Specification<productCategory> withFilters(String search, account_status status) {
        return (Root<productCategory> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
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