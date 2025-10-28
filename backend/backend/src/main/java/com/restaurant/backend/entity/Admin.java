package com.restaurant.backend.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    // ✅ DEFAULT CONSTRUCTOR
    public Admin() {
        super();
        this.setRole(User.Role.ADMIN);
    }

    // ✅ ADMIN CONSTRUCTOR
    public Admin(String name, String email, String password) {
        super(name, email, password, User.Role.ADMIN);
    }

    // ❌ NO EXTRA FIELDS NEEDED - Simple is better!
    // ❌ NO GETTERS/SETTERS NEEDED
}