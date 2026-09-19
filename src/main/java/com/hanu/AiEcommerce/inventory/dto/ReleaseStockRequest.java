package com.hanu.AiEcommerce.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReleaseStockRequest(

        @NotNull(message = "Quantity is required")
        @Positive(message = "Release quantity must be greater than zero")
        Integer quantity
) {
}
