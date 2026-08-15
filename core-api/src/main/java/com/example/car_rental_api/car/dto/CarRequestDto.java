package com.example.car_rental_api.car.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CarRequestDto {
    @NotBlank(message = "Brand can't be empty")
    private String brand;
    @NotBlank(message = "Model can't be empty")
    private String model;
    @NotBlank(message = "License plate can't be empty")
    private String licensePlate;
    @NotNull(message = "Price per day can't be empty")
    @Positive
    private BigDecimal pricePerDay;
    @NotBlank(message = "Description can't be empty")
    private String description;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
