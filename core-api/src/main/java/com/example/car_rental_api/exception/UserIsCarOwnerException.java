package com.example.car_rental_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserIsCarOwnerException extends RuntimeException{
    public UserIsCarOwnerException(String message){super(message);}
}
