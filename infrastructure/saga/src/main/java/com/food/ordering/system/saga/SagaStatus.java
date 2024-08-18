package com.food.ordering.system.saga;

public enum SagaStatus {

    START, FAILED, SUCCEEDED, PROCESSING, COMPENSATING, COMPENSATED
}
