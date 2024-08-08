package com.food.ordering.system.order.service.messaging.mapper;

import static com.food.ordering.system.kafka.order.avro.model.RestaurantOrderStatus.PAID;
import static com.food.ordering.system.kafka.order.avro.model.RestaurantOrderStatus.valueOf;

import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.Product;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderMessagingDataMapper {

    public PaymentRequestAvroModel orderCreatedEventToPaymentRequestAvroModel(OrderCreatedEvent orderCreatedEvent) {
        Order order = orderCreatedEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSagaId("")
            .setCustomerId(order.getCustomerId().getValue().toString())
            .setOrderId(order.getId().getValue().toString())
            .setPrice(order.getPrice().getAmount())
            .setCreatedAt(orderCreatedEvent.getCreatedAt().toInstant())
            .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
            .build();
    }

    public PaymentRequestAvroModel orderCancelledEventToPaymentRequestAvroModel(OrderCancelledEvent orderCancelledEvent) {
        Order order = orderCancelledEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSagaId("")
            .setCustomerId(order.getCustomerId().getValue().toString())
            .setOrderId(order.getId().getValue().toString())
            .setPrice(order.getPrice().getAmount())
            .setCreatedAt(orderCancelledEvent.getCreatedAt().toInstant())
            .setPaymentOrderStatus(PaymentOrderStatus.CANCELLED)
            .build();
    }

    public RestaurantApprovalRequestAvroModel orderPaidEventToRestaurantApprovalRequestAvroModel(OrderPaidEvent orderPaidEvent) {
        Order order = orderPaidEvent.getOrder();
        return RestaurantApprovalRequestAvroModel.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSagaId("")
            .setOrderId(order.getId().getValue().toString())
            .setRestaurantId(order.getId().getValue().toString())
            .setRestaurantOrderStatus(valueOf(order.getOrderStatus().name()))
            .setProducts(order.getItems().stream().map(orderItem -> Product.newBuilder()
                .setId(orderItem.getProduct().getId().getValue().toString())
                .setQuantity(orderItem.getQuantity())
                .build()).toList())
            .setPrice(order.getPrice().getAmount())
            .setCreatedAt(orderPaidEvent.getCreatedAt().toInstant())
            .setRestaurantOrderStatus(PAID)
            .build();
    }
}
