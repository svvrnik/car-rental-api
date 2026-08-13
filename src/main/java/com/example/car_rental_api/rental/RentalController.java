package com.example.car_rental_api.rental;

import com.example.car_rental_api.rental.dto.RentalRequestDto;
import com.example.car_rental_api.rental.dto.RentalResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {
    private final RentalService rentalService;


    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping("/rent")
    public ResponseEntity<RentalResponseDto> rentCar(@Valid @RequestBody RentalRequestDto rentalRequestDto){
        RentalResponseDto rentedCar = rentalService.rentCar(rentalRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(rentedCar);
    }

    @PatchMapping("/return/{rentalId}")
    public ResponseEntity<RentalResponseDto> returnCar(@PathVariable Long rentalId){
        RentalResponseDto returnedCar = rentalService.returnCar(rentalId);
        return ResponseEntity.status(HttpStatus.OK).body(returnedCar);
    }

    @GetMapping("/my-rentals")
    public ResponseEntity<List<RentalResponseDto>> getUserRentals(){
        List<RentalResponseDto> userRentals = rentalService.getUserRentals();
        return ResponseEntity.status(HttpStatus.OK).body(userRentals);
    }
}
