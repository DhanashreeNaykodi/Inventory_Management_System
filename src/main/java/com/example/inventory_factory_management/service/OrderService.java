package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.constants.OrderStatus;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.entity.*;
import com.example.inventory_factory_management.repository.*;
import com.example.inventory_factory_management.specifications.OrderSpecifications;
import com.example.inventory_factory_management.utils.PaginationUtil;
import com.example.inventory_factory_management.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class OrderService {

    private final DistributorOrderRequestRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CentralOfficeInventoryRepository centralInventoryRepository;
    private final DistributorInventoryRepository distributorInventoryRepository;
    private final SecurityUtil securityUtil;

    @Autowired
    public OrderService(DistributorOrderRequestRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ProductRepository productRepository,
                        CentralOfficeInventoryRepository centralInventoryRepository,
                        DistributorInventoryRepository distributorInventoryRepository,
                        SecurityUtil securityUtil) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.centralInventoryRepository = centralInventoryRepository;
        this.distributorInventoryRepository = distributorInventoryRepository;
        this.securityUtil = securityUtil;
    }


    public DistributorOrderRequest placeOrder(PlaceOrderDTO orderRequest) {
        User currentUser = securityUtil.getCurrentUser();
        Long distributorId = currentUser.getUserId();

        if (currentUser.getRole() != Role.DISTRIBUTOR) {
            throw new RuntimeException("Only distributors can place orders");
        }

        if (orderRequest.getOrderItems() == null || orderRequest.getOrderItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }

        // Create order
        DistributorOrderRequest order = new DistributorOrderRequest();
        order.setDistributorId(distributorId);
        order.setDistributorName(currentUser.getUsername());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalPrice(BigDecimal.ZERO);

        // Save order first to get ID
        order = orderRepository.save(order);

        List<OrderItem> orderItemsList = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemDTO : orderRequest.getOrderItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDTO.getProductId()));

            if (product.getStatus() != AccountStatus.ACTIVE) {
                throw new RuntimeException("Product is not available: " + product.getName());
            }

            if (itemDTO.getQuantity() == null || itemDTO.getQuantity() <= 0) {
                throw new RuntimeException("Invalid quantity for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPricePerUnit(product.getPrice());

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalPrice = totalPrice.add(subtotal);

            orderItemsList.add(orderItem);
        }

        // Save order items
        orderItemRepository.saveAll(orderItemsList);

        // Update order with total and items
        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItemsList);
        return orderRepository.save(order);
    }

    public void processOrderAction(Long orderId, OrderActionDTO actionRequest) {
        DistributorOrderRequest order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

         switch (actionRequest.getStatus()) {
            case APPROVED:
                if (order.getStatus() != OrderStatus.PENDING) {
                    throw new RuntimeException("Only pending orders can be approved");
                }
                if (!isStockAvailable(order.getOrderItems())) {
                    throw new RuntimeException("Insufficient stock in central office inventory");
                }
                order.setStatus(OrderStatus.APPROVED);
                break;

            case REJECTED:
                if (order.getStatus() != OrderStatus.PENDING) {
                    throw new RuntimeException("Only pending orders can be rejected");
                }
                if (actionRequest.getRejectReason() == null || actionRequest.getRejectReason().trim().isEmpty()) {
                    throw new RuntimeException("Reject reason is required");
                }
                order.setStatus(OrderStatus.REJECTED);
                order.setRejectReason(actionRequest.getRejectReason().trim());
                break;

            case DELIVERED:
                if (order.getStatus() != OrderStatus.PAID) {
                    throw new RuntimeException("Only paid orders can be marked as delivered");
                }
                order.setStatus(OrderStatus.DELIVERED);
                break;

            default:
                throw new RuntimeException("Invalid action for order processing: " + actionRequest.getStatus());
        }

        orderRepository.save(order);
    }

    public void processPayment(Long orderId) {
        User currentUser = securityUtil.getCurrentUser();
        Long distributorId = currentUser.getUserId();

        DistributorOrderRequest order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!order.getDistributorId().equals(distributorId)) {
            throw new RuntimeException("You can only pay for your own orders");
        }

        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new RuntimeException("Order must be approved before payment");
        }

        updateInventoriesAfterPayment(order);
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
    }

    // Using Criteria Query for advanced filtering (simplified)
    public Page<DistributorOrderRequest> getOrdersWithFilters(OrderFilterDTO filter, BaseRequestDTO request) {
        Pageable pageable = PaginationUtil.toPageable(request); //

        Specification<DistributorOrderRequest> spec = OrderSpecifications.withFilters(
                filter.getDistributorId(),
                filter.getStatus(),
                filter.getDistributorName()
        );

        return orderRepository.findAll(spec, pageable);
    }



    private boolean isStockAvailable(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            CentralOfficeInventory inventory = centralInventoryRepository.findByProductId(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found in central inventory: " + item.getProduct().getId()));

            if (inventory.getQuantity() < item.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    private void updateInventoriesAfterPayment(DistributorOrderRequest order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();

            // Deduct from central office
            CentralOfficeInventory centralInventory = centralInventoryRepository.findByProduct(product)
                    .orElseThrow(() -> new RuntimeException("Product not found in central inventory: " + product.getId()));
            centralInventory.deductQuantity(item.getQuantity().longValue());
            centralInventoryRepository.save(centralInventory);

            // Add to distributor inventory
            DistributorInventory distributorInventory = distributorInventoryRepository
                    .findByDistributorIdAndProduct(order.getDistributorId(), product)
                    .orElse(new DistributorInventory());

            if (distributorInventory.getId() == null) {
                distributorInventory.setDistributorId(order.getDistributorId());
                distributorInventory.setProduct(product);
                distributorInventory.setStockQty(0);
            }

            distributorInventory.setStockQty(distributorInventory.getStockQty() + item.getQuantity());
            distributorInventoryRepository.save(distributorInventory);
        }
    }
}
