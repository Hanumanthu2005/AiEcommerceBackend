package com.hanu.AiEcommerce.order.controller;

import com.hanu.AiEcommerce.order.dto.CreateOrderRequest;
import com.hanu.AiEcommerce.order.dto.OrderResponse;
import com.hanu.AiEcommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("X-User-Id")
            Long userId,

            @Valid
            @RequestBody
            CreateOrderRequest request
    ) {
        OrderResponse response = orderService.createOrder(
                userId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable
            Long orderId,

            @RequestHeader("X-User-Id")
            Long userId
    ) {
        return ResponseEntity.ok(
                orderService.getOrderById(orderId, userId)
        );
    }

    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestHeader("X-User-Id")
            Long userId,

            Pageable pageable
    ) {
        return ResponseEntity
                .ok(orderService.getByUserId(userId, pageable));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable
            Long orderId,

            @RequestHeader("X-User-Id")
            Long userId
    ) {

        return ResponseEntity
                .ok(orderService.cancelOrder(orderId, userId));
    }
}
