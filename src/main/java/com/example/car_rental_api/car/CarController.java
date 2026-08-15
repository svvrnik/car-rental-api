package com.example.car_rental_api.car;


import com.example.car_rental_api.car.dto.CarRequestDto;
import com.example.car_rental_api.car.dto.CarResponseDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {
    private final CarService carService;


    public CarController(CarService carService) {
        this.carService = carService;
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CarResponseDto> addCar(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) @RequestPart("car") @Valid CarRequestDto carRequestDto, @RequestPart(value = "files", required = false) MultipartFile[] files){
        CarResponseDto carResponseDto = carService.addCar(carRequestDto, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(carResponseDto);
    }

    @GetMapping("/my-cars")
    public ResponseEntity<Page<CarResponseDto>> getUserCars(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<CarResponseDto> userCars = carService.getUserCars(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(userCars);
    }

    @GetMapping("/available")
    public ResponseEntity<Page<CarResponseDto>> getAvailableCars(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page,size);
        Page<CarResponseDto> availableCars = carService.getAvailableCars(pageable);
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
