package com.example.notification_service.service;

import com.example.notification_service.dto.EmailNotificationDto;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final MailSender mailSender;

    public EmailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @RabbitListener(queues = "emailQueue")
    public void listen(EmailNotificationDto emailNotificationDto){
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(emailNotificationDto.getFrom());
        mail.setTo(emailNotificationDto.getTo());
        mail.setSubject(emailNotificationDto.getSubject());
        mail.setText(emailNotificationDto.getMessage());

        mailSender.send(mail);
    }
}
