package com.restaurant.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "🚀 Restaurant Backend API is Running!\n\n" +
                "📧 Admin Login: admin@restaurant.com\n" +
                "🔑 Password: admin123\n\n" +
                "📚 API Documentation coming soon...";
    }

    @GetMapping("/health")
    public String health() {
        return "✅ Server is healthy and running!";
    }
}