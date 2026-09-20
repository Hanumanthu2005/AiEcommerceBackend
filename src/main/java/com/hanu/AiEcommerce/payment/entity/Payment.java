package com.hanu.AiEcommerce.payment.entity;

import com.hanu.AiEcommerce.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payments_order_id",
                        columnNames = "order_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_payments_order_id",
                        columnList = "order_id"
                ),
                @Index(
                        name = "idx_payments_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "transaction_id", length = 200)
    private String transactionId;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if(status == null) {
            status = PaymentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
         updatedAt = LocalDateTime.now();
    }
}
