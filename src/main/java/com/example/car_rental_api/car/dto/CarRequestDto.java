package com.example.car_rental_api.car.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class CarRequestDto {
    @NotBlank(message = "Brand can't be empty")
    private String brand;
    @NotBlank(message = "Model can't be empty")
    private String model;
    @NotBlank(message = "License plate can't be empty")
    private String licensePlate;
    private BigDecimal pricePerDay;

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
}
