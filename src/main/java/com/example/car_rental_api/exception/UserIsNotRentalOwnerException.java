package com.example.car_rental_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class UserIsNotRentalOwnerException extends RuntimeException{
    public UserIsNotRentalOwnerException(String message){
        super(message);
    }
}
