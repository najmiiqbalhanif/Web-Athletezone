package com.athletezone.web.services.impl;

import com.athletezone.web.dto.OrderDTO;
import com.athletezone.web.models.Order;
import com.athletezone.web.models.Payment;
import com.athletezone.web.repositories.OrderRepository;
import com.athletezone.web.services.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::orderToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        Payment payment = order.getPayment();
        if (payment != null) {
            payment.setStatus(newStatus);
        }
    }

    @Override
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    private OrderDTO orderToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername()) // Ambil username dari user
                .createdOn(order.getPayment().getCreatedOn().toString()) // Tanggal order
                .cartSummary(order.getPayment().getPaymentItems().stream() // Ringkasan cart
                        .map(item -> item.getProductName() + " x " + item.getQuantity())
                        .collect(Collectors.joining(", ")))
                .totalAmount(order.getPayment().getTotalAmount()) // Total harga
                .paymentMethod(order.getPayment().getPaymentMethod()) // Metode pembayaran
                .paymentStatus(order.getPayment().getStatus()) // Status pembayaran
                .address(order.getPayment().getAddress()) // Alamat pembayaran
                .build();
    }
}
