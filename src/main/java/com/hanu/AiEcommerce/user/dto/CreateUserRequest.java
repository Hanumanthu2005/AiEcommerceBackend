package com.hanu.AiEcommerce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest (
        @NotBlank(message = "First Name is required")
        @Size(max = 50, message = "First name must not exceeds 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceeds 50 characters")
                String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid Email format")
                String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be minimum of 8 characters")
                String password
){
}
