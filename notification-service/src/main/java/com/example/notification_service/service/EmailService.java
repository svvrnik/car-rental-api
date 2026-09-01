package com.example.notification_service.service;

import com.example.notification_service.event.RentalCreatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final MailSender mailSender;
    private final String companyEmail;

    public EmailService(MailSender mailSender, @Value("${app.company.email}") String companyEmail) {
        this.mailSender = mailSender;
        this.companyEmail = companyEmail;
    }

    @RabbitListener(queues = "emailQueue")
    public void listen(RentalCreatedEvent rentalCreatedEvent){
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(this.companyEmail);
        mail.setTo(rentalCreatedEvent.getEmail());
        mail.setSubject("Car Rental Confirmation: " + rentalCreatedEvent.getCarBrand() + " " + rentalCreatedEvent.getCarModel());
        mail.setText("Hi " + rentalCreatedEvent.getEmail() + ",\n\n" +
                "Thank you for renting a car with us! Here are the details of your reservation:\n\n" +
                "Car: " + rentalCreatedEvent.getCarBrand() + " " + rentalCreatedEvent.getCarModel() + " (License plate: " + rentalCreatedEvent.getLicensePlate() + ")\n" +
                "Start date: " + rentalCreatedEvent.getStartDate().toLocalDate() + "\n" +
                "End date: " + rentalCreatedEvent.getEndDate().toLocalDate() + "\n" +
                "Total cost: " + rentalCreatedEvent.getPriceForRentPeriod() + " PLN\n\n" +
                "Have a safe trip!\nYour Car Rental Team");

        mailSender.send(mail);
    }
}
