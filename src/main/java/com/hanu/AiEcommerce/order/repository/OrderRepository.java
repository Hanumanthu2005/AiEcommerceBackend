package com.hanu.AiEcommerce.order.repository;

import com.hanu.AiEcommerce.order.entity.Order;
import com.hanu.AiEcommerce.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
