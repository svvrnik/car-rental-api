package com.example.car_rental_api.car;

import com.example.car_rental_api.car.dto.CarRequestDto;
import com.example.car_rental_api.car.dto.CarResponseDto;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CarService {
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    public CarService(CarRepository carRepository, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    public CarResponseDto addCar(CarRequestDto carRequestDto, Long ownerId){
        //TODO: add CarResponseDto class and return it
        //TODO: find user by e-mail, not by id (will be added while configuring Spring Security)
        User foundUser = userRepository.findById(ownerId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
}
