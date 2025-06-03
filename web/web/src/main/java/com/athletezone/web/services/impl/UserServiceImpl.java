package com.athletezone.web.services.impl;

import com.athletezone.web.dto.UserDTO;
import com.athletezone.web.models.User;
import com.athletezone.web.models.Cart;
import com.athletezone.web.repositories.UserRepository;
import com.athletezone.web.repositories.CartRepository;
import com.athletezone.web.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
    }


    @Override
    public User registerUser(UserDTO userDTO) {
        userRepository.findByEmail(userDTO.getEmail()).ifPresent(user -> {
            throw new IllegalStateException("Email already in use");
        });

        // Buat user baru
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());

        user = userRepository.save(user);

        // Buat cart baru untuk user
        Cart cart = new Cart();
        cart.setUser(user); // Set user pada cart
        cartRepository.save(cart); // Simpan cart ke database

        // Set cart pada user
        user.setCart(cart);

        return userRepository.save(user);
    }

    @Override
    public User registerUserApp(UserDTO userDTO) {
        userRepository.findByEmail(userDTO.getEmail()).ifPresent(user -> {
            throw new IllegalStateException("Email already in use");
        });

        // Buat user baru
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setEmail(userDTO.getFullName());

        user = userRepository.save(user);

        // Buat cart baru untuk user
        Cart cart = new Cart();
        cart.setUser(user); // Set user pada cart
        cartRepository.save(cart); // Simpan cart ke database

        // Set cart pada user
        user.setCart(cart);

        return userRepository.save(user);
    }

    @Override
    public boolean authenticateUser(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> user.getPassword().equals(password)) // Simpan password dalam bentuk hash di produksi
                .orElse(false);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public UserDTO DTOgetUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return convertToDTO(user);
    }

    private static final String BASE_IMAGE_URL = "http://10.0.2.2:8080/"; // Masih untuk emulator, harusnya sesuaikan dengan backendnya

    private UserDTO convertToDTO(User user) {
        String profileImagePath = user.getProfileImage() != null ? user.getProfileImage().replace("\\", "/") : "";
        String fullProfileImageUrl = profileImagePath.isEmpty() ? "" : BASE_IMAGE_URL + profileImagePath;

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .fullName(user.getFullName())
                .profileImage(fullProfileImageUrl)
                .build();
    }
}