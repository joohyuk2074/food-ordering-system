package com.food.ordering.system.order.service.domain.entity;


import com.food.ordering.system.application.handler.domain.entity.AggregateRoot;
import com.food.ordering.system.application.handler.domain.valueobject.CustomerId;

public class Customer extends AggregateRoot<CustomerId> {

    public Customer(CustomerId customerId) {
        super.setId(customerId);
    }
}
