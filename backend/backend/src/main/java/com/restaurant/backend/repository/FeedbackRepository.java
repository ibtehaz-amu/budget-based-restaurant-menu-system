package com.restaurant.backend.repository;

import com.restaurant.backend.entity.Feedback;
import com.restaurant.backend.entity.Dish;
import com.restaurant.backend.entity.Restaurant;
import com.restaurant.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // ✅ Find all feedback for a specific dish
    List<Feedback> findByDish(Dish dish);

    // ✅ Find all feedback for a restaurant (via dish)
    @Query("SELECT f FROM Feedback f WHERE f.dish.restaurant = :restaurant")
    List<Feedback> findByRestaurant(@Param("restaurant") Restaurant restaurant);

    // ✅ Find all feedback by a student
    List<Feedback> findByStudent(User student);

    // ✅ Check if student already gave feedback for a dish
    boolean existsByStudentAndDish(User student, Dish dish);

    // ✅ Get average rating for a dish
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.dish = :dish")
    Optional<Double> findAverageRatingByDish(@Param("dish") Dish dish);

    // ✅ Get average rating for a restaurant
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.dish.restaurant = :restaurant")
    Optional<Double> findAverageRatingByRestaurant(@Param("restaurant") Restaurant restaurant);

    // ✅ Get feedback count for a restaurant
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.dish.restaurant = :restaurant")
    Long countByRestaurant(@Param("restaurant") Restaurant restaurant);

    // ✅ Find all feedback with restaurant eager loading
    @Query("SELECT f FROM Feedback f JOIN FETCH f.dish d JOIN FETCH d.restaurant WHERE f.student = :student")
    List<Feedback> findByStudentWithRestaurant(@Param("student") User student);
}