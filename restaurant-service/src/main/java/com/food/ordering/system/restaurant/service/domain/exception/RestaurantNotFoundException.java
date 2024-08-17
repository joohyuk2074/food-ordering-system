package com.food.ordering.system.restaurant.service.domain.exception;

import com.food.ordering.system.application.handler.domain.exception.DomainException;

public class RestaurantNotFoundException extends DomainException {

    public RestaurantNotFoundException(String message) {
        super(message);
    }

    public RestaurantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
