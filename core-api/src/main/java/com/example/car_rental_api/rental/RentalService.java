package com.example.car_rental_api.rental;

import com.example.car_rental_api.car.Car;
import com.example.car_rental_api.car.CarRepository;
import com.example.car_rental_api.car.CarStatus;
import com.example.car_rental_api.exception.*;
import com.example.car_rental_api.notificaton.dto.EmailNotificationDto;
import com.example.car_rental_api.payment.PaymentService;
import com.example.car_rental_api.rental.dto.RentalRequestDto;
import com.example.car_rental_api.rental.dto.RentalResponseDto;
import com.example.car_rental_api.rental.mapper.RentalMapper;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Service
public class RentalService {
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final RentalMapper rentalMapper;
    private final RabbitTemplate rabbitTemplate;
    private final String companyEmail;
    private final PaymentService paymentService;

    public RentalService(RentalRepository rentalRepository, CarRepository carRepository, UserRepository userRepository, RentalMapper rentalMapper, RabbitTemplate rabbitTemplate, @Value("${app.company.email}") String companyEmail, PaymentService paymentService) {
        this.rentalRepository = rentalRepository;
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.rentalMapper = rentalMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.companyEmail = companyEmail;
        this.paymentService = paymentService;
    }

    private BigDecimal calculateRentalCost(Car car, RentalRequestDto rentalRequestDto){
        //calculates the cost based on calendar days
        //convert to LocalDate to prevent situation where car is rented for 22 hours but for 2 days
        //max because what if sb want to rent car for only x hours? between will return 0 days (and we need to count it as one day)
        return car.getPricePerDay().multiply(BigDecimal.valueOf(Math.max(1,ChronoUnit.DAYS.between(rentalRequestDto.getStartDate().toLocalDate(), rentalRequestDto.getEndDate().toLocalDate()))));
    }

    @Transactional
    public RentalResponseDto rentCar(RentalRequestDto rentalRequestDto){
        if(!rentalRequestDto.getStartDate().isBefore(rentalRequestDto.getEndDate())){
            throw new InvalidRentalPeriodException("Start date must be before end date");
        }

        Car car = carRepository.findByIdWithLock(rentalRequestDto.getCarId()).orElseThrow(() -> new CarNotFoundException("Car not found"));

        if(car.getStatus() != CarStatus.AVAILABLE){
            throw new CarNotAvailableException("Car is not available right now");
        }
        if(rentalRepository.existsOverLappingRental(rentalRequestDto.getCarId(), rentalRequestDto.getStartDate(), rentalRequestDto.getEndDate())){
            throw new OverlappingRentalException("Car is already rented in this time period");
        }

        String loggedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findByEmail(loggedUserEmail).orElseThrow(() -> new UserNotFoundException("User not found"));

        User ownerOfCar = car.getOwner();

        BigDecimal priceForRentPeriod = calculateRentalCost(car, rentalRequestDto);

        paymentService.transferFundsForRental(loggedUser, ownerOfCar, priceForRentPeriod, "Rental: "+car.getBrand()+" "+car.getModel()+" "+car.getLicensePlate());

        Rental rental = new Rental();
        rental.setCar(car);
        rental.setStartDate(rentalRequestDto.getStartDate());
        rental.setEndDate(rentalRequestDto.getEndDate());
        rental.setTotalCost(priceForRentPeriod);
        rental.setUser(loggedUser);
        rental.setStatus(RentalStatus.ACTIVE);
        Rental savedRental = rentalRepository.save(rental);

        EmailNotificationDto emailNotificationDto = new EmailNotificationDto();
        emailNotificationDto.setFrom(companyEmail);
        emailNotificationDto.setTo(loggedUserEmail);
        emailNotificationDto.setSubject("Car Rental Confirmation: " + car.getBrand() + " " + car.getModel());
        emailNotificationDto.setMessage("Hi " + loggedUser.getFirstName() + ",\n\n" +
                "Thank you for renting a car with us! Here are the details of your reservation:\n\n" +
                "Car: " + car.getBrand() + " " + car.getModel() + " (License plate: " + car.getLicensePlate() + ")\n" +
                "Start date: " + rentalRequestDto.getStartDate().toLocalDate() + "\n" +
                "End date: " + rentalRequestDto.getEndDate().toLocalDate() + "\n" +
                "Total cost: " + priceForRentPeriod + " PLN\n\n" +
                "Have a safe trip!\nYour Car Rental Team");

        rabbitTemplate.convertAndSend("emailQueue", emailNotificationDto);

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

    public Page<RentalResponseDto> getUserRentals(Pageable pageable){
        String loggedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        return rentalRepository.findByUserEmail(loggedUserEmail, pageable).map(rentalMapper::rentalToRentalResponseDto);
    }
}
