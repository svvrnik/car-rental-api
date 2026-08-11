package com.example.car_rental_api.car;


import com.example.car_rental_api.car.dto.CarRequestDto;
import com.example.car_rental_api.car.dto.CarResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cars")
public class CarController {
    private final CarService carService;


    public CarController(CarService carService) {
        this.carService = carService;
    }

    @PostMapping("/add")
    public ResponseEntity<CarResponseDto> addCar(@RequestHeader("X-User-Id") Long ownerId, @RequestBody @Valid CarRequestDto carRequestDto){
        CarResponseDto carResponseDto = carService.addCar(carRequestDto,ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(carResponseDto);
    }
}
