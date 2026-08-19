package com.example.car_rental_api.rental;

import com.example.car_rental_api.rental.dto.RentalRequestDto;
import com.example.car_rental_api.rental.dto.RentalResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@Validated
public class RentalController {
    private final RentalService rentalService;


    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping("/rent")
    public ResponseEntity<RentalResponseDto> rentCar(@Valid @RequestBody RentalRequestDto rentalRequestDto){
        RentalResponseDto rentedCar = rentalService.rentCar(rentalRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(rentedCar);
    }

    @PatchMapping("/return/{rentalId}")
    public ResponseEntity<RentalResponseDto> returnCar(@PathVariable Long rentalId){
        RentalResponseDto returnedCar = rentalService.returnCar(rentalId);
        return ResponseEntity.status(HttpStatus.OK).body(returnedCar);
    }

    @GetMapping("/my-rentals")
    public ResponseEntity<Page<RentalResponseDto>> getUserRentals(@RequestParam(defaultValue = "0") @Min(value = 0, message = "Page index must not be less than 0") int page, @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must not be less than 1") @Max(value = 100, message = "Max page size is 100") int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<RentalResponseDto> userRentals = rentalService.getUserRentals(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(userRentals);
    }
}
