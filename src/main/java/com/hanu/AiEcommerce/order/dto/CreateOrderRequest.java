package com.hanu.AiEcommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(

        @NotEmpty(message = "Order must contain at least minimum one item")
        List<@Valid CreateOrderItemRequest> items
) {
}
