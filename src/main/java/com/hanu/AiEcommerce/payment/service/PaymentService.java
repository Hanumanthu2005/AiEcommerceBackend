package com.hanu.AiEcommerce.payment.service;


import com.hanu.AiEcommerce.common.exception.DuplicateResourceException;
import com.hanu.AiEcommerce.common.exception.InvalidOrderStateException;
import com.hanu.AiEcommerce.common.exception.InvalidPaymentStateException;
import com.hanu.AiEcommerce.common.exception.ResourceNotFoundException;
import com.hanu.AiEcommerce.order.entity.Order;
import com.hanu.AiEcommerce.order.enums.OrderStatus;
import com.hanu.AiEcommerce.order.repository.OrderRepository;
import com.hanu.AiEcommerce.order.service.OrderService;
import com.hanu.AiEcommerce.payment.dto.PaymentRequest;
import com.hanu.AiEcommerce.payment.dto.PaymentResponse;
import com.hanu.AiEcommerce.payment.entity.Payment;
import com.hanu.AiEcommerce.payment.enums.PaymentStatus;
import com.hanu.AiEcommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {

        if(paymentRepository.existsByOrderId(request.orderId())) {
            throw new DuplicateResourceException(
                    "Payment already exists for order id " + request.orderId()
            );
        }

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() ->  new ResourceNotFoundException(
                        "Order not found with id " + request.orderId()
                ));

        if(!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new InvalidPaymentStateException(
                    "Payment only created for pending orders"
            );
        }

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .amount(order.getTotal())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        return mapToResponse(payment);
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

        if(status == PaymentStatus.SUCCESS) {
            orderService.confirmOrder(payment.getOrderId());
        }

        if(status == PaymentStatus.FAILED) {
            orderService.cancelOrderAfterPaymentFailure(payment.getOrderId());
        }

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
                payment.getVersion(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
