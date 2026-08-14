package com.example.car_rental_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class CarNotAvailableException extends RuntimeException{
    public CarNotAvailableException(String message){
        super(message);
    }
}
