package com.hanu.AiEcommerce.payment.service;


import com.hanu.AiEcommerce.common.event.PaymentEvent;
import com.hanu.AiEcommerce.common.exception.DuplicateResourceException;
import com.hanu.AiEcommerce.common.exception.InvalidPaymentStateException;
import com.hanu.AiEcommerce.common.exception.ResourceNotFoundException;
import com.hanu.AiEcommerce.common.outbox.OutboxEventService;
import com.hanu.AiEcommerce.order.entity.Order;
import com.hanu.AiEcommerce.order.enums.OrderStatus;
import com.hanu.AiEcommerce.order.repository.OrderRepository;
import com.hanu.AiEcommerce.payment.dto.PaymentRequest;
import com.hanu.AiEcommerce.payment.dto.PaymentResponse;
import com.hanu.AiEcommerce.payment.entity.Payment;
import com.hanu.AiEcommerce.payment.enums.PaymentStatus;
import com.hanu.AiEcommerce.payment.repository.PaymentRepository;
import com.hanu.AiEcommerce.payment.dto.VerifyPaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OutboxEventService outboxEventService;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;


    @Transactional
    public PaymentResponse createPayment(
            PaymentRequest request,
            Long authenticatedUserId
    ) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id " + request.orderId()
                ));

        if (!order.getUserId().equals(authenticatedUserId)) {
            throw new ResourceNotFoundException(
                    "Order not found with id " + request.orderId()
            );
        }

        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new InvalidPaymentStateException(
                    "Payment only created for pending orders"
            );
        }

        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new DuplicateResourceException(
                    "Payment already exists for order id " + order.getId()
            );
        }

        BigDecimal amount = order.getTotal()
                .setScale(2, RoundingMode.UNNECESSARY);

        long amountInPaise = amount
                .movePointRight(2)
                .longValueExact();

        if (amountInPaise <= 0) {
            throw new InvalidPaymentStateException(
                    "Payment amount must be greater than zero"
            );
        }

        try {
            JSONObject razorpayOrderRequest = new JSONObject();
            razorpayOrderRequest.put("amount", amountInPaise);
            razorpayOrderRequest.put("currency", "INR");
            razorpayOrderRequest.put(
                    "receipt",
                    "order_" + order.getId()
            );

            com.razorpay.Order razorpayOrder =
                    razorpayClient.orders.create(razorpayOrderRequest);

            String razorpayOrderId = razorpayOrder.get("id");

            Payment payment = Payment.builder()
                    .orderId(order.getId())
                    .amount(amount)
                    .status(PaymentStatus.PENDING)
                    .razorpayOrderId(razorpayOrderId)
                    .build();

            payment = paymentRepository.save(payment);

            return mapToResponse(payment);

        } catch (RazorpayException exception) {
            throw new IllegalStateException(
                    "Unable to create Razorpay order",
                    exception
            );
        }
    }

    @Transactional
    public PaymentResponse verifyPayment(
            VerifyPaymentRequest request,
            Long authenticatedUserId
    ) {
        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for Razorpay order"
                ));

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found"
                ));

        if (!order.getUserId().equals(authenticatedUserId)) {
            throw new ResourceNotFoundException("Payment not found");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Payment is not in PENDING state"
            );
        }

        if (!isValidRazorpaySignature(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        )) {
            throw new InvalidPaymentStateException(
                    "Invalid Razorpay payment signature"
            );
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(request.razorpayPaymentId());

        PaymentEvent paymentEvent = new PaymentEvent(
                UUID.randomUUID().toString(),
                "PAYMENT_SUCCEEDED",
                payment.getId(),
                payment.getOrderId(),
                payment.getTransactionId()
        );

        outboxEventService.saveEvent(
                paymentEvent.eventType(),
                "PAYMENT",
                payment.getId().toString(),
                paymentEvent
        );

        return mapToResponse(paymentRepository.save(payment));
    }


    @Transactional
    public PaymentResponse updatePaymentStatus(
            Long paymentId,
            PaymentStatus status,
            String transactionId
    ) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with id " + paymentId
                ));

        if(!payment.getStatus().equals(PaymentStatus.PENDING)) {
            throw new InvalidPaymentStateException(
                    "Payment status cannot be changed from " + payment.getStatus()
            );
        }

        payment.setStatus(status);
        payment.setTransactionId(transactionId);

        PaymentEvent paymentEvent = new PaymentEvent(
                UUID.randomUUID().toString(),
                status == PaymentStatus.SUCCESS
                ? "PAYMENT_SUCCEEDED" : "PAYMENT_FAILED",
                payment.getId(),
                payment.getOrderId(),
                payment.getTransactionId()
        );

        outboxEventService.saveEvent(
                paymentEvent.eventType(),
                "PAYMENT",
                payment.getId().toString(),
                paymentEvent
        );

        payment = paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    //================= HELPER ====================

    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getRazorpayOrderId(),
                razorpayKeyId,
                payment.getVersion(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    private boolean isValidRazorpaySignature(
            String razorpayOrderId,
            String razorpayPaymentId,
            String suppliedSignature
    ) {
        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    System.getenv("RAZORPAY_KEY_SECRET")
                            .getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(secretKey);

            String expectedSignature = HexFormat.of().formatHex(
                    mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))
            );

            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    suppliedSignature.getBytes(StandardCharsets.UTF_8)
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to verify Razorpay signature",
                    exception
            );
        }
    }
}
