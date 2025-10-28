package com.restaurant.backend.service;

import com.restaurant.backend.entity.User;
import com.restaurant.backend.repository.UserRepository;
import com.restaurant.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.restaurant.backend.entity.Owner;
import com.restaurant.backend.entity.Admin;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> registerUser(String email, String password, String role, String name, String phoneNumber) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User already exists with this email");
        }

        User user;
        User.Role userRole = User.Role.valueOf(role.toUpperCase());

        // ✅ ROLE-BASED USER CREATION
        switch(userRole) {
            case OWNER:
                // Owners require phone number for business communication
                if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                    throw new RuntimeException("Phone number is required for restaurant owners");
                }
                user = new Owner(name, email, passwordEncoder.encode(password), phoneNumber);
                break;

            case ADMIN:
                user = new Admin(name, email, passwordEncoder.encode(password));
                break;

            case STUDENT:
            default:
                user = new User(name, email, passwordEncoder.encode(password), userRole);
                // If phone provided for student, set it separately
                if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                    user.setPhoneNumber(phoneNumber);
                }
        }

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        // ✅ FIXED: USE HASHMAP INSTEAD OF Map.of()
        Map<String, Object> response = new HashMap<>();

        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", savedUser.getUserId());
        userResponse.put("email", savedUser.getEmail());
        userResponse.put("role", savedUser.getRole().name());
        userResponse.put("name", savedUser.getName());
        userResponse.put("phoneNumber", savedUser.getPhoneNumber()); // ✅ Null safe

        response.put("token", token);
        response.put("user", userResponse);
        response.put("message", "Registration successful");

        return response;
    }

    public Map<String, Object> loginUser(String email, String password) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // ✅ FIXED: USE HASHMAP INSTEAD OF Map.of()
        Map<String, Object> response = new HashMap<>();

        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getUserId());
        userResponse.put("email", user.getEmail());
        userResponse.put("role", user.getRole().name());
        userResponse.put("name", user.getName());
        userResponse.put("phoneNumber", user.getPhoneNumber()); // ✅ Null safe

        response.put("token", token);
        response.put("user", userResponse);
        response.put("message", "Login successful");

        return response;
    }
}