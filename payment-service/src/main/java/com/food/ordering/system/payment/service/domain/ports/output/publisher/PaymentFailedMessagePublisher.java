package com.food.ordering.system.payment.service.domain.ports.output.publisher;

import com.food.ordering.system.application.handler.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;

public interface PaymentFailedMessagePublisher extends DomainEventPublisher<PaymentFailedEvent> {

}