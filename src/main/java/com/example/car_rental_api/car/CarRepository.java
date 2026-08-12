package com.example.car_rental_api.car;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {
    boolean existsByLicensePlate(String licensePlate);

    List<Car> findByStatus(CarStatus status);

    List<Car> findByOwnerEmail(String email);
}
