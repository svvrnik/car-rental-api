package com.example.car_rental_api.rental;

import com.example.car_rental_api.car.Car;
import com.example.car_rental_api.car.CarRepository;
import com.example.car_rental_api.car.CarStatus;
import com.example.car_rental_api.exception.*;
import com.example.car_rental_api.rental.dto.RentalRequestDto;
import com.example.car_rental_api.rental.dto.RentalResponseDto;
import com.example.car_rental_api.rental.mapper.RentalMapper;
import com.example.car_rental_api.transaction.TransactionService;
import com.example.car_rental_api.transaction.TransactionType;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RentalService {
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final RentalMapper rentalMapper;

    public RentalService(RentalRepository rentalRepository, CarRepository carRepository, UserRepository userRepository, TransactionService transactionService, RentalMapper rentalMapper) {
        this.rentalRepository = rentalRepository;
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.rentalMapper = rentalMapper;
    }
    @Transactional
    public RentalResponseDto rentCar(RentalRequestDto rentalRequestDto){
        Car car = carRepository.findById(rentalRequestDto.getCarId()).orElseThrow(() -> new CarNotFoundException("Car not found"));

        if(car.getStatus() != CarStatus.AVAILABLE){
            throw new CarNotAvailableException("Car is not available right now");
        }
        if(rentalRepository.existsOverLappingRental(rentalRequestDto.getCarId(), rentalRequestDto.getStartDate(), rentalRequestDto.getEndDate())){
            throw new OverlappingRentalException("Car is already rented in this time period");
        }

        String loggedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findByEmail(loggedUserEmail).orElseThrow(() -> new UserNotFoundException("User not found"));

        User ownerOfCar = car.getOwner();

        //max because what if sb want to rent car for only x hours? between will return 0 days (and we need to count it as one day)
        BigDecimal priceForRentPeriod = car.getPricePerDay().multiply(BigDecimal.valueOf(Math.max(1,ChronoUnit.DAYS.between(rentalRequestDto.getStartDate(), rentalRequestDto.getEndDate()))));
        if(priceForRentPeriod.compareTo(loggedUser.getAccountBalance())>0){
            throw new InsufficientFundsException("Insufficient funds");
        }

        loggedUser.setAccountBalance(loggedUser.getAccountBalance().subtract(priceForRentPeriod));
        userRepository.save(loggedUser);

        ownerOfCar.setAccountBalance(ownerOfCar.getAccountBalance().add(priceForRentPeriod));
        userRepository.save(ownerOfCar);

        transactionService.createTransaction(loggedUser, car.getOwner(), priceForRentPeriod, TransactionType.RENTAL_PAYMENT);

        Rental rental = new Rental();
        rental.setCar(car);
        rental.setStartDate(rentalRequestDto.getStartDate());
        rental.setEndDate(rentalRequestDto.getEndDate());
        rental.setTotalCost(priceForRentPeriod);
        rental.setUser(loggedUser);
        rental.setStatus(RentalStatus.ACTIVE);
        Rental savedRental = rentalRepository.save(rental);

        return rentalMapper.rentalToRentalResponseDto(savedRental);
    }

    public RentalResponseDto returnCar(Long rentalId){
        String loggedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Rental rental = rentalRepository.findById(rentalId).orElseThrow(() -> new RentalNotFoundException("Rental not found"));

        if(!rental.getUser().getEmail().equals(loggedUserEmail)){
            throw new UserIsNotRentalOwnerException("You can only return your own rentals");
        }

        if(rental.getStatus()!=RentalStatus.ACTIVE){
            throw new RentalNotActiveException("This rental is not active or already completed");
        }

        rental.setStatus(RentalStatus.COMPLETED);
        Rental savedRental = rentalRepository.save(rental);

        return rentalMapper.rentalToRentalResponseDto(savedRental);
    }

    public List<RentalResponseDto> getUserRentals(){
        String loggedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        return rentalRepository.findByUserEmail(loggedUserEmail).stream().map(rentalMapper::rentalToRentalResponseDto).toList();
    }
}
