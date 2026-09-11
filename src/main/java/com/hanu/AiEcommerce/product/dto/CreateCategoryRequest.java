package com.hanu.AiEcommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "Category name required")
        @Size(max = 100, message = "Name size must not exceeds 100 chars")
        String name,

        @Size(max = 500, message = "description size must not exceeds 500 chars")
        String description
) {
}
