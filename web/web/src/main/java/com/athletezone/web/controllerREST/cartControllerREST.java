package com.athletezone.web.controllerREST;

import com.athletezone.web.dto.CartItemDTO;
import com.athletezone.web.models.Cart;
import com.athletezone.web.models.CartItem;
import com.athletezone.web.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class cartControllerREST {

    private final CartService cartService;

    // 🔹 Tambahkan produk ke keranjang
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestParam Long userId, @RequestParam Long productId) {
        try {
            cartService.addToCart(userId, productId);
            return ResponseEntity.ok("Product added to cart successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add product to cart: " + e.getMessage());
        }
    }

    // 🔹 Ambil daftar item dalam cart (dengan DTO)
    @GetMapping("/items/{userId}")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@PathVariable Long userId) {
        try {
            List<CartItem> cartItems = cartService.getCartItemsByUserId(userId);

            // Konversi ke DTO
            List<CartItemDTO> cartItemDTOs = cartItems.stream().map(item -> CartItemDTO.builder()
                    .productName(item.getProduct().getName())
                    .quantity(item.getQuantity())
                    .price(item.getProduct().getPrice())
                    .subTotal(item.getSubTotal())
                    .build()).collect(Collectors.toList());

            return ResponseEntity.ok(cartItemDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 🔹 Ambil keseluruhan cart (untuk total harga)
    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCartByUserId(@PathVariable Long userId) {
        try {
            Cart cart = cartService.getCartByUserId(userId);
            if (cart == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 🔹 Hapus seluruh isi keranjang
    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<String> clearCart(@PathVariable Long userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok("Cart cleared successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to clear cart: " + e.getMessage());
        }
    }
}
