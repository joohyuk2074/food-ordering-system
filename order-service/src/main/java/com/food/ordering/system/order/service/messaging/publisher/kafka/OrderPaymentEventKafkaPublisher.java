package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.dataaccess.order.adapter.OrderRepositoryImpl;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderPaymentEventKafkaPublisher implements PaymentRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;
    private final ObjectMapper objectMapper;
    private final OrderRepositoryImpl orderRepositoryImpl;

    public OrderPaymentEventKafkaPublisher(
        OrderMessagingDataMapper orderMessagingDataMapper,
        KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
        OrderServiceConfigData orderServiceConfigData,
        KafkaMessageHelper kafkaMessageHelper,
        ObjectMapper objectMapper,
        OrderRepositoryImpl orderRepositoryImpl) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
        this.objectMapper = objectMapper;
        this.orderRepositoryImpl = orderRepositoryImpl;
    }

    @Override
    public void publish(
        OrderPaymentOutboxMessage orderPaymentOutboxMessage,
        BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback
    ) {
        OrderPaymentEventPayload orderPaymentEventPayload
            = getOrderPaymentEventPayload(orderPaymentOutboxMessage.getPayload());

        String sagaId = orderPaymentOutboxMessage.getSagaId().toString();

        log.info("Received OrderPaymentOutboxMessage for order id: {} and saga id: {}",
            orderPaymentEventPayload.getOrderId(),
            sagaId
        );

        try {
            PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper
                .orderPaymentEventToPaymentRequestAvroModel(sagaId, orderPaymentEventPayload);

            BiConsumer<SendResult<String, PaymentRequestAvroModel>, Throwable> kafkaCallback
                = kafkaMessageHelper.getKafkaCallback(
                orderServiceConfigData.getPaymentResponseTopicName(),
                paymentRequestAvroModel,
                orderPaymentOutboxMessage,
                outboxCallback,
                orderPaymentEventPayload.getOrderId(),
                "PaymentRequestAvroModel"
            );

            kafkaProducer.send(
                orderServiceConfigData.getPaymentRequestTopicName(),
                sagaId,
                paymentRequestAvroModel,
                kafkaCallback
            );

            log.info("OrderPaymentEventPayload sent to kafka for order id: {} and saga id: {}",
                orderPaymentEventPayload.getOrderId(),
                sagaId
            );
        } catch (Exception e) {
            log.error("Error while sending OrderPaymentEventPayload" +
                    " to kafka with order id: {} and saga id: {}, error: {}",
                orderPaymentEventPayload.getOrderId(), sagaId, e.getMessage(), e);
        }
    }

    private OrderPaymentEventPayload getOrderPaymentEventPayload(String payload) {
        try {
            return objectMapper.readValue(payload, OrderPaymentEventPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Could not read OrderPaymentEventPayload object!", e);
            throw new OrderDomainException("Could not read OrderPaymentPayload object!", e);
        }
    }
}
