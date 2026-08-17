package com.example.car_rental_api.integration;

import static io.restassured.RestAssured.given;

import com.example.car_rental_api.storage.FileStorageService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.example.car_rental_api.transaction.TransactionRepository;
import com.example.car_rental_api.user.Role;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import com.example.car_rental_api.user.dto.FundRequestDto;
import com.example.car_rental_api.user.dto.UserRegisterDto;
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
import org.testcontainers.junit.jupiter.Container;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @LocalServerPort
    private Integer port;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Container
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
    UserRepository userRepository;

    @Autowired
    JWTUtil jwtUtil;

    @Autowired
    TransactionRepository transactionRepository;

    @BeforeEach
    void setUp(){
        RestAssured.baseURI = "http://localhost:"+port;
        transactionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerUserShouldSuccessfullyRegisterUser(){
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setEmail("test@test.com");
        registerDto.setPassword("testPassword");
        registerDto.setFirstName("testFirstName");
        registerDto.setLastName("testFirstName");

        given()
                .contentType(ContentType.JSON)
                .body(registerDto)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        Assertions.assertEquals(1, userRepository.count());
    }

    @Test
    void registerUserShouldThrowEmailAlreadyInUseException(){
        User existingUser = new User();
        existingUser.setEmail("test@test.com");
        existingUser.setPassword("testPassword");
        existingUser.setRole(Role.USER);
        existingUser.setFirstName("testFirstName");
        existingUser.setLastName("testLastName");
        existingUser.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(existingUser);

        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setEmail("test@test.com");
        registerDto.setPassword("testPassword");
        registerDto.setFirstName("testFirstName");
        registerDto.setLastName("testFirsTName");

        given()
                .contentType(ContentType.JSON)
                .body(registerDto)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }


    @Test
    void addFundsShouldSuccessfullyAddFunds(){
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("testPassword");
        user.setRole(Role.USER);
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        FundRequestDto fundRequestDto = new FundRequestDto();
        fundRequestDto.setAmount(BigDecimal.valueOf(500));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(fundRequestDto)
                .when()
                .patch("/api/users/add-funds")
                .then()
                .statusCode(HttpStatus.OK.value());

        User updatedUser = userRepository.findByEmail("test@test.com").orElseThrow();

        Assertions.assertEquals(0, BigDecimal.valueOf(500).compareTo(updatedUser.getAccountBalance()));
    }


    @Test
    void withdrawFundsShouldSuccessfullyWithdrawFunds(){
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("testPassword");
        user.setRole(Role.USER);
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setAccountBalance(BigDecimal.valueOf(1000));
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        FundRequestDto fundRequestDto = new FundRequestDto();
        fundRequestDto.setAmount(BigDecimal.valueOf(300));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(fundRequestDto)
                .when()
                .patch("/api/users/withdraw-funds")
                .then()
                .statusCode(HttpStatus.OK.value());

        User updatedUser = userRepository.findByEmail("test@test.com").orElseThrow();

        Assertions.assertEquals(0, BigDecimal.valueOf(700).compareTo(updatedUser.getAccountBalance()));
    }

    @Test
    void withdrawFundsShouldThrowExceptionWhenInsufficientFunds(){
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("testPassword");
        user.setRole(Role.USER);
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setAccountBalance(BigDecimal.ZERO);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        FundRequestDto fundRequestDto = new FundRequestDto();
        fundRequestDto.setAmount(BigDecimal.valueOf(500));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(fundRequestDto)
                .when()
                .patch("/api/users/withdraw-funds")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
