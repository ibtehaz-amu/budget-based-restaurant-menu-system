package com.restaurant.backend.controller;

import com.restaurant.backend.entity.User;
import com.restaurant.backend.entity.Restaurant;
import com.restaurant.backend.entity.Dish;
import com.restaurant.backend.repository.UserRepository;
import com.restaurant.backend.repository.RestaurantRepository;
import com.restaurant.backend.repository.DishRepository;
import com.restaurant.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private JwtUtil jwtUtil; // ✅ JWT UTIL ADD KARO

    // ✅ HELPER METHOD: Get current logged-in owner
    private User getCurrentOwner(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Invalid authorization");
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);

            User owner = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ VERIFY THAT USER IS REALLY AN OWNER
            if (owner.getRole() != User.Role.OWNER) {
                throw new RuntimeException("Access denied: Only owners can perform this action");
            }

            return owner;

        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    // ✅ ADD RESTAURANT - FIXED
    @PostMapping("/restaurants")
    public ResponseEntity<?> addRestaurant(@RequestBody Restaurant restaurant, HttpServletRequest request) {
        try {
            System.out.println("=== ADD RESTAURANT ===");

            // ✅ CURRENT LOGGED-IN OWNER KO USE KARO
            User owner = getCurrentOwner(request);

            restaurant.setOwner(owner);
            restaurant.setStatus(Restaurant.Status.PENDING);

            Restaurant savedRestaurant = restaurantRepository.save(restaurant);

            System.out.println("✅ SUCCESS: Restaurant added - " + savedRestaurant.getName());

            // ✅ CONSISTENT JSON RESPONSE
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Restaurant added successfully");
            response.put("restaurant", savedRestaurant);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ GET MY RESTAURANTS - FIXED
    @GetMapping("/restaurants")
    public ResponseEntity<?> getMyRestaurants(HttpServletRequest request) {
        try {
            System.out.println("=== GET MY RESTAURANTS ===");

            // ✅ CURRENT LOGGED-IN OWNER KO USE KARO
            User owner = getCurrentOwner(request);

            List<Restaurant> restaurants = restaurantRepository.findByOwner(owner);
            System.out.println("✅ SUCCESS: Found " + restaurants.size() + " restaurants");

            return ResponseEntity.ok(restaurants);

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ ADD DISH TO RESTAURANT - FIXED WITH OWNER VERIFICATION
    @PostMapping("/restaurants/{restaurantId}/dishes")
    public ResponseEntity<?> addDishToRestaurant(
            @PathVariable Long restaurantId,
            @RequestBody Dish dish,
            HttpServletRequest request) {
        try {
            System.out.println("=== ADD DISH TO RESTAURANT: " + restaurantId + " ===");

            // ✅ CURRENT OWNER KO VERIFY KARO
            User owner = getCurrentOwner(request);

            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            // ✅ SECURITY CHECK: Restaurant current owner ki hai ya nahi
            if (!restaurant.getOwner().getUserId().equals(owner.getUserId())) {
                throw new RuntimeException("Access denied: You can only add dishes to your own restaurants");
            }

            dish.setRestaurant(restaurant);
            Dish savedDish = dishRepository.save(dish);

            System.out.println("✅ SUCCESS: Dish added - " + savedDish.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Dish added successfully");
            response.put("dish", savedDish);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ GET RESTAURANT'S DISHES - FIXED WITH OWNER VERIFICATION
    @GetMapping("/restaurants/{restaurantId}/dishes")
    public ResponseEntity<?> getRestaurantDishes(@PathVariable Long restaurantId, HttpServletRequest request) {
        try {
            System.out.println("=== GET DISHES FOR RESTAURANT: " + restaurantId + " ===");

            // ✅ CURRENT OWNER KO VERIFY KARO
            User owner = getCurrentOwner(request);

            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            // ✅ SECURITY CHECK: Restaurant current owner ki hai ya nahi
            if (!restaurant.getOwner().getUserId().equals(owner.getUserId())) {
                throw new RuntimeException("Access denied: You can only view dishes from your own restaurants");
            }

            List<Dish> dishes = dishRepository.findByRestaurant(restaurant);
            System.out.println("✅ SUCCESS: Found " + dishes.size() + " dishes");
            return ResponseEntity.ok(dishes);

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ UPDATE DISH - FIXED WITH OWNER VERIFICATION
    @PutMapping("/dishes/{dishId}")
    public ResponseEntity<?> updateDish(
            @PathVariable Long dishId,
            @RequestBody Dish dishDetails,
            HttpServletRequest request) {
        try {
            System.out.println("=== UPDATE DISH: " + dishId + " ===");

            // ✅ CURRENT OWNER KO VERIFY KARO
            User owner = getCurrentOwner(request);

            Dish dish = dishRepository.findById(dishId)
                    .orElseThrow(() -> new RuntimeException("Dish not found"));

            // ✅ SECURITY CHECK: Dish current owner ki restaurant ka hai ya nahi
            if (!dish.getRestaurant().getOwner().getUserId().equals(owner.getUserId())) {
                throw new RuntimeException("Access denied: You can only update dishes from your own restaurants");
            }

            // Update fields
            if(dishDetails.getName() != null) dish.setName(dishDetails.getName());
            if(dishDetails.getCategory() != null) dish.setCategory(dishDetails.getCategory());
            if(dishDetails.getPrice() != null) dish.setPrice(dishDetails.getPrice());
            if(dishDetails.getAvailability() != null) dish.setAvailability(dishDetails.getAvailability());
            if(dishDetails.getNotes() != null) dish.setNotes(dishDetails.getNotes());

            Dish updatedDish = dishRepository.save(dish);
            System.out.println("✅ SUCCESS: Dish updated - " + updatedDish.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Dish updated successfully");
            response.put("dish", updatedDish);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ DELETE DISH - FIXED WITH OWNER VERIFICATION
    @DeleteMapping("/dishes/{dishId}")
    public ResponseEntity<?> deleteDish(@PathVariable Long dishId, HttpServletRequest request) {
        try {
            System.out.println("=== DELETE DISH: " + dishId + " ===");

            // ✅ CURRENT OWNER KO VERIFY KARO
            User owner = getCurrentOwner(request);

            Dish dish = dishRepository.findById(dishId)
                    .orElseThrow(() -> new RuntimeException("Dish not found"));

            // ✅ SECURITY CHECK: Dish current owner ki restaurant ka hai ya nahi
            if (!dish.getRestaurant().getOwner().getUserId().equals(owner.getUserId())) {
                throw new RuntimeException("Access denied: You can only delete dishes from your own restaurants");
            }

            dishRepository.delete(dish);
            System.out.println("✅ SUCCESS: Dish deleted - ID: " + dishId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Dish deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}