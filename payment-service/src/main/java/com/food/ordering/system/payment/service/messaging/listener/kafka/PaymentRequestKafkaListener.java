package com.food.ordering.system.payment.service.messaging.listener.kafka;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentRequestKafkaListener implements KafkaConsumer<PaymentRequestAvroModel> {

    private final PaymentRequestMessageListener paymentRequestMessageListener;
    private final PaymentMessagingDataMapper paymentMessagingDataMapper;

    public PaymentRequestKafkaListener(
        PaymentRequestMessageListener paymentRequestMessageListener,
        PaymentMessagingDataMapper paymentMessagingDataMapper
    ) {
        this.paymentRequestMessageListener = paymentRequestMessageListener;
        this.paymentMessagingDataMapper = paymentMessagingDataMapper;
    }

    @Override
    @KafkaListener(
        id = "${kafka-consumer-config.payment-consumer-group-id}",
        topics = "${payment-service.payment-request-topic-name}"
    )
    public void receive(
        @Payload List<PaymentRequestAvroModel> messages,
        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
        @Header(KafkaHeaders.OFFSET) List<Long> offset
    ) {
        log.info("{} number of payment requests received with keys: {}, partitions: {} and offsets: {}",
            messages.size(),
            keys.toString(),
            partitions.toString(),
            offset.toString()
        );

        messages.forEach(paymentRequestAvroModel -> {
            if (PaymentOrderStatus.PENDING == paymentRequestAvroModel.getPaymentOrderStatus()) {
                log.info("Processing payment for order id: {}", paymentRequestAvroModel.getOrderId());
                PaymentRequest paymentRequest = paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(paymentRequestAvroModel);
                paymentRequestMessageListener.completedPayment(paymentRequest);
            } else if (PaymentOrderStatus.CANCELLED == paymentRequestAvroModel.getPaymentOrderStatus()) {
                log.info("Cancelling payment for order id: {}", paymentRequestAvroModel.getOrderId());
                PaymentRequest paymentRequest = paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(paymentRequestAvroModel);
                paymentRequestMessageListener.cancelPayment(paymentRequest);
            }
        });
    }
}