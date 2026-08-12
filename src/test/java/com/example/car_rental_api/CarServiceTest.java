package com.example.car_rental_api;

import com.example.car_rental_api.car.Car;
import com.example.car_rental_api.car.CarRepository;
import com.example.car_rental_api.car.CarService;
import com.example.car_rental_api.car.CarStatus;
import com.example.car_rental_api.car.dto.CarRequestDto;
import com.example.car_rental_api.car.dto.CarResponseDto;
import com.example.car_rental_api.user.Role;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
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

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
    @Mock
    private CarRepository carRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CarService carService;

    @Test
    void addCarShouldReturnCarResponseDtoWhenDataIsValid(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");

        CarRequestDto testCarToAdd = new CarRequestDto();
        testCarToAdd.setBrand("testBrand");
        testCarToAdd.setLicensePlate("testLicensePlate");
        testCarToAdd.setModel("testModel");

        Car savedCar = new Car();
        savedCar.setId(1L);
        savedCar.setBrand("testBrand");
        savedCar.setLicensePlate("testLicensePlate");
        savedCar.setModel("testModel");
        savedCar.setStatus(CarStatus.PENDING);


        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        Mockito.when(carRepository.existsByLicensePlate("testLicensePlate")).thenReturn(false);

        Mockito.when(carRepository.save(Mockito.any(Car.class))).thenReturn(savedCar);



        CarResponseDto result = carService.addCar(testCarToAdd);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.getId());
        Mockito.verify(carRepository, Mockito.times(1)).save(Mockito.any(Car.class));
    }

    @Test
    void addCarShouldThrowUsernameNotFoundExceptionWhenOwnerIdDoNotExist(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        CarRequestDto testCarToAdd = new CarRequestDto();
        testCarToAdd.setBrand("testBrand");
        testCarToAdd.setLicensePlate("testLicensePlate");
        testCarToAdd.setModel("testModel");

        Assertions.assertThrows(UsernameNotFoundException.class,() -> {
            carService.addCar(testCarToAdd);
        });

        Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }

    @Test
    void addCarShouldThrowIllegalArgumentExceptionWhenCarWithLicensePlateAlreadyExists(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);

        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setId(1L);

        CarRequestDto testCarToAdd = new CarRequestDto();
        testCarToAdd.setBrand("testBrand");
        testCarToAdd.setLicensePlate("testLicensePlate");
        testCarToAdd.setModel("testModel");

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        Mockito.when(carRepository.existsByLicensePlate("testLicensePlate")).thenReturn(true);

        Assertions.assertThrows(IllegalArgumentException.class, () ->{
            carService.addCar(testCarToAdd);
        });

        Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }

    @Test
    void getAvailableCarsShouldReturnListOfCarsWithAvailableStatus(){
        Car mockCar = new Car();
        mockCar.setId(1L);
        mockCar.setStatus(CarStatus.AVAILABLE);

        Mockito.when(carRepository.findByStatus(CarStatus.AVAILABLE)).thenReturn(List.of(mockCar));

        List<CarResponseDto> result = carService.getAvailableCars();

        Assertions.assertEquals(1, result.size());
    }
}
