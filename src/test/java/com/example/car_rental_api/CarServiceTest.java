package com.example.car_rental_api;

import com.example.car_rental_api.car.Car;
import com.example.car_rental_api.car.CarRepository;
import com.example.car_rental_api.car.CarService;
import com.example.car_rental_api.car.CarStatus;
import com.example.car_rental_api.car.dto.CarRequestDto;
import com.example.car_rental_api.car.dto.CarResponseDto;
import com.example.car_rental_api.car.mapper.CarMapper;
import com.example.car_rental_api.exception.*;
import com.example.car_rental_api.storage.FileStorageService;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
    @Mock
    private CarRepository carRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CarMapper carMapper;
    @Mock
    private FileStorageService fileStorageService;
    @InjectMocks
    private CarService carService;

    @BeforeEach
    void setUpSecurity() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.lenient().when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void addCarShouldReturnCarResponseDtoWhenDataIsValid(){
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

        Mockito.when(carMapper.carRequestDtoToCar(Mockito.any(CarRequestDto.class))).thenReturn(savedCar);

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        Mockito.when(carRepository.existsByLicensePlate("testLicensePlate")).thenReturn(false);

        Mockito.when(carRepository.save(Mockito.any(Car.class))).thenReturn(savedCar);

        CarResponseDto expectedResponse = new CarResponseDto();
        expectedResponse.setId(1L);
        Mockito.when(carMapper.carToCarResponseDto(Mockito.any(Car.class))).thenReturn(expectedResponse);

        CarResponseDto result = carService.addCar(testCarToAdd, null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.getId());
        Mockito.verify(carRepository, Mockito.times(1)).save(Mockito.any(Car.class));
    }

    @Test
    void addCarShouldThrowUserNotFoundExceptionWhenOwnerIdDoNotExist(){
        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        CarRequestDto testCarToAdd = new CarRequestDto();
        testCarToAdd.setBrand("testBrand");
        testCarToAdd.setLicensePlate("testLicensePlate");
        testCarToAdd.setModel("testModel");

        Assertions.assertThrows(UserNotFoundException.class,() -> {
            carService.addCar(testCarToAdd, null);
        });

        Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }

    @Test
    void addCarShouldThrowDuplicateLicensePlateExceptionWhenCarWithLicensePlateAlreadyExists(){
        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setId(1L);

        CarRequestDto testCarToAdd = new CarRequestDto();
        testCarToAdd.setBrand("testBrand");
        testCarToAdd.setLicensePlate("testLicensePlate");
        testCarToAdd.setModel("testModel");

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        Mockito.when(carRepository.existsByLicensePlate("testLicensePlate")).thenReturn(true);

        Assertions.assertThrows(DuplicateLicensePlateException.class, () ->{
            carService.addCar(testCarToAdd, null);
        });

        Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }

    @Test
    void addCarShouldThrowInvalidFileException(){
        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setId(1L);

        CarRequestDto testCarToAdd = new CarRequestDto();
        testCarToAdd.setBrand("testBrand");
        testCarToAdd.setLicensePlate("testLicensePlate");
        testCarToAdd.setModel("testModel");

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        Mockito.when(carRepository.existsByLicensePlate("testLicensePlate")).thenReturn(false);

        Car mockCar = new Car();
        Mockito.when(carMapper.carRequestDtoToCar(Mockito.any(CarRequestDto.class))).thenReturn(mockCar);

        MultipartFile badFile = Mockito.mock(MultipartFile.class);
        Mockito.when(badFile.getContentType()).thenReturn("application/pdf");
        MultipartFile[] files = { badFile };

        Assertions.assertThrows(InvalidFileException.class, () -> {
            carService.addCar(testCarToAdd, files);
        });

        Mockito.verify(fileStorageService, Mockito.never()).uploadFile(Mockito.any());
        Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }

    @Test
    void getAvailableCarsShouldReturnListOfCarsWithAvailableStatus(){
        Car mockCar = new Car();
        mockCar.setId(1L);
        mockCar.setStatus(CarStatus.AVAILABLE);

        Pageable pageable = PageRequest.of(0,10);

        Mockito.when(carRepository.findByStatus(Mockito.eq(CarStatus.AVAILABLE), Mockito.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(mockCar)));

        Mockito.when(carMapper.carToCarResponseDto(Mockito.any(Car.class))).thenReturn(new CarResponseDto());

        Page<CarResponseDto> result = carService.getAvailableCars(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());

    }

    @Test
    void approveCarShouldUpdateCarStatusToAvailable(){
        Car mockCar = new Car();
        mockCar.setId(1L);
        mockCar.setStatus(CarStatus.PENDING);

        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.of(mockCar));

        Mockito.when(carMapper.carToCarResponseDto(Mockito.any(Car.class))).thenReturn(new CarResponseDto());

        Mockito.when(carRepository.save(Mockito.any(Car.class))).thenReturn(mockCar);

        CarResponseDto result = carService.approveCar(1L);

        Mockito.verify(carRepository, Mockito.times(1)).save(Mockito.any(Car.class));
        Assertions.assertEquals(CarStatus.AVAILABLE, mockCar.getStatus());
        Assertions.assertNotNull(result);
    }

    @Test
    void approveCarShouldThrowCarNotFoundException(){
        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(CarNotFoundException.class, () -> {
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

        Mockito.when(carMapper.carToCarResponseDto(Mockito.any(Car.class))).thenReturn(new CarResponseDto());

        Mockito.when(carRepository.save(Mockito.any(Car.class))).thenReturn(mockCar);

        CarResponseDto result = carService.rejectCar(1L);

        Mockito.verify(carRepository, Mockito.times(1)).save(Mockito.any(Car.class));
        Assertions.assertEquals(CarStatus.REJECTED, mockCar.getStatus());
        Assertions.assertNotNull(result);
    }

    @Test
    void rejectCarShouldThrowCarNotFoundException(){
        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(CarNotFoundException.class, () -> {
            carService.rejectCar(1L);
        });

        Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }

    @Test
    void withdrawCarShouldUpdateCarStatusToUnavailable(){
        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setId(1L);

        Car mockCar = new Car();
        mockCar.setStatus(CarStatus.AVAILABLE);
        mockCar.setOwner(mockUser);
        mockCar.setId(1L);

        Mockito.when(carMapper.carToCarResponseDto(Mockito.any(Car.class))).thenReturn(new CarResponseDto());

        Mockito.when(carRepository.save(Mockito.any(Car.class))).thenReturn(mockCar);

        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.of(mockCar));
        CarResponseDto result = carService.withdrawCar(1L);

        Mockito.verify(carRepository, Mockito.times(1)).save(Mockito.any(Car.class));
        Assertions.assertEquals(CarStatus.UNAVAILABLE, mockCar.getStatus());
        Assertions.assertNotNull(result);
    }

    @Test
    void withdrawCarShouldThrowCarNotFoundException(){
        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(CarNotFoundException.class, () -> {
            carService.withdrawCar(1L);
        });

        Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }

    @Test
    void withdrawCarShouldThrowUserIsNotCarOwnerException(){
        User mockUser = new User();
        mockUser.setEmail("differentTest@test.com");
        mockUser.setId(1L);

        Car mockCar = new Car();
        mockCar.setStatus(CarStatus.AVAILABLE);
        mockCar.setOwner(mockUser);
        mockCar.setId(1L);

        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.of(mockCar));

        Assertions.assertThrows(UserIsNotCarOwnerException.class, () -> {
            carService.withdrawCar(1L);
        });
        Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }

    @Test
    void withdrawCarShouldThrowCarCurrentlyRentedException(){
        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setId(1L);

        Car mockCar = new Car();
        mockCar.setStatus(CarStatus.RENTED);
        mockCar.setOwner(mockUser);
        mockCar.setId(1L);

        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.of(mockCar));

        Assertions.assertThrows(CarCurrentlyRentedException.class, () -> {
            carService.withdrawCar(1L);
        });
        Mockito.verify(carRepository, Mockito.never()).save(Mockito.any(Car.class));
    }
    @Test
    void getUserCarsShouldReturnListOfCarsForLoggedUsers(){
        Car mockCar = new Car();
        mockCar.setId(1L);

        Mockito.when(carRepository.findByOwnerEmail(Mockito.eq("test@test.com"), Mockito.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(mockCar)));
        Mockito.when(carMapper.carToCarResponseDto(Mockito.any(Car.class))).thenReturn(new CarResponseDto());

        Pageable pageable = PageRequest.of(0,10);

        Page<CarResponseDto> result = carService.getUserCars(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Mockito.verify(carRepository, Mockito.times(1)).findByOwnerEmail(Mockito.eq("test@test.com"), Mockito.any(Pageable.class));
    }
}
