package com.example.car_rental_api.user;

import com.example.car_rental_api.exception.EmailAlreadyInUseException;
import com.example.car_rental_api.exception.InsufficientFundsException;
import com.example.car_rental_api.exception.UserNotFoundException;
import com.example.car_rental_api.transaction.TransactionService;
import com.example.car_rental_api.transaction.TransactionType;
import com.example.car_rental_api.user.dto.FundRequestDto;
import com.example.car_rental_api.user.dto.UserRegisterDto;
import com.example.car_rental_api.user.dto.UserResponseDto;
import com.example.car_rental_api.user.mapper.UserMapper;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionService transactionService;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TransactionService transactionService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionService = transactionService;
        this.userMapper = userMapper;
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

        foundUser.setAccountBalance(foundUser.getAccountBalance().add(fundRequestDto.getAmount()));
        User savedUser = userRepository.save(foundUser);

        transactionService.createTransaction(null, foundUser, fundRequestDto.getAmount(), TransactionType.DEPOSIT);

        return userMapper.userToUserResponseDto(savedUser);
    }
    @Transactional
    public UserResponseDto withdrawFunds(FundRequestDto fundRequestDto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User foundUser = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

        if(foundUser.getAccountBalance().compareTo(fundRequestDto.getAmount())<0){
            throw new InsufficientFundsException("Insufficient funds");
        }

        foundUser.setAccountBalance(foundUser.getAccountBalance().subtract(fundRequestDto.getAmount()));
        User savedUser = userRepository.save(foundUser);

        transactionService.createTransaction(foundUser, null, fundRequestDto.getAmount(), TransactionType.PAYOUT);

        return userMapper.userToUserResponseDto(savedUser);
    }
}
