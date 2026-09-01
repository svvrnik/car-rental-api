package com.example.car_rental_api.outbox;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class OutboxScheduler {
    private final RabbitTemplate rabbitTemplate;
    private final OutboxRepository outboxRepository;

    public OutboxScheduler(RabbitTemplate rabbitTemplate, OutboxRepository outboxRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.outboxRepository = outboxRepository;
    }

    private String mapRoutingKey(OutboxEvent event){
        return event.getType().toLowerCase().replace('_','.');
    }

    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents(){
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatus(OutboxStatus.PENDING);

        for(OutboxEvent event: pendingEvents){
            try{
                Message jsonMess = MessageBuilder
                        .withBody(event.getPayload().getBytes(StandardCharsets.UTF_8))
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .build();
                rabbitTemplate.send("car-rental.events", mapRoutingKey(event), jsonMess);

                event.setStatus(OutboxStatus.PUBLISHED);
                outboxRepository.save(event);
            }catch(Exception e){
                //event still pending
            }
        }
    }
}
