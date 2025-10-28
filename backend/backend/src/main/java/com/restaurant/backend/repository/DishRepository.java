package com.restaurant.backend.repository;

import com.restaurant.backend.entity.Dish;
import com.restaurant.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Long> {

    // ✅ EXISTING METHODS - UNCHANGED
    List<Dish> findByRestaurant(Restaurant restaurant);

    @Query("SELECT d FROM Dish d WHERE d.category = :category AND d.notes LIKE %:timing%")
    List<Dish> findByCategoryAndTiming(@Param("category") String category, @Param("timing") String timing);

    // ✅ NEW: ONLY APPROVED RESTAURANTS KE DISHES
    @Query("SELECT d FROM Dish d WHERE d.restaurant.status = 'APPROVED'")
    List<Dish> findAllApprovedRestaurantDishes();

    // ✅ Budget search - ONLY APPROVED RESTAURANTS
    @Query("SELECT d FROM Dish d WHERE d.price <= :maxPrice AND d.restaurant.status = 'APPROVED'")
    List<Dish> findByPriceLessThanEqualAndApproved(@Param("maxPrice") Double maxPrice);

    // ✅ Category search - ONLY APPROVED RESTAURANTS
    @Query("SELECT d FROM Dish d WHERE d.category = :category AND d.restaurant.status = 'APPROVED'")
    List<Dish> findByCategoryAndApproved(@Param("category") String category);

    // ✅ Timing based search - ONLY APPROVED RESTAURANTS
    @Query("SELECT d FROM Dish d WHERE d.notes LIKE %:timing% AND d.restaurant.status = 'APPROVED'")
    List<Dish> findByTimingAndApproved(@Param("timing") String timing);

    // ✅ Combined: Budget + Category - ONLY APPROVED RESTAURANTS
    @Query("SELECT d FROM Dish d WHERE d.price <= :maxPrice AND d.category = :category AND d.restaurant.status = 'APPROVED'")
    List<Dish> findByBudgetAndCategoryApproved(@Param("maxPrice") Double maxPrice, @Param("category") String category);

    // ✅ Combined: Budget + Timing - ONLY APPROVED RESTAURANTS
    @Query("SELECT d FROM Dish d WHERE d.price <= :maxPrice AND d.notes LIKE %:timing% AND d.restaurant.status = 'APPROVED'")
    List<Dish> findByBudgetAndTimingApproved(@Param("maxPrice") Double maxPrice, @Param("timing") String timing);

    // ✅ Combined: Category + Timing - ONLY APPROVED RESTAURANTS
    @Query("SELECT d FROM Dish d WHERE d.category = :category AND d.notes LIKE %:timing% AND d.restaurant.status = 'APPROVED'")
    List<Dish> findByCategoryAndTimingApproved(@Param("category") String category, @Param("timing") String timing);

    // ✅ Price range search - ONLY APPROVED RESTAURANTS
    @Query("SELECT d FROM Dish d WHERE d.price BETWEEN :minPrice AND :maxPrice AND d.restaurant.status = 'APPROVED'")
    List<Dish> findByPriceBetweenAndApproved(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    // ✅ Popular dishes - ONLY APPROVED RESTAURANTS
    @Query("SELECT d FROM Dish d WHERE d.availability = 'Available' AND d.price < 150 AND d.restaurant.status = 'APPROVED' ORDER BY d.price ASC")
    List<Dish> findPopularDishesApproved();

    // ✅ Restaurant name search - ONLY APPROVED RESTAURANTS
    @Query("SELECT d FROM Dish d WHERE (d.name LIKE %:restaurantName% OR d.notes LIKE %:restaurantName%) AND d.restaurant.status = 'APPROVED'")
    List<Dish> findByRestaurantNameApproved(@Param("restaurantName") String restaurantName);

    // ✅ KEEP EXISTING METHODS FOR OWNER/ADMIN USE
    List<Dish> findByPriceLessThanEqual(Double maxPrice);
    List<Dish> findByCategoryIgnoreCase(String category);
    List<Dish> findByAvailability(String availability);

    @Query("SELECT d FROM Dish d WHERE d.notes LIKE %:timing%")
    List<Dish> findByTiming(@Param("timing") String timing);

    List<Dish> findByPriceLessThanEqualAndCategoryIgnoreCase(Double maxPrice, String category);

    @Query("SELECT d FROM Dish d WHERE d.price <= :maxPrice AND d.notes LIKE %:timing%")
    List<Dish> findByBudgetAndTiming(@Param("maxPrice") Double maxPrice, @Param("timing") String timing);

    List<Dish> findByPriceBetween(Double minPrice, Double maxPrice);

    @Query("SELECT d FROM Dish d WHERE d.availability = 'Available' AND d.price < 150 ORDER BY d.price ASC")
    List<Dish> findPopularDishes();

    @Query("SELECT d FROM Dish d WHERE d.name LIKE %:restaurantName% OR d.notes LIKE %:restaurantName%")
    List<Dish> findByRestaurantName(@Param("restaurantName") String restaurantName);
}