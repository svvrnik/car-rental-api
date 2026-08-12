package com.example.car_rental_api;

import com.example.car_rental_api.transaction.TransactionService;
import com.example.car_rental_api.transaction.TransactionType;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import com.example.car_rental_api.user.UserService;
import com.example.car_rental_api.user.dto.FundRequestDto;
import com.example.car_rental_api.user.dto.UserRegisterDto;
import com.example.car_rental_api.user.dto.UserResponseDto;
import com.example.car_rental_api.user.mapper.UserMapper;
import io.jsonwebtoken.lang.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private TransactionService transactionService;
    @InjectMocks
    private UserService userService;

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
    void registerUserShouldThrowIllegalArgumentExceptionWhenUserWithThisEmailExists(){
        UserRegisterDto userToAdd = new UserRegisterDto();

        userToAdd.setEmail("test@test.com");
        userToAdd.setFirstName("testFirstName");
        userToAdd.setLastName("testLastName");
        userToAdd.setPassword("testPassword");

        Mockito.when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(userToAdd);
        });
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void addFundsShouldIncreaseUserAccountBalance(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setAccountBalance(BigDecimal.ZERO);

        FundRequestDto fundRequestDto = new FundRequestDto();
        fundRequestDto.setAmount(new BigDecimal("100.00"));

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setAccountBalance(new BigDecimal("100.00"));

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(mockUser);
        Mockito.when(userMapper.userToUserResponseDto(Mockito.any(User.class))).thenReturn(expectedResponse);

        UserResponseDto result = userService.addFunds(fundRequestDto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(new BigDecimal("100.00"), mockUser.getAccountBalance());
        Assertions.assertEquals(new BigDecimal("100.00"), result.getAccountBalance());

        Mockito.verify(userRepository, Mockito.times(1)).save(mockUser);
        Mockito.verify(transactionService, Mockito.times(1)).createTransaction(null, mockUser, new BigDecimal("100.00"), TransactionType.DEPOSIT);
    }

    @Test
    void addFundsShouldThrowUsernameNotFoundException(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        FundRequestDto fundRequestDto = new FundRequestDto();
        fundRequestDto.setAmount(new BigDecimal("100.00"));

        Assertions.assertThrows(UsernameNotFoundException.class,() -> {
            userService.addFunds(fundRequestDto);
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));

        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void withdrawFundsShouldDecreaseUserAccountBalance(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setAccountBalance(new BigDecimal("200.00"));

        FundRequestDto fundRequestDto = new FundRequestDto();
        fundRequestDto.setAmount(new BigDecimal("50.00"));

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setAccountBalance(new BigDecimal("150.00"));

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(mockUser);
        Mockito.when(userMapper.userToUserResponseDto(Mockito.any(User.class))).thenReturn(expectedResponse);

        UserResponseDto result = userService.withdrawFunds(fundRequestDto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(new BigDecimal("150.00"), mockUser.getAccountBalance());
        Assertions.assertEquals(new BigDecimal("150.00"), result.getAccountBalance());

        Mockito.verify(userRepository, Mockito.times(1)).save(mockUser);
        Mockito.verify(transactionService, Mockito.times(1)).createTransaction(mockUser, null, new BigDecimal("50.00"), TransactionType.PAYOUT);
    }
    @Test
    void withdrawFundsShouldThrowIllegalArgumentExceptionWhenInsufficientFunds(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setAccountBalance(new BigDecimal("20.00"));

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        FundRequestDto fundRequestDto = new FundRequestDto();
        fundRequestDto.setAmount(new BigDecimal("100.00"));

        Assertions.assertThrows(IllegalArgumentException.class,() -> {
            userService.withdrawFunds(fundRequestDto);
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void withdrawFundsShouldThrowUsernameNotFoundException(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        FundRequestDto fundRequestDto = new FundRequestDto();
        fundRequestDto.setAmount(new BigDecimal("100.00"));

        Assertions.assertThrows(UsernameNotFoundException.class,() -> {
            userService.withdrawFunds(fundRequestDto);
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }
}
