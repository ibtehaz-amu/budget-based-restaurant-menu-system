package com.restaurant.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
@SpringBootApplication
@ComponentScan(basePackages = "com.restaurant.backend")
public class RestaurantBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestaurantBackendApplication.class, args);
	}

}
