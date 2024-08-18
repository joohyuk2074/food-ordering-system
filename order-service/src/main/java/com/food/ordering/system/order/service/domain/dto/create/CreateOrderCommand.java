package com.food.ordering.system.order.service.domain.dto.create;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderCommand {

    @NotNull
    private UUID customerId;

    @NotNull
    private UUID restaurantId;

    @NotNull
    private BigDecimal price;

    @NotNull
    private List<OrderItem> items;

    @NotNull
    private OrderAddress address;
}
