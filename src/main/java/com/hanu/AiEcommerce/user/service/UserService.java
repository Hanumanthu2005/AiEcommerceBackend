package com.hanu.AiEcommerce.user.service;

import com.hanu.AiEcommerce.user.dto.CreateUserRequest;
import com.hanu.AiEcommerce.user.dto.UserResponse;
import com.hanu.AiEcommerce.user.entity.User;
import com.hanu.AiEcommerce.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("User is alredy exist with email");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(request.password())
                .build();

        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    //==================== helper ===================

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .createAt(user.getCreatedAt())
                .build();
    }
}
