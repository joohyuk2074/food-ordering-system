package com.food.ordering.system.application.handler.domain.valueobject;

import com.food.ordering.system.domain.valueobject.BaseId;
import java.util.UUID;

public class RestaurantId extends BaseId<UUID> {
    public RestaurantId(UUID value) {
        super(value);
    }
}
