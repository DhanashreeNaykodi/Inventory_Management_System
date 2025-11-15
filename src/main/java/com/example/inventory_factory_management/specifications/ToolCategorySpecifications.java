package com.example.inventory_factory_management.specifications;

import com.example.inventory_factory_management.entity.ToolCategory;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class ToolCategorySpecifications {

    public static Specification<ToolCategory> withFilters(String search) {
        return (Root<ToolCategory> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            // Search by category name or description
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                );
                predicate = cb.and(predicate, searchPredicate);
            }

            return predicate;
        };
    }

    public static Specification<ToolCategory> withName(String name) {
        return (Root<ToolCategory> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("name"), name);
        };
    }
}