package com.example.inventory_factory_management.specifications;

import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.constants.Expensive;
import com.example.inventory_factory_management.constants.ToolType;
import com.example.inventory_factory_management.entity.Tool;
import com.example.inventory_factory_management.entity.ToolCategory;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class ToolSpecifications {

    public static Specification<Tool> withFilters(Long categoryId, AccountStatus status, ToolType type, Expensive isExpensive, String search) {

        return (Root<Tool> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            // Filter by category
            if (categoryId != null) {
                Join<Tool, ToolCategory> categoryJoin = root.join("category", JoinType.INNER);
                predicate = cb.and(predicate, cb.equal(categoryJoin.get("id"), categoryId));
            }

            // Filter by status
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            // Filter by type
            if (type != null) {
                predicate = cb.and(predicate, cb.equal(root.get("type"), type));
            }

            // Filter by isExpensive
            if (isExpensive != null) {
                predicate = cb.and(predicate, cb.equal(root.get("isExpensive"), isExpensive));
            }

            // Search by tool name
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = cb.like(cb.lower(root.get("name")), pattern);
                predicate = cb.and(predicate, searchPredicate);
            }

            return predicate;
        };
    }
}