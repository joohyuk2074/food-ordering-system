package com.food.ordering.system.order.service.domain.valueobject;

import com.food.ordering.system.application.handler.domain.valueobject.BaseId;
import java.util.UUID;

public class TrackingId extends BaseId<UUID> {
    public TrackingId(UUID value) {
        super(value);
    }
}
