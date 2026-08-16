package com.example.car_rental_api.integration;

import static io.restassured.RestAssured.given;
import com.example.car_rental_api.car.Car;
import com.example.car_rental_api.car.CarRepository;
import com.example.car_rental_api.car.CarStatus;
import com.example.car_rental_api.rental.Rental;
import com.example.car_rental_api.rental.RentalRepository;
import com.example.car_rental_api.rental.RentalStatus;
import com.example.car_rental_api.rental.dto.RentalRequestDto;
import com.example.car_rental_api.transaction.TransactionRepository;
import com.example.car_rental_api.user.Role;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import com.example.car_rental_api.utils.JWTUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RentalControllerTest {

    @LocalServerPort
    private Integer port;

    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            "postgres:15-alpine"
    );

    @BeforeAll
    static void beforeAll(){
        postgreSQLContainer.start();
    }
    @AfterAll
    static void afterAll(){
        postgreSQLContainer.stop();
    }
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    RentalRepository rentalRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    JWTUtil jwtUtil;

    @BeforeEach
    void setUp(){
        RestAssured.baseURI = "http://localhost:"+port;
        rentalRepository.deleteAll();
        transactionRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void rentCarShouldSuccessfullyRentCar(){
        User owner = new User();
        owner.setEmail("test@test.com");
        owner.setPassword("password123");
        owner.setRole(Role.USER);
        owner.setFirstName("TestFirstName");
        owner.setLastName("TestLastName");
        owner.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(owner);

        Car car = new Car();
        car.setStatus(CarStatus.AVAILABLE);
        car.setLicensePlate("TEST");
        car.setModel("testModel");
        car.setBrand("testBrand");
        car.setDescription("testDescription");
        car.setPricePerDay(BigDecimal.valueOf(100));
        car.setOwner(owner);
        Car savedCar = carRepository.save(car);

        User renter = new User();
        renter.setEmail("test2@test.com");
        renter.setAccountBalance(BigDecimal.valueOf(1000));
        renter.setPassword("password123");
        renter.setRole(Role.USER);
        renter.setFirstName("TestFirstName2");
        renter.setLastName("TestLastName2");
        userRepository.save(renter);

        String token = jwtUtil.generateToken(renter.getEmail());

        RentalRequestDto rentalRequestDto = new RentalRequestDto();
        rentalRequestDto.setCarId(savedCar.getId());
        rentalRequestDto.setStartDate(LocalDateTime.now().plusDays(1));
        rentalRequestDto.setEndDate(LocalDateTime.now().plusDays(3));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(rentalRequestDto)
                .when()
                .post("/api/rentals/rent")
                .then()
                .statusCode(HttpStatus.CREATED.value());
        Assertions.assertEquals(1, rentalRepository.count());
    }

    @Test
    void rentCarShouldThrowCarNotAvailableExceptionBadRequest(){
        User owner = new User();
        owner.setEmail("test@test.com");
        owner.setPassword("password123");
        owner.setRole(Role.USER);
        owner.setFirstName("TestFirstName");
        owner.setLastName("TestLastName");
        owner.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(owner);

        Car car = new Car();
        car.setStatus(CarStatus.UNAVAILABLE);
        car.setLicensePlate("TEST");
        car.setModel("testModel");
        car.setBrand("testBrand");
        car.setDescription("testDescription");
        car.setPricePerDay(BigDecimal.valueOf(100));
        car.setOwner(owner);
        Car savedCar = carRepository.save(car);

        User renter = new User();
        renter.setEmail("test2@test.com");
        renter.setAccountBalance(BigDecimal.valueOf(1000));
        renter.setPassword("password123");
        renter.setRole(Role.USER);
        renter.setFirstName("TestFirstName2");
        renter.setLastName("TestLastName2");
        userRepository.save(renter);

        String token = jwtUtil.generateToken(renter.getEmail());

        RentalRequestDto rentalRequestDto = new RentalRequestDto();
        rentalRequestDto.setCarId(savedCar.getId());
        rentalRequestDto.setStartDate(LocalDateTime.now().plusDays(1));
        rentalRequestDto.setEndDate(LocalDateTime.now().plusDays(3));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(rentalRequestDto)
                .when()
                .post("/api/rentals/rent")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(0, rentalRepository.count());
    }

    @Test
    void rentCarShouldThrowCarNotFoundException(){
        User owner = new User();
        owner.setEmail("test@test.com");
        owner.setPassword("password123");
        owner.setRole(Role.USER);
        owner.setFirstName("TestFirstName");
        owner.setLastName("TestLastName");
        owner.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(owner);

        User renter = new User();
        renter.setEmail("test2@test.com");
        renter.setAccountBalance(BigDecimal.valueOf(1000));
        renter.setPassword("password123");
        renter.setRole(Role.USER);
        renter.setFirstName("TestFirstName2");
        renter.setLastName("TestLastName2");
        userRepository.save(renter);

        String token = jwtUtil.generateToken(renter.getEmail());

        RentalRequestDto rentalRequestDto = new RentalRequestDto();
        rentalRequestDto.setCarId(1L);
        rentalRequestDto.setStartDate(LocalDateTime.now().plusDays(1));
        rentalRequestDto.setEndDate(LocalDateTime.now().plusDays(3));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(rentalRequestDto)
                .when()
                .post("/api/rentals/rent")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        Assertions.assertEquals(0, rentalRepository.count());
    }

    @Test
    void returnCarShouldSuccessfullyReturnCar(){
        User owner = new User();
        owner.setEmail("test@test.com");
        owner.setPassword("password123");
        owner.setRole(Role.USER);
        owner.setFirstName("TestFirstName");
        owner.setLastName("TestLastName");
        owner.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(owner);

        User renter = new User();
        renter.setEmail("test2@test.com");
        renter.setAccountBalance(BigDecimal.valueOf(1000));
        renter.setPassword("password123");
        renter.setRole(Role.USER);
        renter.setFirstName("TestFirstName2");
        renter.setLastName("TestLastName2");
        userRepository.save(renter);

        Car car = new Car();
        car.setStatus(CarStatus.AVAILABLE);
        car.setLicensePlate("TEST");
        car.setModel("testModel");
        car.setBrand("testBrand");
        car.setDescription("testDescription");
        car.setPricePerDay(BigDecimal.valueOf(100));
        car.setOwner(owner);
        Car savedCar = carRepository.save(car);

        String token = jwtUtil.generateToken(renter.getEmail());

        Rental rental = new Rental();
        rental.setStatus(RentalStatus.ACTIVE);
        rental.setUser(renter);
        rental.setTotalCost(BigDecimal.TEN);
        rental.setStartDate(LocalDateTime.now().plusDays(1));
        rental.setEndDate(LocalDateTime.now().plusDays(3));
        rental.setCar(savedCar);

        Rental savedRental = rentalRepository.save(rental);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .patch("/api/rentals/return/"+savedRental.getId())
                .then()
                .statusCode(HttpStatus.OK.value());

        Rental updatedRental = rentalRepository.findById(savedRental.getId()).orElseThrow();
        Assertions.assertEquals(RentalStatus.COMPLETED, updatedRental.getStatus());
    }

    @Test
    void returnCarShouldThrowRentalNotActiveExceptionBadRequest(){
        User owner = new User();
        owner.setEmail("test@test.com");
        owner.setPassword("password123");
        owner.setRole(Role.USER);
        owner.setFirstName("TestFirstName");
        owner.setLastName("TestLastName");
        owner.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(owner);

        User renter = new User();
        renter.setEmail("test2@test.com");
        renter.setAccountBalance(BigDecimal.valueOf(1000));
        renter.setPassword("password123");
        renter.setRole(Role.USER);
        renter.setFirstName("TestFirstName2");
        renter.setLastName("TestLastName2");
        userRepository.save(renter);

        Car car = new Car();
        car.setStatus(CarStatus.AVAILABLE);
        car.setLicensePlate("TEST");
        car.setModel("testModel");
        car.setBrand("testBrand");
        car.setDescription("testDescription");
        car.setPricePerDay(BigDecimal.valueOf(100));
        car.setOwner(owner);
        Car savedCar = carRepository.save(car);

        String token = jwtUtil.generateToken(renter.getEmail());

        Rental rental = new Rental();
        rental.setStatus(RentalStatus.COMPLETED);
        rental.setUser(renter);
        rental.setTotalCost(BigDecimal.TEN);
        rental.setStartDate(LocalDateTime.now().plusDays(1));
        rental.setEndDate(LocalDateTime.now().plusDays(3));
        rental.setCar(savedCar);

        Rental savedRental = rentalRepository.save(rental);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .patch("/api/rentals/return/"+savedRental.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

    }

    @Test
    void getUserRentalsShouldSuccessfullyReturnUserRentals(){
        User owner = new User();
        owner.setEmail("test@test.com");
        owner.setPassword("password123");
        owner.setRole(Role.USER);
        owner.setFirstName("TestFirstName");
        owner.setLastName("TestLastName");
        owner.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(owner);

        User renter = new User();
        renter.setEmail("test2@test.com");
        renter.setAccountBalance(BigDecimal.valueOf(1000));
        renter.setPassword("password123");
        renter.setRole(Role.USER);
        renter.setFirstName("TestFirstName2");
        renter.setLastName("TestLastName2");
        userRepository.save(renter);

        Car car1 = new Car();
        car1.setStatus(CarStatus.AVAILABLE);
        car1.setLicensePlate("TEST1");
        car1.setModel("testModel1");
        car1.setBrand("testBrand1");
        car1.setDescription("testDescription1");
        car1.setPricePerDay(BigDecimal.valueOf(100));
        car1.setOwner(owner);
        Car savedCar1 = carRepository.save(car1);

        Car car2 = new Car();
        car2.setStatus(CarStatus.AVAILABLE);
        car2.setLicensePlate("TEST2");
        car2.setModel("testModel2");
        car2.setBrand("testBrand2");
        car2.setDescription("testDescription2");
        car2.setPricePerDay(BigDecimal.valueOf(150));
        car2.setOwner(owner);
        Car savedCar2 = carRepository.save(car2);

        String token = jwtUtil.generateToken(renter.getEmail());

        Rental rental1 = new Rental();
        rental1.setStatus(RentalStatus.ACTIVE);
        rental1.setUser(renter);
        rental1.setTotalCost(BigDecimal.TEN);
        rental1.setStartDate(LocalDateTime.now().plusDays(1));
        rental1.setEndDate(LocalDateTime.now().plusDays(3));
        rental1.setCar(savedCar1);
        rentalRepository.save(rental1);

        Rental rental2 = new Rental();
        rental2.setStatus(RentalStatus.ACTIVE);
        rental2.setUser(renter);
        rental2.setTotalCost(BigDecimal.TEN);
        rental2.setStartDate(LocalDateTime.now().plusDays(4));
        rental2.setEndDate(LocalDateTime.now().plusDays(6));
        rental2.setCar(savedCar2);
        rentalRepository.save(rental2);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/rentals/my-rentals")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.size()", org.hamcrest.Matchers.equalTo(2));
    }
}
