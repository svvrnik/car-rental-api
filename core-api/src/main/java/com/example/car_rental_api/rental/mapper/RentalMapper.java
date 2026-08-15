package com.example.car_rental_api.rental.mapper;

import com.example.car_rental_api.car.mapper.CarMapper;
import com.example.car_rental_api.rental.Rental;
import com.example.car_rental_api.rental.dto.RentalResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CarMapper.class})
public interface RentalMapper {
    RentalResponseDto rentalToRentalResponseDto(Rental rental);
}
