package com.restaurant.backend.controller;

import com.restaurant.backend.entity.Dish;
import com.restaurant.backend.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dishes")
@CrossOrigin(origins = "http://localhost:3001")
public class DishController {

    @Autowired
    private DishRepository dishRepository;

    // ✅ 1. Get all dishes FROM APPROVED RESTAURANTS ONLY
    @GetMapping("/all")
    public ResponseEntity<List<Dish>> getAllDishes() {
        System.out.println("=== GET ALL DISHES FROM APPROVED RESTAURANTS ===");
        List<Dish> dishes = dishRepository.findAllApprovedRestaurantDishes(); // ✅ CHANGED
        System.out.println("✅ SUCCESS: Found " + dishes.size() + " dishes from APPROVED restaurants");
        return ResponseEntity.ok(dishes);
    }

    // ✅ 2. Budget based search - ONLY APPROVED RESTAURANTS
    @GetMapping("/budget/{maxPrice}")
    public ResponseEntity<List<Dish>> getDishesByBudget(@PathVariable Double maxPrice) {
        System.out.println("=== BUDGET SEARCH FOR APPROVED RESTAURANTS: " + maxPrice + " ===");

        List<Dish> dishes = dishRepository.findByPriceLessThanEqualAndApproved(maxPrice); // ✅ CHANGED

        // ✅ COMPLETE DEBUG INFORMATION
        System.out.println("✅ SUCCESS: Found " + dishes.size() + " dishes from APPROVED restaurants under budget " + maxPrice);

        if (!dishes.isEmpty()) {
            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                System.out.println("--- Dish " + (i+1) + " ---");
                System.out.println("ID: " + dish.getDishId());
                System.out.println("Name: " + dish.getName());
                System.out.println("Category: " + dish.getCategory());
                System.out.println("Price: " + dish.getPrice());
                System.out.println("Availability: " + dish.getAvailability());
                System.out.println("Quantity: " + dish.getQuantity());
                System.out.println("Notes: " + dish.getNotes());
                if (dish.getRestaurant() != null) {
                    System.out.println("Restaurant: " + dish.getRestaurant().getName() + " | Status: " + dish.getRestaurant().getStatus());
                } else {
                    System.out.println("Restaurant: NULL");
                }
            }
        } else {
            System.out.println("❌ No dishes found under budget: " + maxPrice + " from APPROVED restaurants");
        }

