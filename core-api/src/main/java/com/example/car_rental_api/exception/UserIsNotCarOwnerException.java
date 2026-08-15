package com.example.car_rental_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class UserIsNotCarOwnerException extends RuntimeException{
    public UserIsNotCarOwnerException(String message){
        super(message);
    }
}
