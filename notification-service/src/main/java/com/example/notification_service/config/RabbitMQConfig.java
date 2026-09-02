package com.example.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue deadLetterQueue(){
        return new Queue("emailQueue.dlq", true);
    }
    @Bean
    public Queue emailQueue(){
        return QueueBuilder
                .durable("emailQueue")
                .withArgument("x-dead-letter-exchange","")
                .withArgument("x-dead-letter-routing-key", "emailQueue.dlq")
                .build();
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
