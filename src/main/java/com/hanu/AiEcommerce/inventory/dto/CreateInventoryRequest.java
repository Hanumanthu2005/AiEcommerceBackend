package com.hanu.AiEcommerce.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateInventoryRequest(
        @NotNull(message = "product id is required")
        Long productId,

        @NotNull(message = "quantity is required")
        @PositiveOrZero(message = "quantity cannot be negative")
        Integer quantity
) {
}
