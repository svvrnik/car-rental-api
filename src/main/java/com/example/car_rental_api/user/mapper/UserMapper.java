package com.example.car_rental_api.user.mapper;

import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.dto.UserResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto userToUserResponseDto(User user);
}
