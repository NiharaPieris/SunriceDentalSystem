package com.sunrise.dental.service;

import com.sunrise.dental.dao.UserDAO;
import com.sunrise.dental.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User authenticate(String username, String rawPassword) {
        User user = userDAO.findByUsername(username);
        if (user != null && encoder.matches(rawPassword, user.getPassword())) {
            return user;
        }
        return null;
    }
}