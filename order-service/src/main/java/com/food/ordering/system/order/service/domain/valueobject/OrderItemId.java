package com.food.ordering.system.order.service.domain.valueobject;


import com.food.ordering.system.application.handler.domain.valueobject.BaseId;

public class OrderItemId extends BaseId<Long> {

    public OrderItemId(Long value) {
        super(value);
    }
}
