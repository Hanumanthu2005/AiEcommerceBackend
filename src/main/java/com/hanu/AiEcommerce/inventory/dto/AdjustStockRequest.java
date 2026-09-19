package com.hanu.AiEcommerce.inventory.dto;

import jakarta.validation.constraints.NotNull;

public record AdjustStockRequest(

        @NotNull(message = "Quantity change is required")
        Integer quantity
) {
}
