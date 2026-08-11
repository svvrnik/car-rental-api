package com.example.car_rental_api;

import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import com.example.car_rental_api.user.UserService;
import com.example.car_rental_api.user.dto.UserRegisterDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    void registerUserShouldCreateNewUserInRepository(){
        UserRegisterDto userToAdd = new UserRegisterDto();

        userToAdd.setEmail("test@test.com");
        userToAdd.setFirstName("testFirstName");
        userToAdd.setLastName("testLastName");
        userToAdd.setPassword("testPassword");

        userService.registerUser(userToAdd);

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
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
}
