package com.example.car_rental_api.rental;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    @Query("SELECT CASE WHEN COUNT(r)>0 THEN true ELSE false END FROM Rental r WHERE r.car.id = :carId AND r.status = 'ACTIVE' AND r.startDate <= :endDate AND r.endDate >= :startDate")
    boolean existsOverLappingRental(@Param("carId") Long carId, @Param("startDate")LocalDateTime startDate, @Param("endDate")LocalDateTime endDate);

    @EntityGraph(attributePaths = {"car", "car.images"})
    Page<Rental> findByUserEmail(String email, Pageable pageable);
}