package com.athletezone.web.controllerREST;

import com.athletezone.web.dto.PaymentDTO;
import com.athletezone.web.dto.PaymentItemDTO;
import com.athletezone.web.models.Order;
import com.athletezone.web.models.Payment;
import com.athletezone.web.models.User;
import com.athletezone.web.services.OrderService;
import com.athletezone.web.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkoutpayment")

public class checkoutPaymentControllerREST {
    private final OrderService orderService;
    private final UserService userService;

    public checkoutPaymentControllerREST(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }
    public static class CheckoutPaymentRequest extends PaymentDTO {
        private List<PaymentItemDTO> paymentItems;

        public List<PaymentItemDTO> getPaymentItems() {
            return paymentItems;
        }

        public void setPaymentItems(List<PaymentItemDTO> paymentItems) {
            this.paymentItems = paymentItems;
        }
    }
    @PostMapping("/submit")
    public ResponseEntity<?> submitCheckout(@RequestBody CheckoutPaymentRequest request) {
        User user = userService.getUserById(request.getUserId());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Payment payment = new Payment();
        payment.setAddress(request.getAddress());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTotalAmount(request.getTotalAmount());
        payment.setStatus("PENDING");

        try {
            Order order = orderService.processCheckout(user, payment, request.getPaymentItems());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to process checkout: " + e.getMessage());
        }
    }
}