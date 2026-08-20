package com.example.car_rental_api.user;

import com.example.car_rental_api.exception.EmailAlreadyInUseException;
import com.example.car_rental_api.exception.UserNotFoundException;
import com.example.car_rental_api.payment.PaymentService;
import com.example.car_rental_api.transaction.TransactionService;
import com.example.car_rental_api.user.dto.FundRequestDto;
import com.example.car_rental_api.user.dto.UserRegisterDto;
import com.example.car_rental_api.user.dto.UserResponseDto;
import com.example.car_rental_api.user.mapper.UserMapper;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PaymentService paymentService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, PaymentService paymentService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.paymentService = paymentService;
    }

    public UserResponseDto registerUser(UserRegisterDto userRegisterDto){
        User createdUser = new User();
        if(userRepository.existsByEmail(userRegisterDto.getEmail())){
            throw new EmailAlreadyInUseException("User with this e-mail address exists.");
        }
        createdUser.setEmail(userRegisterDto.getEmail());
        createdUser.setFirstName(userRegisterDto.getFirstName());
        createdUser.setLastName(userRegisterDto.getLastName());
        createdUser.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
        createdUser.setRole(Role.USER);
        createdUser.setAccountBalance(BigDecimal.ZERO);

        User savedUser = userRepository.save(createdUser);
        return userMapper.userToUserResponseDto(savedUser);
    }
    @Transactional
    public UserResponseDto addFunds(FundRequestDto fundRequestDto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User foundUser = userRepository.findByEmail(email).orElseThrow(() ->  new UserNotFoundException("User not found"));

        User savedUser = paymentService.addFundsToUserAccount(foundUser, fundRequestDto.getAmount(), "Account deposit");

        return userMapper.userToUserResponseDto(savedUser);
    }
    @Transactional
    public UserResponseDto withdrawFunds(FundRequestDto fundRequestDto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User foundUser = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

        User savedUser = paymentService.withdrawFundsFromUserAccount(foundUser, fundRequestDto.getAmount(), "Account withdrawal");

        return userMapper.userToUserResponseDto(savedUser);
    }
}
