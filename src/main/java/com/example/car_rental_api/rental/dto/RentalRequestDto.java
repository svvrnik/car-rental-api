package com.example.car_rental_api.rental.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class RentalRequestDto {
    @NotNull(message = "Car id can't be empty")
    private Long carId;
    @NotNull(message = "Start date can't be empty")
    @FutureOrPresent(message = "Start date must be in the future")
    private LocalDateTime startDate;
    @NotNull(message = "End date can't be empty")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
