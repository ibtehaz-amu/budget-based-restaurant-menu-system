package com.restaurant.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public String test() {
        System.out.println("✅ Test endpoint called!");
        return "✅ Backend is working! - Test endpoint";
    }

    @GetMapping("/auth")
    public String authTest() {
        System.out.println("✅ Auth test endpoint called!");
        return "✅ Auth test endpoint is working!";
    }
}