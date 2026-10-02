package com.hanu.AiEcommerce.auth;

import com.hanu.AiEcommerce.common.exception.ResourceNotFoundException;
import com.hanu.AiEcommerce.security.JwtService;
import com.hanu.AiEcommerce.user.dto.LoginRequest;
import com.hanu.AiEcommerce.user.dto.LoginResponse;
import com.hanu.AiEcommerce.user.entity.User;
import com.hanu.AiEcommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid email or password"
                ));

        if(!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().toString()
        );

        return new LoginResponse(
                token,
                user.getEmail(),
                user.getId(),
                user.getRole()
        );
    }
}
