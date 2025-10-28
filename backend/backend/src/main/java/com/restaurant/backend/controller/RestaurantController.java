package com.restaurant.backend.controller;

import com.restaurant.backend.entity.Restaurant;
import com.restaurant.backend.entity.User;
import com.restaurant.backend.repository.RestaurantRepository;
import com.restaurant.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "http://localhost:3000")
public class RestaurantController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ EXISTING METHODS - UNCHANGED
    // Owner: Register new restaurant
    @PostMapping("/register")
    public ResponseEntity<?> registerRestaurant(@RequestBody Restaurant restaurant) {
        try {
            restaurant.setStatus(Restaurant.Status.PENDING);
            Restaurant savedRestaurant = restaurantRepository.save(restaurant);
            return ResponseEntity.ok(savedRestaurant);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Restaurant registration failed");
        }
    }

    // ✅ NEW: Create restaurant with proper owner assignment
    @PostMapping
    public ResponseEntity<?> createRestaurant(@RequestBody Restaurant restaurant) {
        try {
            System.out.println("=== CREATE RESTAURANT ===");
            System.out.println("Restaurant Name: " + restaurant.getName());
            System.out.println("Restaurant Address: " + restaurant.getAddress());

            // ✅ Set default status to PENDING
            restaurant.setStatus(Restaurant.Status.PENDING);

            // ✅ NOTE: Owner will be set by the frontend or via relationship
            // In your current flow, owner should already be set from the frontend
            // If not, we'll handle it in the service layer

            Restaurant savedRestaurant = restaurantRepository.save(restaurant);

            System.out.println("✅ Restaurant created: " + savedRestaurant.getName());
            System.out.println("✅ Restaurant ID: " + savedRestaurant.getRestaurantId());
            System.out.println("✅ Restaurant Status: " + savedRestaurant.getStatus());

            return ResponseEntity.ok(savedRestaurant);

        } catch (Exception e) {
            System.out.println("❌ Error creating restaurant: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating restaurant: " + e.getMessage());
        }
    }

    // ✅ EXISTING METHODS - UNCHANGED
    // Admin: Get pending restaurants
    @GetMapping("/pending")
    public ResponseEntity<List<Restaurant>> getPendingRestaurants() {
        List<Restaurant> pendingRestaurants = restaurantRepository.findByStatus(Restaurant.Status.PENDING);
        return ResponseEntity.ok(pendingRestaurants);
    }

    // Admin: Approve restaurant
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveRestaurant(@PathVariable Long id) {
        try {
            Optional<Restaurant> restaurantOpt = restaurantRepository.findById(id);
            if (restaurantOpt.isPresent()) {
                Restaurant restaurant = restaurantOpt.get();
                restaurant.setStatus(Restaurant.Status.APPROVED);
                restaurantRepository.save(restaurant);
                return ResponseEntity.ok("Restaurant approved successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Approval failed");
        }
    }

    // Get all approved restaurants
    @GetMapping("/approved")
    public ResponseEntity<List<Restaurant>> getApprovedRestaurants() {
        List<Restaurant> approvedRestaurants = restaurantRepository.findByStatus(Restaurant.Status.APPROVED);
        return ResponseEntity.ok(approvedRestaurants);
    }

    // Get restaurants by owner
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Restaurant>> getRestaurantsByOwner(@PathVariable Long ownerId) {
        List<Restaurant> ownerRestaurants = restaurantRepository.findByOwnerUserId(ownerId);
        return ResponseEntity.ok(ownerRestaurants);
    }

    // ✅ EXISTING METHODS - UNCHANGED
    // 1. Reject restaurant endpoint
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectRestaurant(@PathVariable Long id) {
        try {
            Optional<Restaurant> restaurantOpt = restaurantRepository.findById(id);
            if (restaurantOpt.isPresent()) {
                Restaurant restaurant = restaurantOpt.get();
                restaurant.setStatus(Restaurant.Status.REJECTED);
                restaurantRepository.save(restaurant);
                return ResponseEntity.ok("Restaurant rejected successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Rejection failed");
        }
    }

    // 2. Get restaurant by ID
    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable Long id) {
        Optional<Restaurant> restaurant = restaurantRepository.findById(id);
        return restaurant.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // 3. Search restaurants by name
    @GetMapping("/search")
    public ResponseEntity<List<Restaurant>> searchRestaurants(@RequestParam String name) {
        List<Restaurant> restaurants = restaurantRepository.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok(restaurants);
    }

    // 4. Get all restaurants (for admin)
    @GetMapping("/all")
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        List<Restaurant> allRestaurants = restaurantRepository.findAll();
        return ResponseEntity.ok(allRestaurants);
    }
}