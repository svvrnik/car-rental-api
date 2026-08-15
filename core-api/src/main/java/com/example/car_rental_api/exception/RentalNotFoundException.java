package com.example.car_rental_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class RentalNotFoundException extends RuntimeException{
    public RentalNotFoundException(String message){
        super(message);
    }
}
