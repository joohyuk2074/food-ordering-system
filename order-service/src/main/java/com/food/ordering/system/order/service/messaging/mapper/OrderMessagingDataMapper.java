package com.food.ordering.system.order.service.messaging.mapper;

import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.Product;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantOrderStatus;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import java.util.UUID;
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType;
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
            .setRestaurantOrderStatus(RestaurantOrderStatus.valueOf(order.getOrderStatus().name()))
            .setProducts(order.getItems().stream().map(orderItem -> Product.newBuilder()
                .setId(orderItem.getProduct().getId().getValue().toString())
                .setQuantity(orderItem.getQuantity())
                .build()).toList())
            .setPrice(order.getPrice().getAmount())
            .setCreatedAt(orderPaidEvent.getCreatedAt().toInstant())
            .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
            .build();
    }

    public PaymentResponse paymentResponseAvroModelToPaymentResponse(PaymentResponseAvroModel paymentResponseAvroModel) {
        return PaymentResponse.builder()
            .id(paymentResponseAvroModel.getId())
            .sagaId(paymentResponseAvroModel.getSagaId())
            .paymentId(paymentResponseAvroModel.getPaymentId())
            .customerId(paymentResponseAvroModel.getCustomerId())
            .orderId(paymentResponseAvroModel.getOrderId())
            .price(paymentResponseAvroModel.getPrice())
            .createdAt(paymentResponseAvroModel.getCreatedAt())
            .paymentStatus(PaymentStatus.valueOf(paymentResponseAvroModel.getPaymentStatus().name()))
            .failureMessages(paymentResponseAvroModel.getFailureMessages())
            .build();
    }

    public RestaurantApprovalResponse approvalResponseAvroModelToApprovalResponse(
        RestaurantApprovalResponseAvroModel approvalResponseAvroModel
    ) {
        return RestaurantApprovalResponse.builder()
            .id(approvalResponseAvroModel.getId())
            .sagaId(approvalResponseAvroModel.getSagaId())
            .restaurantId(approvalResponseAvroModel.getRestaurantId())
            .orderId(approvalResponseAvroModel.getOrderId())
            .createdAt(approvalResponseAvroModel.getCreatedAt())
            .orderApprovalStatus(OrderApprovalStatus.valueOf(approvalResponseAvroModel.getOrderApprovalStatus().name()))
            .failureMessages(approvalResponseAvroModel.getFailureMessages())
            .build();
    }

    public PaymentRequestAvroModel orderPaymentEventToPaymentRequestAvroModel(
        String sagaId,
        OrderPaymentEventPayload orderPaymentEventPayload
    ) {
        return PaymentRequestAvroModel.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSagaId(sagaId)
            .setCustomerId(orderPaymentEventPayload.getCustomerId())
            .setOrderId(orderPaymentEventPayload.getOrderId())
            .setPrice(orderPaymentEventPayload.getPrice())
            .setCreatedAt(orderPaymentEventPayload.getCreatedAt().toInstant())
            .setPaymentOrderStatus(PaymentOrderStatus.valueOf(orderPaymentEventPayload.getPaymentOrderStatus()))
            .build();
    }
}
