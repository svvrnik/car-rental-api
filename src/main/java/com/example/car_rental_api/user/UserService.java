package com.example.car_rental_api.user;

import com.example.car_rental_api.user.dto.UserRegisterDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(UserRegisterDto userRegisterDto){
        User createdUser = new User();
        if(userRepository.existsByEmail(userRegisterDto.getEmail())){
            throw new IllegalArgumentException("User with this e-mail address exists.");
        }
        createdUser.setEmail(userRegisterDto.getEmail());
        createdUser.setFirstName(userRegisterDto.getFirstName());
        createdUser.setLastName(userRegisterDto.getLastName());
        createdUser.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
        createdUser.setRole(Role.USER);
        createdUser.setAccountBalance(BigDecimal.ZERO);

        userRepository.save(createdUser);
    }
}
