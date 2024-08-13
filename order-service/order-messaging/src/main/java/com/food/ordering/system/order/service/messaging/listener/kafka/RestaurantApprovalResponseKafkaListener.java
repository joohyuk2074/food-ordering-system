package com.food.ordering.system.order.service.messaging.listener.kafka;

import static com.food.ordering.system.order.service.domain.entity.Order.FAILURE_MESSAGE_DELIMITER;

import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantapproval.RestaurantApprovalResponseMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RestaurantApprovalResponseKafkaListener implements KafkaConsumer<RestaurantApprovalResponseAvroModel> {

    private final RestaurantApprovalResponseMessageListener restaurantApprovalResponseMessageListener;
    private final OrderMessagingDataMapper orderMessagingDataMapper;

    public RestaurantApprovalResponseKafkaListener(
        RestaurantApprovalResponseMessageListener restaurantApprovalResponseMessageListener,
        OrderMessagingDataMapper orderMessagingDataMapper
    ) {
        this.restaurantApprovalResponseMessageListener = restaurantApprovalResponseMessageListener;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
    }

    @Override
    @KafkaListener(
        id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
        topics = "${order-service.restaurant-approval-response-topic-name}"
    )
    public void receive(
        @Payload List<RestaurantApprovalResponseAvroModel> messages,
        @Header(KafkaHeaders.KEY) List<String> keys,
        @Header(KafkaHeaders.PARTITION) List<Integer> partitions,
        @Header(KafkaHeaders.OFFSET) List<Long> offset
    ) {
        log.info("{} number of restaurant approval response received with keys {}, partitions {}, offset {}",
            messages.size(),
            keys.toString(),
            partitions.toString(),
            offset.toString()
        );

        messages.forEach(restaurantApprovalResponseAvroModel -> {
            if (OrderApprovalStatus.APPROVED == restaurantApprovalResponseAvroModel.getOrderApprovalStatus()) {
                log.info("Processing approved order for order id: {}",
                    restaurantApprovalResponseAvroModel.getOrderId());
                RestaurantApprovalResponse restaurantApprovalResponse = orderMessagingDataMapper
                    .approvalResponseAvroModelToApprovalResponse(restaurantApprovalResponseAvroModel);
                restaurantApprovalResponseMessageListener.orderApproved(restaurantApprovalResponse);
            } else if (OrderApprovalStatus.REJECTED == restaurantApprovalResponseAvroModel.getOrderApprovalStatus()) {
                log.info("Processing rejected order for order id: {}, with failure messages: {} ",
                    restaurantApprovalResponseAvroModel.getOrderId(),
                    String.join(FAILURE_MESSAGE_DELIMITER, restaurantApprovalResponseAvroModel.getFailureMessages())
                );
                RestaurantApprovalResponse restaurantApprovalResponse = orderMessagingDataMapper
                    .approvalResponseAvroModelToApprovalResponse(restaurantApprovalResponseAvroModel);
                restaurantApprovalResponseMessageListener.orderRejected(restaurantApprovalResponse);
            }
        });
    }
}
