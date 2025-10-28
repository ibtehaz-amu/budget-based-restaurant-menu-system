package com.restaurant.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @Column(nullable = false)
    private Integer rating; // 1 to 5 stars

    @Column(length = 500)
    private String comment;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private User student;

    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    @JsonIgnore
    private Dish dish;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // ✅ Constructors
    public Feedback() {
        this.createdAt = LocalDateTime.now();
    }

    public Feedback(Integer rating, String comment, User student, Dish dish) {
        this.rating = rating;
        this.comment = comment;
        this.student = student;
        this.dish = dish;
        this.createdAt = LocalDateTime.now();
    }

    // ✅ Getters and Setters
    public Long getFeedbackId() { return feedbackId; }
    public void setFeedbackId(Long feedbackId) { this.feedbackId = feedbackId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Dish getDish() { return dish; }
    public void setDish(Dish dish) { this.dish = dish; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ✅ Utility method for JSON response
    public String getStudentName() {
        return student != null ? student.getName() : "Unknown";
    }

    public String getDishName() {
        return dish != null ? dish.getName() : "Unknown Dish";
    }

    public String getRestaurantName() {
        return dish != null && dish.getRestaurant() != null ?
                dish.getRestaurant().getName() : "Unknown Restaurant";
    }
}