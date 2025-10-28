package com.restaurant.backend.controller;

import com.restaurant.backend.entity.*;
import com.restaurant.backend.repository.*;
import com.restaurant.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ HELPER: Get current user from JWT token
    private User getCurrentUser(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Invalid authorization");
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    // ✅ 1. STUDENT: Add feedback for a dish
    @PostMapping("/student/dishes/{dishId}/add")
    public ResponseEntity<?> addFeedback(
            @PathVariable Long dishId,
            @RequestBody FeedbackRequest feedbackRequest,
            HttpServletRequest request) {

        try {
            System.out.println("=== ADD FEEDBACK FOR DISH: " + dishId + " ===");

            // Get current student
            User student = getCurrentUser(request);
            if (student.getRole() != User.Role.STUDENT) {
                throw new RuntimeException("Only students can add feedback");
            }

            // Find dish
            Dish dish = dishRepository.findById(dishId)
                    .orElseThrow(() -> new RuntimeException("Dish not found"));

            // Check if student already gave feedback
            if (feedbackRepository.existsByStudentAndDish(student, dish)) {
                throw new RuntimeException("You have already given feedback for this dish");
            }

            // Validate rating
            if (feedbackRequest.getRating() < 1 || feedbackRequest.getRating() > 5) {
                throw new RuntimeException("Rating must be between 1 and 5");
            }

            // Create and save feedback
            Feedback feedback = new Feedback(
                    feedbackRequest.getRating(),
                    feedbackRequest.getComment(),
                    student,
                    dish
            );

            Feedback savedFeedback = feedbackRepository.save(feedback);

            System.out.println("✅ Feedback added - Dish: " + dish.getName() + ", Rating: " + feedbackRequest.getRating());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Feedback submitted successfully");
            response.put("feedbackId", savedFeedback.getFeedbackId());
            response.put("rating", savedFeedback.getRating());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Error adding feedback: " + e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ 2. STUDENT: Get my feedback history
    @GetMapping("/student/my-feedback")
    public ResponseEntity<?> getMyFeedback(HttpServletRequest request) {
        try {
            System.out.println("=== GET MY FEEDBACK ===");

            User student = getCurrentUser(request);
            if (student.getRole() != User.Role.STUDENT) {
                throw new RuntimeException("Access denied");
            }

            List<Feedback> feedbackList = feedbackRepository.findByStudentWithRestaurant(student);

            // Convert to simple response
            List<Map<String, Object>> simpleFeedback = feedbackList.stream()
                    .map(feedback -> {
                        Map<String, Object> fb = new HashMap<>();
                        fb.put("feedbackId", feedback.getFeedbackId());
                        fb.put("rating", feedback.getRating());
                        fb.put("comment", feedback.getComment());
                        fb.put("createdAt", feedback.getCreatedAt());
                        fb.put("dishName", feedback.getDishName());
                        fb.put("restaurantName", feedback.getRestaurantName());
                        return fb;
                    })
                    .collect(Collectors.toList());

            System.out.println("✅ Found " + simpleFeedback.size() + " feedback entries");

            return ResponseEntity.ok(simpleFeedback);

        } catch (Exception e) {
            System.out.println("❌ Error getting feedback: " + e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ 3. OWNER: Get feedback for my restaurants
    @GetMapping("/owner/restaurants/{restaurantId}/feedback")
    public ResponseEntity<?> getRestaurantFeedback(
            @PathVariable Long restaurantId,
            HttpServletRequest request) {

        try {
            System.out.println("=== GET RESTAURANT FEEDBACK: " + restaurantId + " ===");

            // Verify owner and restaurant ownership
            User owner = getCurrentUser(request);
            if (owner.getRole() != User.Role.OWNER) {
                throw new RuntimeException("Access denied: Only owners can view restaurant feedback");
            }

            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            // Security check: Restaurant belongs to current owner
            if (!restaurant.getOwner().getUserId().equals(owner.getUserId())) {
                throw new RuntimeException("Access denied: You can only view feedback for your own restaurants");
            }

            List<Feedback> feedbackList = feedbackRepository.findByRestaurant(restaurant);

            // Calculate average rating
            Optional<Double> avgRatingOpt = feedbackRepository.findAverageRatingByRestaurant(restaurant);
            Double avgRating = avgRatingOpt.orElse(0.0);

            // Convert to simple response
            List<Map<String, Object>> simpleFeedback = feedbackList.stream()
                    .map(feedback -> {
                        Map<String, Object> fb = new HashMap<>();
                        fb.put("feedbackId", feedback.getFeedbackId());
                        fb.put("rating", feedback.getRating());
                        fb.put("comment", feedback.getComment());
                        fb.put("createdAt", feedback.getCreatedAt());
                        fb.put("studentName", feedback.getStudentName());
                        fb.put("dishName", feedback.getDishName());
                        return fb;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("restaurantName", restaurant.getName());
            response.put("totalFeedback", feedbackList.size());
            response.put("averageRating", Math.round(avgRating * 10.0) / 10.0); // Round to 1 decimal
            response.put("feedback", simpleFeedback);

            System.out.println("✅ Found " + feedbackList.size() + " feedback entries for restaurant");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Error getting restaurant feedback: " + e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ 4. ADMIN: Get all feedback in system
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllFeedback(HttpServletRequest request) {
        try {
            System.out.println("=== GET ALL FEEDBACK ===");

            User admin = getCurrentUser(request);
            if (admin.getRole() != User.Role.ADMIN) {
                throw new RuntimeException("Access denied: Only admins can view all feedback");
            }

            List<Feedback> feedbackList = feedbackRepository.findAll();

            // Convert to simple response
            List<Map<String, Object>> simpleFeedback = feedbackList.stream()
                    .map(feedback -> {
                        Map<String, Object> fb = new HashMap<>();
                        fb.put("feedbackId", feedback.getFeedbackId());
                        fb.put("rating", feedback.getRating());
                        fb.put("comment", feedback.getComment());
                        fb.put("createdAt", feedback.getCreatedAt());
                        fb.put("studentName", feedback.getStudentName());
                        fb.put("dishName", feedback.getDishName());
                        fb.put("restaurantName", feedback.getRestaurantName());
                        return fb;
                    })
                    .collect(Collectors.toList());

            // ✅ FIXED LINE: Remove "+ total"
            System.out.println("✅ Found " + feedbackList.size() + " feedback entries");

            return ResponseEntity.ok(simpleFeedback);

        } catch (Exception e) {
            System.out.println("❌ Error getting all feedback: " + e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ 5. Get dish rating summary
    @GetMapping("/dishes/{dishId}/rating")
    public ResponseEntity<?> getDishRating(@PathVariable Long dishId) {
        try {
            System.out.println("=== GET DISH RATING: " + dishId + " ===");

            Dish dish = dishRepository.findById(dishId)
                    .orElseThrow(() -> new RuntimeException("Dish not found"));

            Optional<Double> avgRatingOpt = feedbackRepository.findAverageRatingByDish(dish);
            Double avgRating = avgRatingOpt.orElse(0.0);

            List<Feedback> feedbackList = feedbackRepository.findByDish(dish);
            long ratingCount = feedbackList.size();

            Map<String, Object> response = new HashMap<>();
            response.put("dishId", dishId);
            response.put("dishName", dish.getName());
            response.put("averageRating", Math.round(avgRating * 10.0) / 10.0);
            response.put("ratingCount", ratingCount);
            response.put("restaurantName", dish.getRestaurant().getName());

            System.out.println("✅ Dish rating - Average: " + avgRating + ", Count: " + ratingCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Error getting dish rating: " + e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ DTO for feedback request
    public static class FeedbackRequest {
        private Integer rating;
        private String comment;

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}