package com.example.car_rental_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarRentalApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarRentalApiApplication.class, args);
	}

}
