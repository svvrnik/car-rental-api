package com.example.car_rental_api.user;

import com.example.car_rental_api.user.dto.FundRequestDto;
import com.example.car_rental_api.user.dto.UserRegisterDto;
import com.example.car_rental_api.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody @Valid UserRegisterDto userRegisterDto){
        userService.registerUser(userRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/add-funds")
    public ResponseEntity<UserResponseDto> addFunds(@RequestBody @Valid FundRequestDto fundRequestDto){
        UserResponseDto response = userService.addFunds(fundRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/withdraw-funds")
    public ResponseEntity<UserResponseDto> withdrawfunds(@RequestBody @Valid FundRequestDto fundRequestDto){
        UserResponseDto response = userService.withdrawFunds(fundRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
