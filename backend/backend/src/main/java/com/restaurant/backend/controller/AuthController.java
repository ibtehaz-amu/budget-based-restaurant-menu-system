package com.restaurant.backend.controller;

import com.restaurant.backend.service.AuthService;
import com.restaurant.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("🔑 LOGIN ATTEMPT - Email: " + request.getEmail());
            System.out.println("🔑 Password length: " + (request.getPassword() != null ? request.getPassword().length() : 0));

            Map<String, Object> response = authService.loginUser(request.getEmail(), request.getPassword());

            System.out.println("✅ LOGIN SUCCESS - Email: " + request.getEmail());
            System.out.println("✅ Response keys: " + response.keySet());
            System.out.println("✅ Token in response: " + (response.containsKey("token") ? "YES" : "NO"));

            if(response.containsKey("token")) {
                String token = (String) response.get("token");
                System.out.println("✅ Token length: " + (token != null ? token.length() : 0));
                System.out.println("✅ Token preview: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "NULL"));
            }

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.out.println("❌ LOGIN FAILED - " + request.getEmail() + " - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("❌ LOGIN UNEXPECTED ERROR - " + request.getEmail() + " - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            System.out.println("========== 🎯 REGISTRATION START ==========");
            System.out.println("📧 Email: " + request.getEmail());
            System.out.println("👤 Name: " + request.getName());
            System.out.println("🎭 Role: " + request.getRole());
            System.out.println("📞 Phone: " + request.getPhoneNumber());
            System.out.println("===========================================");

            Map<String, Object> response = authService.registerUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getRole(),
                    request.getName(),
                    request.getPhoneNumber()
            );

            System.out.println("✅ REGISTRATION SUCCESS - User: " + request.getEmail());
            System.out.println("========== 🎉 REGISTRATION END ==========");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.out.println("❌ REGISTRATION FAILED - " + request.getEmail() + " - Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========== ❌ REGISTRATION FAILED ==========");
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("❌ REGISTRATION UNEXPECTED ERROR - " + request.getEmail() + " - Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========== 💥 UNEXPECTED ERROR ==========");
            return ResponseEntity.badRequest().body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    // ✅ SIMPLE REGISTER ENDPOINT (Backup)
    @PostMapping("/register-simple")
    public ResponseEntity<?> registerSimple(@RequestBody Map<String, String> request) {
        try {
            System.out.println("========== 🔧 SIMPLE REGISTER START ==========");
            String email = request.get("email");
            String password = request.get("password");
            String role = request.get("role");
            String name = request.get("name");
            String phoneNumber = request.get("phoneNumber");

            System.out.println("📧 Email: " + email);
            System.out.println("👤 Name: " + name);
            System.out.println("🎭 Role: " + role);
            System.out.println("📞 Phone: " + phoneNumber);

            Map<String, Object> response = authService.registerUser(email, password, role, name, phoneNumber);

            System.out.println("✅ SIMPLE REGISTER SUCCESS - User: " + email);
            System.out.println("========== 🔧 SIMPLE REGISTER END ==========");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ SIMPLE REGISTER FAILED - Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========== ❌ SIMPLE REGISTER FAILED ==========");
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test-token")
    public ResponseEntity<?> testToken(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("🔐 TOKEN VALIDATION ATTEMPT");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("❌ INVALID AUTHORIZATION HEADER");
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid authorization header"));
            }

            String token = authHeader.substring(7);
            System.out.println("🔑 Token: " + token.substring(0, 20) + "...");

            if (!jwtUtil.validateToken(token)) {
                System.out.println("❌ INVALID TOKEN");
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
            }

            String email = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            System.out.println("✅ TOKEN VALID - User: " + email + ", Role: " + role);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Token is valid!");
            response.put("email", email);
            response.put("role", role);
            response.put("isValid", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ TOKEN VALIDATION FAILED - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Token validation failed: " + e.getMessage()));
        }
    }

    // ✅ DTO CLASSES - STATIC
    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String email;
        private String password;
        private String role;
        private String name;
        private String phoneNumber;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }
}