package com.athletezone.web.services.impl;

import com.athletezone.web.models.Cart;
import com.athletezone.web.models.CartItem;
import com.athletezone.web.models.Product;
import com.athletezone.web.models.User;
import com.athletezone.web.repositories.CartItemRepository;
import com.athletezone.web.repositories.CartRepository;
import com.athletezone.web.repositories.ProductRepository;
import com.athletezone.web.repositories.UserRepository;   // << import UserRepository
import com.athletezone.web.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;   // << tambahkan ini

    @Override
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    public List<CartItem> getCartItemsByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found for user ID: " + userId);
        }
        return cartItemRepository.findByCartId(cart.getId());
    }

    @Override
    public void addToCart(Long userId, Long productId) {
        // Cari cart berdasarkan user
        Cart cart = cartRepository.findByUserId(userId);

        if (cart == null) {
            cart = new Cart();

            // Ambil objek User dari userId
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            cart.setUser(user);  // Set User pada Cart
            cart = cartRepository.save(cart); // Simpan cart baru dulu supaya dapat ID
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Cek apakah produk sudah ada di cart item
        Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItem.setSubTotal(cartItem.getQuantity() * product.getPrice());
            cartItemRepository.save(cartItem);
        } else {
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(1)
                    .subTotal(product.getPrice())
                    .build();
            cartItemRepository.save(newCartItem);

            // Perbarui koleksi cartItems agar tetap konsisten
            cart.getCartItems().add(newCartItem);
        }

        // Hitung ulang total harga berdasarkan cartItems yang terbaru
        double totalPrice = cartItemRepository.findByCartId(cart.getId())
                .stream()
                .mapToDouble(CartItem::getSubTotal)
                .sum();
        cart.setTotalPrice(totalPrice);

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart != null) {
            cartItemRepository.deleteAllByCartId(cart.getId());
            cart.setTotalPrice(0);
            cartRepository.save(cart);
        }
    }
}
