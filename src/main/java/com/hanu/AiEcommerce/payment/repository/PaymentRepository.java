package com.hanu.AiEcommerce.payment.repository;

import com.hanu.AiEcommerce.payment.entity.Payment;
import com.hanu.AiEcommerce.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    boolean existsByOrderId(Long OrderId);

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);

    long countByStatus(PaymentStatus status);
}
