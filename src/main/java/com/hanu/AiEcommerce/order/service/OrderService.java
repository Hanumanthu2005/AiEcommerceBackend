package com.hanu.AiEcommerce.order.service;

import com.hanu.AiEcommerce.common.exception.InvalidOrderStateException;
import com.hanu.AiEcommerce.common.exception.ResourceNotFoundException;
import com.hanu.AiEcommerce.inventory.service.InventoryService;
import com.hanu.AiEcommerce.order.dto.CreateOrderRequest;
import com.hanu.AiEcommerce.order.dto.OrderItemResponse;
import com.hanu.AiEcommerce.order.dto.OrderResponse;
import com.hanu.AiEcommerce.order.entity.Order;
import com.hanu.AiEcommerce.order.entity.OrderItem;
import com.hanu.AiEcommerce.order.enums.OrderStatus;
import com.hanu.AiEcommerce.order.repository.OrderRepository;
import com.hanu.AiEcommerce.product.entity.Product;
import com.hanu.AiEcommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    @Transactional
    public OrderResponse createOrder(
            Long userId,
            CreateOrderRequest request
    ) {

        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .userId(userId)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total =  BigDecimal.ZERO;

        for(var item : request.items()) {

            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id " + item.productId()
                    ));

            inventoryService.reserveStock(
                    product.getId(),
                    item.quantity()
            );

            BigDecimal unitPrice = product.getPrice();

            BigDecimal subTotal = unitPrice.multiply(
                    BigDecimal.valueOf(item.quantity())
            );

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .quantity(item.quantity())
                    .unitPrice(unitPrice)
                    .subTotal(subTotal)
                    .build();

            order.addItem(orderItem);

            total = total.add(subTotal);
        }

        order.setTotal(total);

        order = orderRepository.save(order);

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id " + orderId
                ));

        if(!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        }

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getByUserId(
            Long userId,
            Pageable pageable
    ) {
        return orderRepository
                .findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id " + orderId
                ));

        if(!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Order not found with id " + orderId
            );
        }

        if(!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new InvalidOrderStateException(
                    "Only pending order can be cancelled"
            );
        }

        for(OrderItem item : order.getItems()) {

            inventoryService.releaseStock(
                    item.getProductId(),
                    item.getQuantity()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        order = orderRepository.save(order);

        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse confirmOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id " + orderId
                ));

        if(!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new InvalidOrderStateException(
                    "Order cannot be confirmed from staus " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.CONFIRMED);

        order = orderRepository.save(order);

        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrderAfterPaymentFailure(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id " + orderId
                ));

        if(!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new InvalidOrderStateException(
                    "Order cannot be cancelled from staus " + order.getStatus()
            );
        }

        for(OrderItem item : order.getItems()) {

            inventoryService.releaseStock(
                    item.getProductId(),
                    item.getQuantity()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        order = orderRepository.save(order);

        return mapToResponse(order);
    }

    // ====================== HELPERS =========================

    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(orderItem -> new OrderItemResponse(
                        orderItem.getProductId(),
                        orderItem.getQuantity(),
                        orderItem.getUnitPrice(),
                        orderItem.getSubTotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotal(),
                items,
                order.getVersion(),
                order.getCreateAt(),
                order.getUpdatedAt()
        );
    }
}
