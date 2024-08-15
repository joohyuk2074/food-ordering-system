package com.food.ordering.system.application.handler.domain.valueobject;

import com.food.ordering.system.domain.valueobject.BaseId;
import java.util.UUID;

public class OrderId extends BaseId<UUID> {

    public OrderId(UUID value) {
        super(value);
    }
}
