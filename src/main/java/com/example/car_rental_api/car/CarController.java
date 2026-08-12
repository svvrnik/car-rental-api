package com.example.car_rental_api.car;


import com.example.car_rental_api.car.dto.CarRequestDto;
import com.example.car_rental_api.car.dto.CarResponseDto;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {
    private final CarService carService;


    public CarController(CarService carService) {
        this.carService = carService;
    }

    @PostMapping("/add")
    public ResponseEntity<CarResponseDto> addCar(@RequestBody @Valid CarRequestDto carRequestDto){
        CarResponseDto carResponseDto = carService.addCar(carRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(carResponseDto);
    }

    @GetMapping("/my-cars")
    public ResponseEntity<List<CarResponseDto>> getUserCars(){
        List<CarResponseDto> userCars = carService.getUserCars();
        return ResponseEntity.status(HttpStatus.OK).body(userCars);
    }

    @GetMapping("/available")
    public ResponseEntity<List<CarResponseDto>> getAvailableCars(){
        List<CarResponseDto> availableCars = carService.getAvailableCars();
        return ResponseEntity.status(HttpStatus.OK).body(availableCars);
    }

    @PatchMapping("/approve/{carId}")
    public ResponseEntity<CarResponseDto> approveCar(@PathVariable Long carId){
        CarResponseDto approvedCar = carService.approveCar(carId);
        return ResponseEntity.status(HttpStatus.OK).body(approvedCar);
    }

    @PatchMapping("/reject/{carId}")
    public ResponseEntity<CarResponseDto> rejectCar(@PathVariable Long carId){
        CarResponseDto rejectedCar = carService.rejectCar(carId);
        return ResponseEntity.status(HttpStatus.OK).body(rejectedCar);
    }

    @PatchMapping("/withdraw/{carId}")
    public ResponseEntity<CarResponseDto> withdrawCar(@PathVariable Long carId){
        CarResponseDto withdrewCar = carService.withdrawCar(carId);
        return ResponseEntity.status(HttpStatus.OK).body(withdrewCar);
    }
}
