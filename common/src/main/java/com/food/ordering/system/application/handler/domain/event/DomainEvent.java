package com.food.ordering.system.application.handler.domain.event;

public interface DomainEvent<T> {

    void fire();
}
