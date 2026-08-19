package com.example.car_rental_api;

import com.example.car_rental_api.exception.EmailAlreadyInUseException;
import com.example.car_rental_api.exception.InsufficientFundsException;
import com.example.car_rental_api.exception.UserNotFoundException;
import com.example.car_rental_api.payment.PaymentService;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import com.example.car_rental_api.user.UserService;
import com.example.car_rental_api.user.dto.FundRequestDto;
import com.example.car_rental_api.user.dto.UserRegisterDto;
import com.example.car_rental_api.user.dto.UserResponseDto;
import com.example.car_rental_api.user.mapper.UserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PaymentService paymentService;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUpSecurity() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.lenient().when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void registerUserShouldCreateNewUserInRepositoryAndReturnResponse(){
        UserRegisterDto userToAdd = new UserRegisterDto();

        userToAdd.setEmail("test@test.com");
        userToAdd.setFirstName("testFirstName");
        userToAdd.setLastName("testLastName");
        userToAdd.setPassword("testPassword");

        Mockito.when(userMapper.userToUserResponseDto(Mockito.any(User.class))).thenReturn(new UserResponseDto());
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("pass");

        User mockedSavedUser = new User();
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(mockedSavedUser);

        UserResponseDto response = userService.registerUser(userToAdd);

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Assertions.assertNotNull(response);
    }
    @Test
    void registerUserShouldThrowEmailAlreadyInUseException(){
        UserRegisterDto userToAdd = new UserRegisterDto();

        userToAdd.setEmail("test@test.com");
        userToAdd.setFirstName("testFirstName");
        userToAdd.setLastName("testLastName");
        userToAdd.setPassword("testPassword");

        Mockito.when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        Assertions.assertThrows(EmailAlreadyInUseException.class, () -> {
            userService.registerUser(userToAdd);
        });
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void addFundsShouldIncreaseUserAccountBalance(){
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setAccountBalance(BigDecimal.ZERO);

        FundRequestDto fundRequestDto = new FundRequestDto();
        fundRequestDto.setAmount(new BigDecimal("100.00"));

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setAccountBalance(new BigDecimal("100.00"));

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        Mockito.when(paymentService.addFundsToUserAccount(Mockito.any(User.class), Mockito.any(BigDecimal.class))).thenReturn(mockUser);
        Mockito.when(userMapper.userToUserResponseDto(Mockito.any(User.class))).thenReturn(expectedResponse);

        UserResponseDto result = userService.addFunds(fundRequestDto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(new BigDecimal("100.00"), result.getAccountBalance());

       Mockito.verify(paymentService, Mockito.times(1)).addFundsToUserAccount(mockUser, new BigDecimal("100.00"));
    }

    @Test
    void addFundsShouldThrowUserNotFoundException(){
        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        FundRequestDto fundRequestDto = new FundRequestDto();
        fundRequestDto.setAmount(new BigDecimal("100.00"));

        Assertions.assertThrows(UserNotFoundException.class,() -> {
            userService.addFunds(fundRequestDto);
        });

        Mockito.verify(paymentService, Mockito.never()).addFundsToUserAccount(Mockito.any(User.class), Mockito.any(BigDecimal.class));
    }

    @Test
    void withdrawFundsShouldDecreaseUserAccountBalance(){
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setAccountBalance(new BigDecimal("200.00"));

        FundRequestDto fundRequestDto = new FundRequestDto();
        fundRequestDto.setAmount(new BigDecimal("50.00"));

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setAccountBalance(new BigDecimal("150.00"));

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        Mockito.when(paymentService.withdrawFundsFromUserAccount(Mockito.any(User.class), Mockito.any(BigDecimal.class))).thenReturn(mockUser);
        Mockito.when(userMapper.userToUserResponseDto(Mockito.any(User.class))).thenReturn(expectedResponse);

        UserResponseDto result = userService.withdrawFunds(fundRequestDto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(new BigDecimal("150.00"), result.getAccountBalance());

        Mockito.verify(paymentService, Mockito.times(1)).withdrawFundsFromUserAccount(mockUser, new BigDecimal("50.00"));
    }
    @Test
    void withdrawFundsShouldThrowInsufficientFundsException(){
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setAccountBalance(new BigDecimal("20.00"));

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        Mockito.when(paymentService.withdrawFundsFromUserAccount(Mockito.any(User.class), Mockito.any(BigDecimal.class))).thenThrow(new InsufficientFundsException("Insufficient funds"));

        FundRequestDto fundRequestDto = new FundRequestDto();
        fundRequestDto.setAmount(new BigDecimal("100.00"));

        Assertions.assertThrows(InsufficientFundsException.class,() -> {
            userService.withdrawFunds(fundRequestDto);
        });
    }

    @Test
    void withdrawFundsShouldThrowUserNotFoundException(){
        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        FundRequestDto fundRequestDto = new FundRequestDto();
        fundRequestDto.setAmount(new BigDecimal("100.00"));

        Assertions.assertThrows(UserNotFoundException.class,() -> {
            userService.withdrawFunds(fundRequestDto);
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }
}
