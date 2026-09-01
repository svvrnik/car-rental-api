package com.example.notification_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue emailQueue(){
        return new Queue("emailQueue", true);
    }
    @Bean
    public MessageConverter messageConverter(){
        return new JacksonJsonMessageConverter();
    }
    @Bean
    public TopicExchange carRentalExchange(){
        return new TopicExchange("car-rental.events");
    }
    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange carRentalExchange){
        return
                BindingBuilder
                        .bind(emailQueue)
                        .to(carRentalExchange)
                        .with("rental.created");
    }
}
