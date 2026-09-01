package com.example.car_rental_api;

import com.example.car_rental_api.outbox.OutboxEvent;
import com.example.car_rental_api.outbox.OutboxRepository;
import com.example.car_rental_api.outbox.OutboxScheduler;
import com.example.car_rental_api.outbox.OutboxStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class OutboxSchedulerTest {
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private OutboxRepository outboxRepository;
    @InjectMocks
    private OutboxScheduler outboxScheduler;

    @Test
    void processOutboxEventsShouldPublishPendingEvents(){
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventId("eventId1");
        outboxEvent.setType("RENTAL_CREATED");
        outboxEvent.setPayload("{\"rentalId\":1}");
        outboxEvent.setStatus(OutboxStatus.PENDING);

        Mockito.when(outboxRepository.findByStatus(OutboxStatus.PENDING)).thenReturn(List.of(outboxEvent));

        outboxScheduler.processOutboxEvents();

        Mockito.verify(rabbitTemplate, Mockito.times(1)).send(Mockito.eq("car-rental.events"), Mockito.eq("rental.created"), Mockito.any());

        Assertions.assertEquals(OutboxStatus.PUBLISHED, outboxEvent.getStatus());

        Mockito.verify(outboxRepository, Mockito.times(1)).save(Mockito.any(OutboxEvent.class));
    }

    @Test
    void processOutboxEventsShouldKeepEventPendingWhenPublishingFails(){
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventId("eventId1");
        outboxEvent.setType("RENTAL_CREATED");
        outboxEvent.setPayload("{\"rentalId\":1}");
        outboxEvent.setStatus(OutboxStatus.PENDING);

        Mockito.when(outboxRepository.findByStatus(OutboxStatus.PENDING)).thenReturn(List.of(outboxEvent));

        Mockito.doThrow(new RuntimeException("Example exception")).when(rabbitTemplate).send(Mockito.eq("car-rental.events"), Mockito.eq("rental.created"), Mockito.any());

        outboxScheduler.processOutboxEvents();

        Mockito.verify(rabbitTemplate, Mockito.times(1)).send(Mockito.eq("car-rental.events"), Mockito.eq("rental.created"), Mockito.any());

        Assertions.assertEquals(OutboxStatus.PENDING, outboxEvent.getStatus());

        Mockito.verify(outboxRepository, Mockito.never()).save(Mockito.any(OutboxEvent.class));

    }
}
