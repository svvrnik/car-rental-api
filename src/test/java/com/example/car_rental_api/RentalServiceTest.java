package com.example.car_rental_api;

import com.example.car_rental_api.car.Car;
import com.example.car_rental_api.car.CarRepository;
import com.example.car_rental_api.car.CarStatus;
import com.example.car_rental_api.rental.Rental;
import com.example.car_rental_api.rental.RentalRepository;
import com.example.car_rental_api.rental.RentalService;
import com.example.car_rental_api.rental.RentalStatus;
import com.example.car_rental_api.rental.dto.RentalRequestDto;
import com.example.car_rental_api.rental.dto.RentalResponseDto;
import com.example.car_rental_api.rental.mapper.RentalMapper;
import com.example.car_rental_api.transaction.TransactionService;
import com.example.car_rental_api.transaction.TransactionType;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionService transactionService;
    @Mock
    private RentalMapper rentalMapper;
    @InjectMocks
    private RentalService rentalService;

    @BeforeEach
    void setUpSecurity() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.lenient().when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void rentCarShouldThrowExceptionWhenCarIsNotFound(){
        RentalRequestDto mockRental = new RentalRequestDto();
        mockRental.setCarId(1L);
        Mockito.when(carRepository.findById(mockRental.getCarId())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            rentalService.rentCar(mockRental);
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(User.class), Mockito.any(User.class), Mockito.any(BigDecimal.class), Mockito.any(TransactionType.class));
        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }

    @Test
    void rentCarShouldThrowExceptionIfCarIsNotAvailable(){
        RentalRequestDto mockRental = new RentalRequestDto();
        mockRental.setCarId(1L);

        Car mockCar = new Car();
        mockCar.setId(1L);
        mockCar.setStatus(CarStatus.UNAVAILABLE);

        Mockito.when(carRepository.findById(mockRental.getCarId())).thenReturn(Optional.of(mockCar));

        Assertions.assertThrows(IllegalStateException.class, () -> {
            rentalService.rentCar(mockRental);
        });
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(User.class), Mockito.any(User.class), Mockito.any(BigDecimal.class), Mockito.any(TransactionType.class));
        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }

    @Test
    void rentCarShouldThrowExceptionIfExistsOverLappingRental(){
        RentalRequestDto mockRental = new RentalRequestDto();
        mockRental.setCarId(1L);
        mockRental.setStartDate(LocalDateTime.now());
        mockRental.setEndDate(LocalDateTime.now().plusDays(1));

        Car mockCar = new Car();
        mockCar.setId(1L);
        mockCar.setStatus(CarStatus.AVAILABLE);

        Mockito.when(carRepository.findById(mockRental.getCarId())).thenReturn(Optional.of(mockCar));

        Mockito.when(rentalRepository.existsOverLappingRental(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class))).thenReturn(true);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            rentalService.rentCar(mockRental);
        });
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(User.class), Mockito.any(User.class), Mockito.any(BigDecimal.class), Mockito.any(TransactionType.class));
        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }

    @Test
    void rentCarShouldThrowExceptionIfUserNotFound() {
        RentalRequestDto mockRental = new RentalRequestDto();
        mockRental.setCarId(1L);
        mockRental.setStartDate(LocalDateTime.now());
        mockRental.setEndDate(LocalDateTime.now().plusDays(1));

        Car mockCar = new Car();
        mockCar.setId(1L);
        mockCar.setStatus(CarStatus.AVAILABLE);

        Mockito.when(carRepository.findById(mockRental.getCarId())).thenReturn(Optional.of(mockCar));

        Mockito.when(rentalRepository.existsOverLappingRental(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class))).thenReturn(false);

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            rentalService.rentCar(mockRental);
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(User.class), Mockito.any(User.class), Mockito.any(BigDecimal.class), Mockito.any(TransactionType.class));
        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }

    @Test
    void rentCarShouldThrowExceptionIfUserDoNotHaveEnoughCredits() {
        RentalRequestDto mockRental = new RentalRequestDto();
        mockRental.setCarId(1L);
        mockRental.setStartDate(LocalDateTime.now());
        mockRental.setEndDate(LocalDateTime.now().plusDays(1));

        Car mockCar = new Car();
        mockCar.setId(1L);
        mockCar.setStatus(CarStatus.AVAILABLE);
        mockCar.setPricePerDay(BigDecimal.valueOf(100));
        mockCar.setOwner(new User());

        Mockito.when(carRepository.findById(mockRental.getCarId())).thenReturn(Optional.of(mockCar));

        Mockito.when(rentalRepository.existsOverLappingRental(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class))).thenReturn(false);

        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setId(1L);
        mockUser.setAccountBalance(BigDecimal.valueOf(50));

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            rentalService.rentCar(mockRental);
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(User.class), Mockito.any(User.class), Mockito.any(BigDecimal.class), Mockito.any(TransactionType.class));
        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }
    @Test
    void rentCarShouldSuccessfullyRentCarAndSaveData() {
        RentalRequestDto mockRental = new RentalRequestDto();
        mockRental.setCarId(1L);
        mockRental.setStartDate(LocalDateTime.now().plusDays(1));
        mockRental.setEndDate(LocalDateTime.now().plusDays(3));

        User mockOwner = new User();
        mockOwner.setId(2L);
        mockOwner.setAccountBalance(BigDecimal.valueOf(1000));

        Car mockCar = new Car();
        mockCar.setId(1L);
        mockCar.setStatus(CarStatus.AVAILABLE);
        mockCar.setPricePerDay(BigDecimal.valueOf(100));
        mockCar.setOwner(mockOwner);

        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.of(mockCar));

        Mockito.when(rentalRepository.existsOverLappingRental(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class))).thenReturn(false);

        User mockLoggedUser = new User();
        mockLoggedUser.setId(1L);
        mockLoggedUser.setEmail("test@test.com");
        mockLoggedUser.setAccountBalance(BigDecimal.valueOf(500));

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockLoggedUser));

        RentalResponseDto expectedResponse = new RentalResponseDto();
        Mockito.when(rentalRepository.save(Mockito.any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(rentalMapper.rentalToRentalResponseDto(Mockito.any(Rental.class))).thenReturn(expectedResponse);

        RentalResponseDto result = rentalService.rentCar(mockRental);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedResponse, result);

        Mockito.verify(userRepository, Mockito.times(2)).save(Mockito.any(User.class));

        Mockito.verify(transactionService, Mockito.times(1)).createTransaction(
                Mockito.eq(mockLoggedUser),
                Mockito.eq(mockOwner),
                Mockito.eq(BigDecimal.valueOf(200)),
                Mockito.eq(TransactionType.RENTAL_PAYMENT)
        );

        Mockito.verify(rentalRepository, Mockito.times(1)).save(Mockito.any(Rental.class));
    }
    @Test
    void returnCarShouldThrowExceptionWhenRentalIsNotFound() {
        Mockito.when(rentalRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            rentalService.returnCar(1L);
        });

        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }

    @Test
    void returnCarShouldThrowExceptionIfUserIsNotOwner() {
        Rental mockRental = new Rental();
        User wrongUser = new User();
        wrongUser.setEmail("test2@test.com");
        mockRental.setUser(wrongUser);

        Mockito.when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            rentalService.returnCar(1L);
        });

        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }

    @Test
    void returnCarShouldThrowExceptionIfRentalIsNotActive() {
        User correctUser = new User();
        correctUser.setEmail("test@test.com");

        Rental mockRental = new Rental();
        mockRental.setUser(correctUser);
        mockRental.setStatus(RentalStatus.COMPLETED);

        Mockito.when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));

        Assertions.assertThrows(IllegalStateException.class, () -> {
            rentalService.returnCar(1L);
        });

        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }

    @Test
    void returnCarShouldSuccessfullyReturnCarAndSaveData() {
        User correctUser = new User();
        correctUser.setEmail("test@test.com");

        Rental mockRental = new Rental();
        mockRental.setUser(correctUser);
        mockRental.setStatus(RentalStatus.ACTIVE);

        Mockito.when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));

        RentalResponseDto expectedResponse = new RentalResponseDto();
        Mockito.when(rentalRepository.save(Mockito.any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(rentalMapper.rentalToRentalResponseDto(Mockito.any(Rental.class))).thenReturn(expectedResponse);

        RentalResponseDto result = rentalService.returnCar(1L);

        Assertions.assertEquals(RentalStatus.COMPLETED, mockRental.getStatus());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedResponse, result);

        Assertions.assertEquals(RentalStatus.COMPLETED, mockRental.getStatus());

        Mockito.verify(rentalRepository, Mockito.times(1)).save(mockRental);
    }

    @Test
    void getUserRentalsShouldThrowExceptionIfUserNotFound() {
        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            rentalService.getUserRentals();
        });

        Mockito.verify(rentalRepository, Mockito.never()).findByUserEmail(Mockito.anyString());
    }

    @Test
    void getUserRentalsShouldSuccessfullyReturnListOfUserRentals() {
        User mockUser = new User();
        mockUser.setEmail("test@test.com");

        Rental mockRental1 = new Rental();
        Rental mockRental2 = new Rental();

        RentalResponseDto mockDto1 = new RentalResponseDto();
        RentalResponseDto mockDto2 = new RentalResponseDto();

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        Mockito.when(rentalRepository.findByUserEmail("test@test.com")).thenReturn(java.util.List.of(mockRental1, mockRental2));
        Mockito.when(rentalMapper.rentalToRentalResponseDto(mockRental1)).thenReturn(mockDto1);
        Mockito.when(rentalMapper.rentalToRentalResponseDto(mockRental2)).thenReturn(mockDto2);

        java.util.List<RentalResponseDto> result = rentalService.getUserRentals();

        Assertions.assertEquals(2, result.size());
        Mockito.verify(rentalRepository, Mockito.times(1)).findByUserEmail("test@test.com");
    }
}
