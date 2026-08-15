package com.example.car_rental_api.rental.dto;

import com.example.car_rental_api.car.dto.CarResponseDto;
import com.example.car_rental_api.rental.RentalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RentalResponseDto {
    private Long id;
    private CarResponseDto car;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalCost;
    private RentalStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CarResponseDto getCar() {
        return car;
    }

    public void setCar(CarResponseDto car) {
        this.car = car;
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

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public RentalStatus getStatus() {
        return status;
    }

    public void setStatus(RentalStatus status) {
        this.status = status;
    }
}
