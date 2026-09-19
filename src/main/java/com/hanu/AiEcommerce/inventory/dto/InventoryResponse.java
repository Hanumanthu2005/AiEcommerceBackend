package com.hanu.AiEcommerce.inventory.dto;

import java.time.LocalDateTime;

public record InventoryResponse(
        Long id,
        Long productId,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
