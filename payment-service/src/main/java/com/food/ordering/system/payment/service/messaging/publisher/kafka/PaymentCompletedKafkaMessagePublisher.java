package com.food.ordering.system.payment.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.payment.service.domain.config.PaymentServiceConfigData;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.ports.output.publisher.PaymentCompletedMessagePublisher;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentCompletedKafkaMessagePublisher implements PaymentCompletedMessagePublisher {

    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    private final KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer;
    private final PaymentServiceConfigData paymentServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    public PaymentCompletedKafkaMessagePublisher(
        PaymentMessagingDataMapper paymentMessagingDataMapper,
        KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer,
        PaymentServiceConfigData paymentServiceConfigData,
        KafkaMessageHelper kafkaMessageHelper
    ) {
        this.paymentMessagingDataMapper = paymentMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.paymentServiceConfigData = paymentServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(PaymentCompletedEvent domainEvent) {
        String orderId = domainEvent.getPayment().getOrderId().getValue().toString();

        log.info("Received PaymentCompletedEvent for order id: {}", orderId);

        try {
            PaymentResponseAvroModel paymentResponseAvroModel
                = paymentMessagingDataMapper.paymentCompletedEventToPaymentResponseAvroModel(domainEvent);

            BiConsumer<SendResult<String, PaymentResponseAvroModel>, Throwable> kafkaCallback = kafkaMessageHelper.getKafkaCallback(
                paymentServiceConfigData.getPaymentResponseTopicName(),
                paymentResponseAvroModel,
                orderId
            );

            kafkaProducer.send(
                paymentServiceConfigData.getPaymentResponseTopicName(),
                orderId,
                paymentResponseAvroModel,
                kafkaCallback
            );

            log.info("PaymentResponseAvroModel sent to kafka for order id: {}", orderId);
        } catch (Exception e) {
            log.info("Error while sending PaymentResponseAvroModel message"
                + " to kafka with order id: {}, error: {}", orderId, e.getMessage());
        }
    }
}