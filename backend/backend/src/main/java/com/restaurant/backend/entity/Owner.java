package com.restaurant.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("OWNER") // ✅ Important: Single Table Inheritance
public class Owner extends User {

    // ✅ OWNER-SPECIFIC RELATIONSHIPS
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Restaurant> restaurantList = new ArrayList<>();

    // ✅ DEFAULT CONSTRUCTOR (JPA Requirement)
    public Owner() {
        super(); // Parent constructor call
        this.setRole(User.Role.OWNER); // ✅ Automatically set role to OWNER
    }

    // ✅ CONSTRUCTOR FOR OWNER REGISTRATION (With phone - as per your User entity)
    public Owner(String name, String email, String password, String phoneNumber) {
        super(name, email, password, User.Role.OWNER, phoneNumber); // ✅ Phone required for owners
    }

    // ✅ BUSINESS METHODS (SRS ke hisaab se)

    // Restaurant add karna (FR-2.1)
    public void addRestaurant(Restaurant restaurant) {
        restaurant.setOwner(this); // ✅ Set owner reference
        this.restaurantList.add(restaurant);
    }

    // Restaurant remove karna
    public void removeRestaurant(Restaurant restaurant) {
        this.restaurantList.remove(restaurant);
        restaurant.setOwner(null);
    }

    // ✅ GETTERS & SETTERS
    public List<Restaurant> getRestaurantList() {
        return restaurantList;
    }

    public void setRestaurantList(List<Restaurant> restaurantList) {
        this.restaurantList = restaurantList;
    }

    // ✅ toString FOR DEBUGGING
    @Override
    public String toString() {
        return "Owner{" +
                "userId=" + getUserId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phoneNumber='" + getPhoneNumber() + '\'' +
                ", restaurantCount=" + restaurantList.size() +
                '}';
    }
}