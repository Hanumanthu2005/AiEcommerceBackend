package com.hanu.AiEcommerce.user.dto;

import com.hanu.AiEcommerce.user.entity.Role;

public record LoginResponse(

        String token,
        String email,
        Long userId,
        Role role
) {
}
