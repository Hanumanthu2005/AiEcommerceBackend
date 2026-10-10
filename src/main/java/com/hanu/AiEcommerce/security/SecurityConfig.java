
package com.hanu.AiEcommerce.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) ->
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized: valid authentication is required"
                );
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) ->
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Forbidden: you do not have permission to access this resource"
                );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/users",
                                "/api/v1/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/categories"
                        ).hasAnyRole("ADMIN", "SELLER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/inventory"
                        ).hasAnyRole("ADMIN", "SELLER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/inventory/product/**"
                        ).hasAnyRole("ADMIN", "SELLER")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/inventory/product/*/reserve",
                                "/api/v1/inventory/product/*/release"
                        ).denyAll()

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/inventory/product/*/stock"
                        ).hasAnyRole("ADMIN", "SELLER")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
