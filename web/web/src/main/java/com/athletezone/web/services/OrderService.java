package com.athletezone.web.services;

import com.athletezone.web.dto.OrderDTO;
import com.athletezone.web.models.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public interface OrderService {
    public List<OrderDTO> getAllOrders();

    public List<Order> getOrdersByUserId(Long userId);

    public void updateOrderStatus(Long orderId, String newStatus);

    public Order saveOrder(Order order);
}
