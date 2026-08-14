package com.example.car_rental_api.car;

import com.example.car_rental_api.car.dto.CarRequestDto;
import com.example.car_rental_api.car.dto.CarResponseDto;
import com.example.car_rental_api.car.mapper.CarMapper;
import com.example.car_rental_api.exception.*;
import com.example.car_rental_api.user.Role;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarService {
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final CarMapper carMapper;
    private final String companyEmail;
    public CarService(CarRepository carRepository, UserRepository userRepository, CarMapper carMapper, @Value("${app.company.email}") String companyEmail) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.carMapper = carMapper;
        this.companyEmail = companyEmail;
    }

    public CarResponseDto addCar(CarRequestDto carRequestDto){
        String emailOfLoggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User foundUser = userRepository.findByEmail(emailOfLoggedInUser).orElseThrow(() -> new UserNotFoundException("User not found"));

        if(carRepository.existsByLicensePlate(carRequestDto.getLicensePlate())){
            throw new DuplicateLicensePlateException("Car with this license plate exists.");
        }
        Car createdCar = carMapper.carRequestDtoToCar(carRequestDto);

        if(foundUser.getRole() == Role.ADMIN){
            createdCar.setStatus(CarStatus.AVAILABLE);
            User companyAccount = userRepository.findByEmail(companyEmail).orElseThrow(() -> new CompanyAccountNotFoundException("Company account is missing in the database")); //only for educational purpose - when renting car, everything goes to company account (virtual wallet)
            companyAccount.addCar(createdCar);
        }else{
            createdCar.setStatus(CarStatus.PENDING);
            foundUser.addCar(createdCar);
        }

        Car savedCar = carRepository.save(createdCar);

        return carMapper.carToCarResponseDto(savedCar);
    }

    public List<CarResponseDto> getAvailableCars(){
        return carRepository.findByStatus(CarStatus.AVAILABLE).stream()
                .map(carMapper::carToCarResponseDto).toList();
    }

    public List<CarResponseDto> getUserCars(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return carRepository.findByOwnerEmail(email).stream().map(carMapper::carToCarResponseDto).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CarResponseDto approveCar(Long carId){
        Car foundCar = carRepository.findById(carId).orElseThrow(() -> new CarNotFoundException("Car not found"));
        foundCar.setStatus(CarStatus.AVAILABLE);
        Car savedCar = carRepository.save(foundCar);
        return carMapper.carToCarResponseDto(savedCar);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CarResponseDto rejectCar(Long carId){
        Car foundCar = carRepository.findById(carId).orElseThrow(() -> new CarNotFoundException("Car not found"));
        foundCar.setStatus(CarStatus.REJECTED);
        Car savedCar = carRepository.save(foundCar);
        return carMapper.carToCarResponseDto(savedCar);
    }

    public CarResponseDto withdrawCar(Long carId){
        String emailOfLoggedUser = SecurityContextHolder.getContext().getAuthentication().getName();

        Car foundCar = carRepository.findById(carId).orElseThrow(() -> new CarNotFoundException("Car not found"));

        if(!foundCar.getOwner().getEmail().equals(emailOfLoggedUser)){
            throw new UserIsNotCarOwnerException("You are not owner of this car");
        }
        if(foundCar.getStatus() == CarStatus.RENTED){
            throw new CarCurrentlyRentedException("Car is actually rented");
        }
        foundCar.setStatus(CarStatus.UNAVAILABLE);
        Car savedCar = carRepository.save(foundCar);
        return carMapper.carToCarResponseDto(savedCar);
    }
}
