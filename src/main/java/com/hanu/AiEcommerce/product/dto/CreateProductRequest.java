package com.hanu.AiEcommerce.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 200)
        String name,

        @NotBlank(message = "Product description is required")
        @Size(max = 2000)
        String description,

        @NotBlank(message = "Sku is required")
        @Size(max = 50)
        String sku,

        @NotNull(message = "Product price is required")
        @DecimalMin(
                value = "0.01",
                message = "Price must be grater than zero"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "Invalid price format"
        )
        BigDecimal price,

        @NotNull(message = "Seller id is required")
        Long sellerId,

        @NotNull(message = "Category id is required")
        Long categoryId
) {
}
