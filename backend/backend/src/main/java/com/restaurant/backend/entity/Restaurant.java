package com.restaurant.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    @Column(nullable = false)
    private String name;

    private String address;
    private String openingTime;
    private String closingTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    // ✅ YEH LINE UPDATE KARO - @JsonIgnore ADD KARO
    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnore  // ✅ YEH ADD KARO - Circular reference fix
    private User owner;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Dish> dishes;

    // Constructors
    public Restaurant() {}

    public Restaurant(String name, String address, String openingTime, String closingTime, Status status, User owner) {
        this.name = name;
        this.address = address;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.status = status;
        this.owner = owner;
    }

    // Getters and Setters
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOpeningTime() { return openingTime; }
    public void setOpeningTime(String openingTime) { this.openingTime = openingTime; }

    public String getClosingTime() { return closingTime; }
    public void setClosingTime(String closingTime) { this.closingTime = closingTime; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public List<Dish> getDishes() { return dishes; }
    public void setDishes(List<Dish> dishes) { this.dishes = dishes; }

    @Override
    public String toString() {
        return "Restaurant{id=" + restaurantId + ", name='" + name + "', status=" + status + "}";
    }
}