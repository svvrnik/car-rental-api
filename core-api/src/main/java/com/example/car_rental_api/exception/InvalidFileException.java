package com.example.car_rental_api.exception;

public class InvalidFileException extends RuntimeException{
    public InvalidFileException(String message){
        super(message);
    }
}
