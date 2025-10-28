package com.restaurant.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "dishes")// ✅ Table name

public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dish_id")
    private Long dishId;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
   // @JsonIgnore
    private Restaurant restaurant;

    @Column(name = "name")
    private String name;

    @Column(name = "category")
    private String category;

    @Column(name = "price")
    private Double price;

    @Column(name = "quantity")
    private String quantity;

    @Column(name = "availability")
    private String availability;

    @Column(name = "notes")
    private String notes;

    // Constructors
    public Dish() {}

    public Dish(String name, String category, Double price, String availability, Restaurant restaurant) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.availability = availability;
        this.restaurant = restaurant;
    }

    // Getters and Setters
    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }

    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}