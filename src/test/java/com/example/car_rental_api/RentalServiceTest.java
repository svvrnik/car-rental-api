package com.example.car_rental_api;

import com.example.car_rental_api.car.Car;
import com.example.car_rental_api.car.CarRepository;
import com.example.car_rental_api.car.CarStatus;
import com.example.car_rental_api.exception.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    void rentCarShouldThrowCarNotFoundException(){
        RentalRequestDto mockRental = new RentalRequestDto();
        mockRental.setCarId(1L);
        Mockito.when(carRepository.findById(mockRental.getCarId())).thenReturn(Optional.empty());

        Assertions.assertThrows(CarNotFoundException.class, () -> {
            rentalService.rentCar(mockRental);
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(User.class), Mockito.any(User.class), Mockito.any(BigDecimal.class), Mockito.any(TransactionType.class));
        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }

    @Test
    void rentCarShouldThrowCarNotAvailableException(){
        RentalRequestDto mockRental = new RentalRequestDto();
        mockRental.setCarId(1L);

        Car mockCar = new Car();
        mockCar.setId(1L);
        mockCar.setStatus(CarStatus.UNAVAILABLE);

        Mockito.when(carRepository.findById(mockRental.getCarId())).thenReturn(Optional.of(mockCar));

        Assertions.assertThrows(CarNotAvailableException.class, () -> {
            rentalService.rentCar(mockRental);
        });
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(User.class), Mockito.any(User.class), Mockito.any(BigDecimal.class), Mockito.any(TransactionType.class));
        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }

    @Test
    void rentCarShouldThrowOverlappingRentalException(){
        RentalRequestDto mockRental = new RentalRequestDto();
        mockRental.setCarId(1L);
        mockRental.setStartDate(LocalDateTime.now());
        mockRental.setEndDate(LocalDateTime.now().plusDays(1));

        Car mockCar = new Car();
        mockCar.setId(1L);
        mockCar.setStatus(CarStatus.AVAILABLE);

        Mockito.when(carRepository.findById(mockRental.getCarId())).thenReturn(Optional.of(mockCar));

        Mockito.when(rentalRepository.existsOverLappingRental(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class))).thenReturn(true);

        Assertions.assertThrows(OverlappingRentalException.class, () -> {
            rentalService.rentCar(mockRental);
        });
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(User.class), Mockito.any(User.class), Mockito.any(BigDecimal.class), Mockito.any(TransactionType.class));
        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }

    @Test
    void rentCarShouldThrowUserNotFoundException() {
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

        Assertions.assertThrows(UserNotFoundException.class, () -> {
            rentalService.rentCar(mockRental);
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(User.class), Mockito.any(User.class), Mockito.any(BigDecimal.class), Mockito.any(TransactionType.class));
        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }

    @Test
    void rentCarShouldThrowInsufficientFundsException() {
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

        Assertions.assertThrows(InsufficientFundsException.class, () -> {
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
    void returnCarShouldThrowRentalNotFoundException() {
        Mockito.when(rentalRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(RentalNotFoundException.class, () -> {
            rentalService.returnCar(1L);
        });

        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }

    @Test
    void returnCarShouldThrowUserIsNotRentalOwnerException() {
        Rental mockRental = new Rental();
        User wrongUser = new User();
        wrongUser.setEmail("test2@test.com");
        mockRental.setUser(wrongUser);

        Mockito.when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));

        Assertions.assertThrows(UserIsNotRentalOwnerException.class, () -> {
            rentalService.returnCar(1L);
        });

        Mockito.verify(rentalRepository, Mockito.never()).save(Mockito.any(Rental.class));
    }

    @Test
    void returnCarShouldThrowRentalNotActiveException() {
        User correctUser = new User();
        correctUser.setEmail("test@test.com");

        Rental mockRental = new Rental();
        mockRental.setUser(correctUser);
        mockRental.setStatus(RentalStatus.COMPLETED);

        Mockito.when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));

        Assertions.assertThrows(RentalNotActiveException.class, () -> {
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
    void getUserRentalsShouldSuccessfullyReturnListOfUserRentals() {
        Rental mockRental1 = new Rental();
        Rental mockRental2 = new Rental();

        RentalResponseDto mockDto1 = new RentalResponseDto();
        RentalResponseDto mockDto2 = new RentalResponseDto();

        Pageable pageable = PageRequest.of(0,10);

        Mockito.when(rentalRepository.findByUserEmail(Mockito.eq("test@test.com"), Mockito.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(mockRental1, mockRental2)));
        Mockito.when(rentalMapper.rentalToRentalResponseDto(mockRental1)).thenReturn(mockDto1);
        Mockito.when(rentalMapper.rentalToRentalResponseDto(mockRental2)).thenReturn(mockDto2);

        Page<RentalResponseDto> result = rentalService.getUserRentals(pageable);

        Assertions.assertEquals(2, result.getTotalElements());
        Mockito.verify(rentalRepository, Mockito.times(1)).findByUserEmail(Mockito.eq("test@test.com"), Mockito.any(Pageable.class));
    }
}
