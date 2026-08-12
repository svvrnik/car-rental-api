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
import jakarta.persistence.EntityNotFoundException;
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

    @Test
    void approveCarShouldUpdateCarStatusToAvailable(){
        Car mockCar = new Car();
        mockCar.setId(1L);
        mockCar.setStatus(CarStatus.PENDING);

        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.of(mockCar));

        carService.approveCar(1L);

        Mockito.verify(carRepository, Mockito.times(1)).save(Mockito.any(Car.class));
        Assertions.assertEquals(CarStatus.AVAILABLE, mockCar.getStatus());
    }

    @Test
    void approveCarShouldThrowEntityNotFoundException(){
        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            carService.approveCar(1L);
        });

    Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }

    @Test
    void rejectCarShouldUpdateCarStatusToReject(){
        Car mockCar = new Car();
        mockCar.setId(1L);
        mockCar.setStatus(CarStatus.PENDING);

        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.of(mockCar));

        carService.rejectCar(1L);

        Mockito.verify(carRepository, Mockito.times(1)).save(Mockito.any(Car.class));
        Assertions.assertEquals(CarStatus.REJECTED, mockCar.getStatus());
    }

    @Test
    void rejectCarShouldThrowEntityNotFoundException(){
        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            carService.rejectCar(1L);
        });

        Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }

    @Test
    void withdrawCarShouldUpdateCarStatusToUnavailable(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);

        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setId(1L);

        Car mockCar = new Car();
        mockCar.setStatus(CarStatus.AVAILABLE);
        mockCar.setOwner(mockUser);
        mockCar.setId(1L);

        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.of(mockCar));
        carService.withdrawCar(1L);

        Mockito.verify(carRepository, Mockito.times(1)).save(Mockito.any(Car.class));
        Assertions.assertEquals(CarStatus.UNAVAILABLE, mockCar.getStatus());
    }

    @Test
    void withdrawCarShouldThrowEntityNotFoundException(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            carService.withdrawCar(1L);
        });

        Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }

    @Test
    void withdrawCarShouldThrowIllegalArgumentException(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);

        User mockUser = new User();
        mockUser.setEmail("differentTest@test.com");
        mockUser.setId(1L);

        Car mockCar = new Car();
        mockCar.setStatus(CarStatus.AVAILABLE);
        mockCar.setOwner(mockUser);
        mockCar.setId(1L);

        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.of(mockCar));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            carService.withdrawCar(1L);
        });
        Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }

    @Test
    void withdrawCarShouldThrowIllegalStateException(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);

        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setId(1L);

        Car mockCar = new Car();
        mockCar.setStatus(CarStatus.RENTED);
        mockCar.setOwner(mockUser);
        mockCar.setId(1L);

        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.of(mockCar));

        Assertions.assertThrows(IllegalStateException.class, () -> {
            carService.withdrawCar(1L);
        });
        Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }
}
