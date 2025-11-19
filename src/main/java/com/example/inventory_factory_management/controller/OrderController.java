package com.example.inventory_factory_management.controller;

import com.example.inventory_factory_management.constants.OrderStatus;
import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.entity.DistributorOrderRequest;
import com.example.inventory_factory_management.service.OrderService;
import com.example.inventory_factory_management.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/distributor")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<BaseResponseDTO<Page<ProductDTO>>> getAllProducts(
            @Valid @ModelAttribute BaseRequestDTO request,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId) {

        BaseResponseDTO<Page<ProductDTO>> response = productService.getAllProducts(request, search, categoryId, "ACTIVE");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{id}/detail")
    public ResponseEntity<BaseResponseDTO<ProductDTO>> getProductDetail(@PathVariable Long id) {
        BaseResponseDTO<ProductDTO> response = productService.getProductDetail(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/orders/create")
    public BaseResponseDTO<DistributorOrderRequest> placeOrder(@Valid @RequestBody PlaceOrderDTO orderRequest) {
        try {
            DistributorOrderRequest order = orderService.placeOrder(orderRequest);
            return BaseResponseDTO.success("Order placed successfully", order);
        } catch (RuntimeException e) {
            return BaseResponseDTO.error(e.getMessage());
        }
    }

    @GetMapping("/my-orders")
    public BaseResponseDTO<Page<DistributorOrderRequest>> getMyOrders(BaseRequestDTO request) {
        try {
            OrderFilterDTO filter = new OrderFilterDTO();
            Page<DistributorOrderRequest> orders = orderService.getOrdersWithFilters(filter, request);
            return BaseResponseDTO.success("orders retrieved", orders);
        } catch (RuntimeException e) {
            return BaseResponseDTO.error(e.getMessage());
        }
    }


    @PostMapping("/{orderId}/pay")
    public BaseResponseDTO<String> processPayment(@PathVariable Long orderId) {
        try {
            orderService.processPayment(orderId);
            return BaseResponseDTO.success("Payment processed successfully");
        } catch (RuntimeException e) {
            return BaseResponseDTO.error(e.getMessage());
        }
    }



//    For chief officers
    @PreAuthorize("hasRole('CENTRAL_OFFICER')")
    @GetMapping("/orders")
    public BaseResponseDTO<Page<DistributorOrderRequest>> getOrdersWithFilters(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String distributorName,
            BaseRequestDTO request) {
        try {
            OrderFilterDTO filter = new OrderFilterDTO();
            filter.setStatus(status);
            filter.setDistributorName(distributorName);

            Page<DistributorOrderRequest> orders = orderService.getOrdersWithFilters(filter, request);
            return BaseResponseDTO.success("Orders retrieved successfully", orders);
        } catch (RuntimeException e) {
            return BaseResponseDTO.error(e.getMessage());
        }
    }


    @PreAuthorize("hasRole('CENTRAL_OFFICER')")
    @PostMapping("/{orderId}/process")
    public BaseResponseDTO<String> processOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderActionDTO actionRequest) {
        try {
            orderService.processOrderAction(orderId, actionRequest);
            String message = "";
            switch (actionRequest.getStatus()) {
                case APPROVED:
                    message = "Order approved successfully";
                    break;
                case REJECTED:
                    message = "Order rejected successfully";
                    break;
                case DELIVERED:
                    message = "Order marked as delivered successfully";
                    break;
                default:
                    message = "Order processed successfully";
            }
            return BaseResponseDTO.success(message);
        } catch (RuntimeException e) {
            return BaseResponseDTO.error(e.getMessage());
        }
    }

}
