package com.example.car_rental_api.exception;

public class InvalidRentalPeriodException extends RuntimeException{
    public InvalidRentalPeriodException(String message){
        super(message);
    }
}
