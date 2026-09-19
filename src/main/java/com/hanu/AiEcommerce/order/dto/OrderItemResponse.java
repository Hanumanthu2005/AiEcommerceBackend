package com.hanu.AiEcommerce.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subTotal
) {
}
