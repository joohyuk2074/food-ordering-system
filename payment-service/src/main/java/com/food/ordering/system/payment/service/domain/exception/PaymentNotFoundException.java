package com.food.ordering.system.payment.service.domain.exception;

import com.food.ordering.system.application.handler.domain.exception.DomainException;

public class PaymentNotFoundException extends DomainException {

    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentNotFoundException(String message) {
        super(message);
    }
}
