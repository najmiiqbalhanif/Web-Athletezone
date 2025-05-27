package com.athletezone.web.services.impl;

import com.athletezone.web.dto.OrderDTO;
import com.athletezone.web.dto.PaymentItemDTO;
import com.athletezone.web.models.*;
import com.athletezone.web.repositories.OrderRepository;
import com.athletezone.web.repositories.PaymentItemRepository;
import com.athletezone.web.repositories.PaymentRepository;
import com.athletezone.web.repositories.ProductRepository;
import com.athletezone.web.services.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final PaymentRepository paymentRepository;

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

    @Transactional
    public Order processCheckout(User user, Payment payment, List<PaymentItemDTO> paymentItems) {
        payment.setStatus("PENDING");
        payment = paymentRepository.save(payment);

        Order order = new Order();
        order.setUser(user);
        order.setPayment(payment);
        order = orderRepository.save(order);

        for (PaymentItemDTO itemDTO : paymentItems) {
            PaymentItem paymentItem = new PaymentItem();
            paymentItem.setPayment(payment);
            paymentItem.setProductName(itemDTO.getProductName());
            paymentItem.setQuantity(itemDTO.getQuantity());
            paymentItem.setPrice(itemDTO.getPrice());
            paymentItem.setSubTotal(itemDTO.getSubTotal());

            paymentItemRepository.save(paymentItem);
        }

        return order;
    }

}
