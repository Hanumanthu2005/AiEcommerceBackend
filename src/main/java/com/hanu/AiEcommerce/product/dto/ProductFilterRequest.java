package com.hanu.AiEcommerce.product.dto;

import java.math.BigDecimal;

public record ProductFilterRequest(
        Long categoryId,
        Long sellerId,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
