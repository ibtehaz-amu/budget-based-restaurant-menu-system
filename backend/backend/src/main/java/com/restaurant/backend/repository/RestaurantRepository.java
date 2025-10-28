package com.restaurant.backend.repository;

import com.restaurant.backend.entity.Restaurant;
import com.restaurant.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // ✅ 1. Status ke hisaab se restaurants dhundega
    List<Restaurant> findByStatus(Restaurant.Status status);

    // ✅ 2. Owner ID ke hisaab se restaurants dhundega
    List<Restaurant> findByOwnerUserId(Long ownerId);

    // ✅ 3. Name search ke liye
    List<Restaurant> findByNameContainingIgnoreCase(String name);

    // ✅ 4. Owner object ke hisaab se restaurants dhundega
    List<Restaurant> findByOwner(User owner);
}