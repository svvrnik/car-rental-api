package com.example.car_rental_api.integration;

import static io.restassured.RestAssured.given;
import com.example.car_rental_api.car.Car;
import com.example.car_rental_api.car.CarRepository;
import com.example.car_rental_api.car.CarStatus;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarControllerTest {

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
        transactionRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getAvailableCarsShouldSuccessfullyReturnAvailableCars(){
        User owner = new User();
        owner.setEmail("test@test.com");
        owner.setPassword("password123");
        owner.setRole(Role.USER);
        owner.setFirstName("testFirstName");
        owner.setLastName("testLastName");
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
        carRepository.save(car);

        String token = jwtUtil.generateToken(owner.getEmail());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/cars/available")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.size()", org.hamcrest.Matchers.equalTo(1));
    }

    @Test
    void getUserCarsShouldSuccessfullyReturnUserCars(){
        User owner = new User();
        owner.setEmail("test@test.com");
        owner.setPassword("password123");
        owner.setRole(Role.USER);
        owner.setFirstName("testFirstName");
        owner.setLastName("testLastName");
        owner.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(owner);

        Car car = new Car();
        car.setStatus(CarStatus.PENDING);
        car.setLicensePlate("TEST");
        car.setModel("testModel");
        car.setBrand("testBrand");
        car.setDescription("testDescription");
        car.setPricePerDay(BigDecimal.valueOf(100));
        car.setOwner(owner);
        carRepository.save(car);

        String token = jwtUtil.generateToken(owner.getEmail());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/cars/my-cars")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.size()", org.hamcrest.Matchers.equalTo(1));
    }

    @Test
    void withdrawCarShouldSuccessfullyWithdrawCar(){
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

        String token = jwtUtil.generateToken(owner.getEmail());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .patch("/api/cars/withdraw/" + savedCar.getId())
                .then()
                .statusCode(HttpStatus.OK.value());

        Car updatedCar = carRepository.findById(savedCar.getId()).orElseThrow();
        Assertions.assertEquals(CarStatus.UNAVAILABLE, updatedCar.getStatus());
    }

    @Test
    void approveCarShouldSuccessfullyApproveCar(){
        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPassword("password123");
        admin.setRole(Role.ADMIN);
        admin.setFirstName("AdminFirstName");
        admin.setLastName("AdminLastName");
        admin.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(admin);

        User owner = new User();
        owner.setEmail("test@test.com");
        owner.setPassword("password123");
        owner.setRole(Role.USER);
        owner.setFirstName("TestFirstName");
        owner.setLastName("TestLastName");
        owner.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(owner);

        Car car = new Car();
        car.setStatus(CarStatus.PENDING);
        car.setLicensePlate("TEST");
        car.setModel("testModel");
        car.setBrand("testBrand");
        car.setDescription("testDescription");
        car.setPricePerDay(BigDecimal.valueOf(100));
        car.setOwner(owner);
        Car savedCar = carRepository.save(car);

        String token = jwtUtil.generateToken(admin.getEmail());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .patch("/api/cars/approve/" + savedCar.getId())
                .then()
                .statusCode(HttpStatus.OK.value());

        Car updatedCar = carRepository.findById(savedCar.getId()).orElseThrow();
        Assertions.assertEquals(CarStatus.AVAILABLE, updatedCar.getStatus());
    }

    @Test
    void rejectCarShouldSuccessfullyRejectCar(){
        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPassword("password123");
        admin.setRole(Role.ADMIN);
        admin.setFirstName("AdminFirstName");
        admin.setLastName("AdminLastName");
        admin.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(admin);

        User owner = new User();
        owner.setEmail("test@test.com");
        owner.setPassword("password123");
        owner.setRole(Role.USER);
        owner.setFirstName("TestFirstName");
        owner.setLastName("TestLastName");
        owner.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(owner);

        Car car = new Car();
        car.setStatus(CarStatus.PENDING);
        car.setLicensePlate("TEST");
        car.setModel("testModel");
        car.setBrand("testBrand");
        car.setDescription("testDescription");
        car.setPricePerDay(BigDecimal.valueOf(100));
        car.setOwner(owner);
        Car savedCar = carRepository.save(car);

        String token = jwtUtil.generateToken(admin.getEmail());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .patch("/api/cars/reject/" + savedCar.getId())
                .then()
                .statusCode(HttpStatus.OK.value());

        Car updatedCar = carRepository.findById(savedCar.getId()).orElseThrow();
        Assertions.assertEquals(CarStatus.REJECTED, updatedCar.getStatus());
    }

    @Test
    void withdrawCarShouldThrowUserIsNotCarOwnerExceptionForbidden(){
        User owner = new User();
        owner.setEmail("test@test.com");
        owner.setPassword("password123");
        owner.setRole(Role.USER);
        owner.setFirstName("TestFirstName");
        owner.setLastName("TestLastName");
        owner.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(owner);

        User stranger = new User();
        stranger.setEmail("test2@test.com");
        stranger.setPassword("password123");
        stranger.setRole(Role.USER);
        stranger.setFirstName("TestFirstName2");
        stranger.setLastName("TestLastName2");
        stranger.setAccountBalance(BigDecimal.valueOf(1000));
        userRepository.save(stranger);

        Car car = new Car();
        car.setStatus(CarStatus.AVAILABLE);
        car.setLicensePlate("TEST");
        car.setModel("testModel");
        car.setBrand("testBrand");
        car.setDescription("testDescription");
        car.setPricePerDay(BigDecimal.valueOf(100));
        car.setOwner(owner);
        Car savedCar = carRepository.save(car);

        String token = jwtUtil.generateToken(stranger.getEmail());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .patch("/api/cars/withdraw/" + savedCar.getId())
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void addCarShouldSuccessfullyAddCar(){
        User owner = new User();
        owner.setEmail("test@test.com");
        owner.setPassword("password123");
        owner.setRole(Role.USER);
        owner.setFirstName("TestFirstName");
        owner.setLastName("TestLastName");
        owner.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(owner);

        String token = jwtUtil.generateToken(owner.getEmail());

        String carJson = "{" +
                "\"brand\":\"testBrand\"," +
                "\"model\":\"testModel\"," +
                "\"licensePlate\":\"TEST-PLATE\"," +
                "\"pricePerDay\":100," +
                "\"description\":\"testDescription\"" +
                "}";

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.MULTIPART)
                .multiPart("car", carJson, "application/json")
                .when()
                .post("/api/cars/add")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        Assertions.assertEquals(1, carRepository.count());
    }

    @Test
    void addCarShouldThrowDuplicateLicensePlateExceptionBadRequest(){
        User owner = new User();
        owner.setEmail("test@test.com");
        owner.setPassword("password123");
        owner.setRole(Role.USER);
        owner.setFirstName("TestFirstName");
        owner.setLastName("TestLastName");
        owner.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(owner);

        Car existingCar = new Car();
        existingCar.setStatus(CarStatus.AVAILABLE);
        existingCar.setLicensePlate("DUPLICATE");
        existingCar.setModel("testModel");
        existingCar.setBrand("testBrand");
        existingCar.setDescription("testDescription");
        existingCar.setPricePerDay(BigDecimal.valueOf(100));
        existingCar.setOwner(owner);
        carRepository.save(existingCar);

        String token = jwtUtil.generateToken(owner.getEmail());

        String carJson = "{" +
                "\"brand\":\"testBrand\"," +
                "\"model\":\"testModel\"," +
                "\"licensePlate\":\"DUPLICATE\"," +
                "\"pricePerDay\":100," +
                "\"description\":\"testDescription\"" +
                "}";

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.MULTIPART)
                .multiPart("car", carJson, "application/json")
                .when()
                .post("/api/cars/add")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }
}