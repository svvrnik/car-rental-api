package com.example.car_rental_api.car;

import com.example.car_rental_api.car.dto.CarRequestDto;
import com.example.car_rental_api.car.dto.CarResponseDto;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarService {
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    public CarService(CarRepository carRepository, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    public CarResponseDto addCar(CarRequestDto carRequestDto){
        //TODO: find user by e-mail, not by id (will be added while configuring Spring Security)
        //TODO: verification by JWT token
        String emailOfLoggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User foundUser = userRepository.findByEmail(emailOfLoggedInUser).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Car createdCar = new Car();

        if(carRepository.existsByLicensePlate(carRequestDto.getLicensePlate())){
            throw new IllegalArgumentException("Car with this license plate exists.");
        }

        createdCar.setBrand(carRequestDto.getBrand());
        createdCar.setModel(carRequestDto.getModel());
        createdCar.setLicensePlate(carRequestDto.getLicensePlate());
        createdCar.setPricePerDay(carRequestDto.getPricePerDay());
        foundUser.addCar(createdCar);
        createdCar.setStatus(CarStatus.PENDING);

        Car savedCar = carRepository.save(createdCar);

        CarResponseDto responseDto = new CarResponseDto();
        responseDto.setId(savedCar.getId());
        responseDto.setBrand(savedCar.getBrand());
        responseDto.setModel(savedCar.getModel());
        responseDto.setLicensePlate(savedCar.getLicensePlate());
        responseDto.setPricePerDay(savedCar.getPricePerDay());
        responseDto.setStatus(savedCar.getStatus());

        return responseDto;
    }

    public List<CarResponseDto> getAvailableCars(){
        return carRepository.findByStatus(CarStatus.AVAILABLE).stream()
                .map(car -> {
                    CarResponseDto responseDto = new CarResponseDto();

                    responseDto.setId(car.getId());
                    responseDto.setBrand(car.getBrand());
                    responseDto.setLicensePlate(car.getLicensePlate());
                    responseDto.setModel(car.getModel());
                    responseDto.setPricePerDay(car.getPricePerDay());
                    responseDto.setStatus(car.getStatus());

                    return responseDto;
                }).toList();
    }
}
