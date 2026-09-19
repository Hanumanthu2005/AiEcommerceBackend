package com.hanu.AiEcommerce.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReserveStockRequest(

        @NotNull(message = "Reserved stock quantity is required")
        @Positive(message = "Reserved stock quantity must be greater than zero")
        Integer quantity
) {
}
