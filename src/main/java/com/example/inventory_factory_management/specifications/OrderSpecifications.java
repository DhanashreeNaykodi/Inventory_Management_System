package com.example.inventory_factory_management.specifications;

import com.example.inventory_factory_management.constants.OrderStatus;
import com.example.inventory_factory_management.entity.DistributorOrderRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecifications {

    public static Specification<DistributorOrderRequest> withFilters(
            Long distributorId,
            OrderStatus status,
            String distributorName) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (distributorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("distributorId"), distributorId));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (distributorName != null && !distributorName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("distributorName")),
                        "%" + distributorName.toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<DistributorOrderRequest> withStatus(OrderStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<DistributorOrderRequest> withDistributorId(Long distributorId) {
        return (root, query, criteriaBuilder) ->
                distributorId == null ? null : criteriaBuilder.equal(root.get("distributorId"), distributorId);
    }
}
