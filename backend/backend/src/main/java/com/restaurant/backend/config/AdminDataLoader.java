package com.restaurant.backend.config;

import com.restaurant.backend.entity.Admin;
import com.restaurant.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminDataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createDefaultAdmin();
    }

    private void createDefaultAdmin() {
        String adminEmail = "admin@restaurant.com";
        String adminPassword = "admin123";

        // Check if admin already exists
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            Admin admin = new Admin(
                    "System Administrator",
                    adminEmail,
                    passwordEncoder.encode(adminPassword)  // ✅ Password encode karo
            );

            userRepository.save(admin);

            System.out.println("==========================================");
            System.out.println("✅ PERMANENT ADMIN CREATED SUCCESSFULLY!");
            System.out.println("📧 Email: " + adminEmail);
            System.out.println("🔑 Password: " + adminPassword);  // ✅ Plain password show karo
            System.out.println("🎯 Role: ADMIN");
            System.out.println("==========================================");
        } else {
            System.out.println("✅ Admin already exists: " + adminEmail);
        }
    }
}