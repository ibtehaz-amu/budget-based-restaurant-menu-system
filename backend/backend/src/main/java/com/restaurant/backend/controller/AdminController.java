package com.restaurant.backend.controller;

import com.restaurant.backend.entity.Admin;
import com.restaurant.backend.entity.Restaurant;
import com.restaurant.backend.entity.User;
import com.restaurant.backend.repository.AdminRepository;
import com.restaurant.backend.repository.RestaurantRepository;
import com.restaurant.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ 1. GET PENDING RESTAURANTS (FIXED - No circular reference)
    @GetMapping("/restaurants/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingRestaurants() {
        System.out.println("=== GET PENDING RESTAURANTS ===");
        List<Restaurant> pendingRestaurants = restaurantRepository.findByStatus(Restaurant.Status.PENDING);
        System.out.println("✅ Found " + pendingRestaurants.size() + " pending restaurants");

        // ✅ SIMPLE RESPONSE WITHOUT CIRCULAR REFERENCES
        List<Map<String, Object>> simpleRestaurants = pendingRestaurants.stream()
                .map(restaurant -> {
                    Map<String, Object> simpleRestaurant = new HashMap<>();
                    simpleRestaurant.put("restaurantId", restaurant.getRestaurantId());
                    simpleRestaurant.put("name", restaurant.getName());
                    simpleRestaurant.put("address", restaurant.getAddress());
                    simpleRestaurant.put("openingTime", restaurant.getOpeningTime());
                    simpleRestaurant.put("closingTime", restaurant.getClosingTime());
                    simpleRestaurant.put("status", restaurant.getStatus());

                    // Owner ka sirf basic info
                    if(restaurant.getOwner() != null) {
                        Map<String, Object> ownerInfo = new HashMap<>();
                        ownerInfo.put("name", restaurant.getOwner().getName());
                        ownerInfo.put("email", restaurant.getOwner().getEmail());
                        ownerInfo.put("phone", restaurant.getOwner().getPhoneNumber());
                        simpleRestaurant.put("owner", ownerInfo);
                    }

                    return simpleRestaurant;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(simpleRestaurants);
    }

    // ✅ 2. APPROVE RESTAURANT (FR-3.1)
    @PostMapping("/restaurants/{restaurantId}/approve")
    public ResponseEntity<?> approveRestaurant(@PathVariable Long restaurantId) {
        try {
            System.out.println("=== APPROVE RESTAURANT: " + restaurantId + " ===");

            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            restaurant.setStatus(Restaurant.Status.APPROVED);
            restaurantRepository.save(restaurant);

            System.out.println("✅ Restaurant approved: " + restaurant.getName());

            // ✅ JSON response return karo
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Restaurant approved successfully: " + restaurant.getName());
            response.put("restaurantId", restaurantId);
            response.put("restaurantName", restaurant.getName());
            response.put("status", "APPROVED");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());

            // ✅ JSON error response
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    // ✅ DELETE USER API - FIXED WITH CASCADE HANDLING
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            System.out.println("=== DELETE USER: " + userId + " ===");

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Safety check - Admin khudko delete na kar sake
            if (user.getRole() == User.Role.ADMIN) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Cannot delete administrator account");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // ✅ FIRST: Check if user has restaurants (Owner hai)
            if (user.getRole() == User.Role.OWNER) {
                List<Restaurant> userRestaurants = restaurantRepository.findByOwner(user);
                System.out.println("🟡 User has " + userRestaurants.size() + " restaurants");

                if (!userRestaurants.isEmpty()) {
                    // ✅ OPTION 1: Delete all restaurants first (CASCADE)
                    restaurantRepository.deleteAll(userRestaurants);
                    System.out.println("✅ Deleted " + userRestaurants.size() + " restaurants owned by user");

                    // ✅ OPTION 2: Or set restaurants to null owner
                    // for (Restaurant restaurant : userRestaurants) {
                    //     restaurant.setOwner(null);
                    //     restaurantRepository.save(restaurant);
                    // }
                }
            }

            // ✅ NOW delete the user
            userRepository.delete(user);

            System.out.println("✅ User deleted: " + user.getName());

            // ✅ JSON response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User deleted successfully: " + user.getName());
            response.put("deletedUserId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Error deleting user: " + e.getMessage());
            e.printStackTrace(); // ✅ Detailed error print karo

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error deleting user: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    // ✅ 3. REJECT RESTAURANT (FR-3.1)
    @PostMapping("/restaurants/{restaurantId}/reject")
    public ResponseEntity<?> rejectRestaurant(@PathVariable Long restaurantId) {
        try {
            System.out.println("=== REJECT RESTAURANT: " + restaurantId + " ===");

            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            restaurant.setStatus(Restaurant.Status.REJECTED);
            restaurantRepository.save(restaurant);

            System.out.println("✅ Restaurant rejected: " + restaurant.getName());

            // ✅ JSON response return karo
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Restaurant rejected: " + restaurant.getName());
            response.put("restaurantId", restaurantId);
            response.put("restaurantName", restaurant.getName());
            response.put("status", "REJECTED");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());

            // ✅ JSON error response
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ 4. GET ALL USERS (FIXED - No circular reference)
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        System.out.println("=== GET ALL USERS ===");
        List<User> users = userRepository.findAll();
        System.out.println("✅ Found " + users.size() + " users");

        // ✅ SIMPLE RESPONSE WITHOUT CIRCULAR REFERENCES
        List<Map<String, Object>> simpleUsers = users.stream()
                .map(user -> {
                    Map<String, Object> simpleUser = new HashMap<>();
                    simpleUser.put("userId", user.getUserId());
                    simpleUser.put("name", user.getName());
                    simpleUser.put("email", user.getEmail());
                    simpleUser.put("role", user.getRole());
                    simpleUser.put("phoneNumber", user.getPhoneNumber());
                    simpleUser.put("dtype", user.getClass().getSimpleName()); // Shows Owner/Admin/User
                    return simpleUser;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(simpleUsers);
    }

    // ✅ 5. GET ALL RESTAURANTS (FIXED - No circular reference)
    @GetMapping("/restaurants")
    public ResponseEntity<List<Map<String, Object>>> getAllRestaurants() {
        System.out.println("=== GET ALL RESTAURANTS ===");
        List<Restaurant> restaurants = restaurantRepository.findAll();
        System.out.println("✅ Found " + restaurants.size() + " restaurants");

        // ✅ SIMPLE RESPONSE WITHOUT CIRCULAR REFERENCES
        List<Map<String, Object>> simpleRestaurants = restaurants.stream()
                .map(restaurant -> {
                    Map<String, Object> simpleRestaurant = new HashMap<>();
                    simpleRestaurant.put("restaurantId", restaurant.getRestaurantId());
                    simpleRestaurant.put("name", restaurant.getName());
                    simpleRestaurant.put("address", restaurant.getAddress());
                    simpleRestaurant.put("openingTime", restaurant.getOpeningTime());
                    simpleRestaurant.put("closingTime", restaurant.getClosingTime());
                    simpleRestaurant.put("status", restaurant.getStatus());

                    // Owner ka sirf basic info
                    if(restaurant.getOwner() != null) {
                        Map<String, Object> ownerInfo = new HashMap<>();
                        ownerInfo.put("name", restaurant.getOwner().getName());
                        ownerInfo.put("email", restaurant.getOwner().getEmail());
                        simpleRestaurant.put("owner", ownerInfo);
                    }

                    return simpleRestaurant;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(simpleRestaurants);
    }
}