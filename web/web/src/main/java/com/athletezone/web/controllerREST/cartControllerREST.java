package com.athletezone.web.controllerREST;

import com.athletezone.web.dto.CartItemDTO;
import com.athletezone.web.models.CartItem;
import com.athletezone.web.models.Product;
import com.athletezone.web.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class cartControllerREST {

    private final CartService cartService;

    // Endpoint untuk menambahkan produk ke cart
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestParam Long userId, @RequestParam Long productId) {
        try {
            cartService.addToCart(userId, productId);
            return ResponseEntity.ok("Product added to cart successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add product to cart: " + e.getMessage());
        }
    }

    private static final String BASE_IMAGE_URL = "http://10.0.2.2:8080/";

    // Ambil semua item cart berdasarkan userId
    @GetMapping("/items/{userId}")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@PathVariable Long userId) {
        try {
            List<CartItem> cartItems = cartService.getCartItemsByUserId(userId);

            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                    .map(item -> {
                        String rawPath = item.getProduct().getPhotoUrl() != null
                                ? item.getProduct().getPhotoUrl().replace("\\", "/")
                                : "";
                        String fullImageUrl = rawPath.isEmpty() ? "" : BASE_IMAGE_URL + rawPath;

                        return CartItemDTO.builder()
                                .id(item.getProduct().getId())
                                .name(item.getProduct().getName())
                                .photoUrl(fullImageUrl)  // Di sini URL berbeda untuk setiap produk
                                .price(item.getProduct().getPrice())
                                .category(item.getProduct().getCategory())
                                .brand(item.getProduct().getBrand())
                                .quantity(item.getQuantity())
                                .build();
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(cartItemDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}