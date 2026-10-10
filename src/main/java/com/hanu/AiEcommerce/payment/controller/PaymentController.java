package com.hanu.AiEcommerce.payment.controller;

import com.hanu.AiEcommerce.payment.dto.PaymentRequest;
import com.hanu.AiEcommerce.payment.dto.PaymentResponse;
import com.hanu.AiEcommerce.payment.dto.PaymentStatusUpdateRequest;
import com.hanu.AiEcommerce.payment.dto.VerifyPaymentRequest;
import com.hanu.AiEcommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid
            @RequestBody
            PaymentRequest request,

            Authentication authentication
    ) {

        Long userId = Long.parseLong(authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        paymentService.createPayment(request, userId)
                );
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                paymentService.verifyPayment(request, userId)
        );
    }
}
