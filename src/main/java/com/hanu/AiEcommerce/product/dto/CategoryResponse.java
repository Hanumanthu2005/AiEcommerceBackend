package com.hanu.AiEcommerce.product.dto;

import lombok.Builder;

@Builder
public record CategoryResponse(
        Long id,
        String name,
        String description
) {
}
