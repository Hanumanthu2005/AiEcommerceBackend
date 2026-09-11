package com.hanu.AiEcommerce.product.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ProductResponse(
        Long id,
        String name,
        String description,
        String sku,
        BigDecimal price,
        Long sellerId,
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt
) {
}
