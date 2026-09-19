package com.hanu.AiEcommerce.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(
                        name = "idx_order_items_order_id",
                        columnList = "order_id"
                ),

                @Index(
                        name = "idx_order_items_product_id",
                        columnList = "product_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(
            fetch =  FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_order")
    )
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subTotal;

    @PrePersist
    @PreUpdate
    protected void calculateSubTotal() {
        if(unitPrice != null && quantity != null) {
            subTotal = unitPrice.multiply(
                    BigDecimal.valueOf(quantity)
            );
        }
    }

}
