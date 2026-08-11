package com.example.car_rental_api.user;

import com.example.car_rental_api.user.dto.UserRegisterDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(UserRegisterDto userRegisterDto){
        User createdUser = new User();
        if(userRepository.existsByEmail(userRegisterDto.getEmail())){
            throw new IllegalArgumentException("User with this e-mail address exists.");
        }
        createdUser.setEmail(userRegisterDto.getEmail());
        createdUser.setFirstName(userRegisterDto.getFirstName());
        createdUser.setLastName(userRegisterDto.getLastName());
        createdUser.setPassword(userRegisterDto.getPassword()); //TODO: add passwordEncoder.encode()
        createdUser.setRole(Role.USER);
        createdUser.setAccountBalance(BigDecimal.ZERO);

        userRepository.save(createdUser);
    }
}
