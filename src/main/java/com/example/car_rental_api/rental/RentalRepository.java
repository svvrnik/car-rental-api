package com.example.car_rental_api.rental;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    @Query("SELECT CASE WHEN COUNT(r)>0 THEN true ELSE false END FROM Rental r WHERE r.car.id = :carId AND r.status = 'ACTIVE' AND r.startDate <= :endDate AND r.endDate >= :startDate")
    boolean existsOverLappingRental(@Param("carId") Long carId, @Param("startDate")LocalDateTime startDate, @Param("endDate")LocalDateTime endDate);
    List<Rental> findByUserEmail(String email);
}
