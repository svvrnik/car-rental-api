package com.example.car_rental_api.car.mapper;

import com.example.car_rental_api.car.Car;
import com.example.car_rental_api.car.dto.CarRequestDto;
import com.example.car_rental_api.car.dto.CarResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CarMapper {
    CarResponseDto carToCarResponseDto(Car car);
    Car carRequestDtoToCar(CarRequestDto carRequestDto);

}
