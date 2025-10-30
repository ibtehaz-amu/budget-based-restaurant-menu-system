package com.restaurant.backend.repository;

import com.restaurant.backend.entity.Restaurant;
import com.restaurant.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Status ke hisaab se restaurants dhundega
    List<Restaurant> findByStatus(Restaurant.Status status);

    //  Owner ID ke hisaab se restaurants dhundega
    List<Restaurant> findByOwnerUserId(Long ownerId);

    // Name search ke liye
    List<Restaurant> findByNameContainingIgnoreCase(String name);

    // Owner object ke hisaab se restaurants dhundega
    List<Restaurant> findByOwner(User owner);
}