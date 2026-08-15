package com.example.car_rental_api.car;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {
    boolean existsByLicensePlate(String licensePlate);

    Page<Car> findByStatus(CarStatus status, Pageable pageable);

    Page<Car> findByOwnerEmail(String email, Pageable pageable);
}
