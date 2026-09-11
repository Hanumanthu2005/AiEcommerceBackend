package com.hanu.AiEcommerce.user.dto;

import com.hanu.AiEcommerce.user.entity.Role;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        LocalDateTime createdAt,
        Role role
) {
}
