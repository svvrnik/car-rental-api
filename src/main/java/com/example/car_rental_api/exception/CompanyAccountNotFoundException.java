package com.example.car_rental_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class CompanyAccountNotFoundException extends RuntimeException{
    public CompanyAccountNotFoundException(String message){
        super(message);
    }
}
