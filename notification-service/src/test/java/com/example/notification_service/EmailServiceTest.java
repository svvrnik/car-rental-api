package com.example.notification_service;

import com.example.notification_service.entity.ProcessedEvent;
import com.example.notification_service.event.RentalCreatedEvent;
import com.example.notification_service.repository.ProcessedEventRepository;
import com.example.notification_service.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cglib.core.Local;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
    @Mock
    private MailSender mailSender;
    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private EmailService emailService;

    @Test
    void listenShouldProcessEventAndSendEmailIfEventIsNew(){
        RentalCreatedEvent rentalCreatedEvent = new RentalCreatedEvent();
        rentalCreatedEvent.setCarBrand("testBrand");
        rentalCreatedEvent.setCarId(1L);
        rentalCreatedEvent.setCarModel("testModel");
        rentalCreatedEvent.setCreatedAt(LocalDateTime.now());
        rentalCreatedEvent.setEmail("testEmail@testEmail.com");
        rentalCreatedEvent.setEndDate(LocalDateTime.now().plusDays(2));
        rentalCreatedEvent.setLicensePlate("testLicensePlate");
        rentalCreatedEvent.setEventId("testId");
        rentalCreatedEvent.setPriceForRentPeriod(BigDecimal.valueOf(300));
        rentalCreatedEvent.setStartDate(LocalDateTime.now());

        Mockito.when(processedEventRepository.existsById("testId")).thenReturn(false);

        emailService.listen(rentalCreatedEvent);

        Mockito.verify(processedEventRepository, Mockito.times(1)).save(Mockito.any(ProcessedEvent.class));
        Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(SimpleMailMessage.class));
    }
    @Test
    void listenShouldNotSendEmailIfEventIsDuplicate(){
        RentalCreatedEvent rentalCreatedEvent = new RentalCreatedEvent();
        rentalCreatedEvent.setCarBrand("testBrand");
        rentalCreatedEvent.setCarId(1L);
        rentalCreatedEvent.setCarModel("testModel");
        rentalCreatedEvent.setCreatedAt(LocalDateTime.now());
        rentalCreatedEvent.setEmail("testEmail@testEmail.com");
        rentalCreatedEvent.setEndDate(LocalDateTime.now().plusDays(2));
        rentalCreatedEvent.setLicensePlate("testLicensePlate");
        rentalCreatedEvent.setEventId("testId");
        rentalCreatedEvent.setPriceForRentPeriod(BigDecimal.valueOf(300));
        rentalCreatedEvent.setStartDate(LocalDateTime.now());

        Mockito.when(processedEventRepository.existsById("testId")).thenReturn(true);

        emailService.listen(rentalCreatedEvent);

        Mockito.verify(processedEventRepository, Mockito.never()).save(Mockito.any(ProcessedEvent.class));
        Mockito.verify(mailSender, Mockito.never()).send(Mockito.any(SimpleMailMessage.class));
    }
}
