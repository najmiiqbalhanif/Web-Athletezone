package com.athletezone.web.services;

import com.athletezone.web.dto.UserDTO;
import com.athletezone.web.models.User;

public interface UserService {
    User registerUser(UserDTO userDTO);

    User registerUserApp(UserDTO userDTO);

    boolean authenticateUser(String email, String password);

    User findByEmail(String email);

    public User getUserById(Long id);

    public UserDTO DTOgetUserById(Long id);
}