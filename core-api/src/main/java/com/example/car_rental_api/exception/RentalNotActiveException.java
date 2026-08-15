package com.example.car_rental_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class RentalNotActiveException extends RuntimeException{
    public RentalNotActiveException(String message){
        super(message);
    }
}
