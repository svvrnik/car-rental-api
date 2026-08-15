package com.example.car_rental_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class DuplicateLicensePlateException extends RuntimeException{
    public DuplicateLicensePlateException(String message){
        super(message);
    }
}
