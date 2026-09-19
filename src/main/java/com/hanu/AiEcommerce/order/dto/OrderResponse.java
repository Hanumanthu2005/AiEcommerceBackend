package com.hanu.AiEcommerce.order.dto;

import com.hanu.AiEcommerce.order.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        OrderStatus status,
        BigDecimal totalPrice,
        List<OrderItemResponse> items,
        Long version,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
}
