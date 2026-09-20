package com.hanu.AiEcommerce.payment.controller;

import com.hanu.AiEcommerce.payment.dto.PaymentRequest;
import com.hanu.AiEcommerce.payment.dto.PaymentResponse;
import com.hanu.AiEcommerce.payment.dto.PaymentStatusUpdateRequest;
import com.hanu.AiEcommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            PaymentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        paymentService.createPayment(request)
                );
    }

    @PatchMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable
            Long paymentId,

            @Valid
            @RequestBody
            PaymentStatusUpdateRequest request
    ) {

        return ResponseEntity.ok(
                paymentService.updatePaymentStatus(
                        paymentId,
                        request.status(),
                        request.transactionId()
                )
        );
    }
}