        return ResponseEntity.ok(dishes);
    }

    // ✅ 3. Category based search - ONLY APPROVED RESTAURANTS
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Dish>> getDishesByCategory(@PathVariable String category) {
        System.out.println("=== CATEGORY SEARCH FOR APPROVED RESTAURANTS: " + category + " ===");
        List<Dish> dishes = dishRepository.findByCategoryAndApproved(category); // ✅ CHANGED
        System.out.println("✅ SUCCESS: Found " + dishes.size() + " dishes in category " + category + " from APPROVED restaurants");
        return ResponseEntity.ok(dishes);
    }

    // ✅ 4. Timing based search - ONLY APPROVED RESTAURANTS
    @GetMapping("/search/category-timing")
    public ResponseEntity<List<Dish>> getDishesByCategoryAndTiming(
            @RequestParam String category,
            @RequestParam String timing) {

        System.out.println("=== CATEGORY + TIMING SEARCH FOR APPROVED RESTAURANTS: " + category + " & " + timing + " ===");

        // ✅ Custom query for category + timing - ONLY APPROVED
        List<Dish> dishes = dishRepository.findByCategoryAndTimingApproved(category, timing); // ✅ CHANGED

        System.out.println("✅ SUCCESS: Found " + dishes.size() + " dishes from APPROVED restaurants");
        return ResponseEntity.ok(dishes);
    }

    // ✅ 5. Available dishes only - ONLY APPROVED RESTAURANTS
    @GetMapping("/available")
    public ResponseEntity<List<Dish>> getAvailableDishes() {
        System.out.println("=== AVAILABLE DISHES FROM APPROVED RESTAURANTS ===");
        // ✅ We need to create this method in repository or use existing with filter
        List<Dish> dishes = dishRepository.findAllApprovedRestaurantDishes(); // ✅ Temporary fix
        dishes = dishes.stream().filter(dish -> "Available".equals(dish.getAvailability())).toList();
        System.out.println("✅ SUCCESS: Found " + dishes.size() + " available dishes from APPROVED restaurants");
        return ResponseEntity.ok(dishes);
    }

    // ✅ 6. Combined search: Budget + Category - ONLY APPROVED RESTAURANTS
    @GetMapping("/search/budget-category")
    public ResponseEntity<List<Dish>> getDishesByBudgetAndCategory(
            @RequestParam Double maxPrice,
            @RequestParam String category) {
        System.out.println("=== COMBINED SEARCH FOR APPROVED RESTAURANTS: Budget " + maxPrice + " & Category " + category + " ===");
        List<Dish> dishes = dishRepository.findByBudgetAndCategoryApproved(maxPrice, category); // ✅ CHANGED
        System.out.println("✅ SUCCESS: Found " + dishes.size() + " dishes from APPROVED restaurants");
        return ResponseEntity.ok(dishes);
    }

    // ✅ 7. Combined search: Budget + Timing - ONLY APPROVED RESTAURANTS
    @GetMapping("/search/budget-timing")
    public ResponseEntity<List<Dish>> getDishesByBudgetAndTiming(
            @RequestParam Double maxPrice,
            @RequestParam String timing) {
        System.out.println("=== COMBINED SEARCH FOR APPROVED RESTAURANTS: Budget " + maxPrice + " & Timing " + timing + " ===");
        List<Dish> dishes = dishRepository.findByBudgetAndTimingApproved(maxPrice, timing); // ✅ CHANGED
        System.out.println("✅ SUCCESS: Found " + dishes.size() + " dishes from APPROVED restaurants");
        return ResponseEntity.ok(dishes);
    }

    // ✅ 8. Popular dishes - ONLY APPROVED RESTAURANTS
    @GetMapping("/popular")
    public ResponseEntity<List<Dish>> getPopularDishes() {
        System.out.println("=== POPULAR DISHES FROM APPROVED RESTAURANTS ===");
        List<Dish> dishes = dishRepository.findPopularDishesApproved(); // ✅ CHANGED
        System.out.println("✅ SUCCESS: Found " + dishes.size() + " popular dishes from APPROVED restaurants");
        return ResponseEntity.ok(dishes);
    }

    // ✅ 9. Search by restaurant name - ONLY APPROVED RESTAURANTS
    @GetMapping("/restaurant/{restaurantName}")
    public ResponseEntity<List<Dish>> getDishesByRestaurantName(@PathVariable String restaurantName) {
        System.out.println("=== RESTAURANT SEARCH IN APPROVED RESTAURANTS: " + restaurantName + " ===");
        List<Dish> dishes = dishRepository.findByRestaurantNameApproved(restaurantName); // ✅ CHANGED
        System.out.println("✅ SUCCESS: Found " + dishes.size() + " dishes from APPROVED restaurant: " + restaurantName);
        return ResponseEntity.ok(dishes);
    }

    // ✅ 10. Get dishes by price range - ONLY APPROVED RESTAURANTS
    @GetMapping("/price-range")
    public ResponseEntity<List<Dish>> getDishesByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        System.out.println("=== PRICE RANGE IN APPROVED RESTAURANTS: " + minPrice + " to " + maxPrice + " ===");
        List<Dish> dishes = dishRepository.findByPriceBetweenAndApproved(minPrice, maxPrice); // ✅ CHANGED
        System.out.println("✅ SUCCESS: Found " + dishes.size() + " dishes in price range from APPROVED restaurants");
        return ResponseEntity.ok(dishes);
    }

    // ✅ YEH TEMPORARY METHOD ADD KARO DEBUG KE LIYE
    @GetMapping("/debug/{id}")
    public ResponseEntity<?> getDishDebug(@PathVariable Long id) {
        Optional<Dish> dishOpt = dishRepository.findById(id);
        if (dishOpt.isPresent()) {
            Dish dish = dishOpt.get();

            // ✅ Debug information
            System.out.println("=== DISH DEBUG INFO ===");
            System.out.println("Dish ID: " + dish.getDishId());
            System.out.println("Name: " + dish.getName());
            System.out.println("Category: " + dish.getCategory());
            System.out.println("Price: " + dish.getPrice());
            System.out.println("Availability: " + dish.getAvailability());
            System.out.println("Restaurant: " + (dish.getRestaurant() != null ? dish.getRestaurant().getName() : "null"));
            System.out.println("Restaurant Status: " + (dish.getRestaurant() != null ? dish.getRestaurant().getStatus() : "null"));

            return ResponseEntity.ok(dish);
        }
        return ResponseEntity.notFound().build();
    }
}