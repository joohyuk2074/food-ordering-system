package com.food.ordering.system.order.service.messaging.kafka;

import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderKafkaMessageHelper {

    public <T> BiConsumer<SendResult<String, T>, Throwable> getKafkaCallback(
        String topicName,
        T message,
        String orderId
    ) {
        return (result, ex) -> {
            if (ex == null) {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Received new metadata. orderId: {}; Topic: {}; Partition {}; Offset {}; Timestamp {}, at time {}",
                    orderId,
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    metadata.timestamp(),
                    System.nanoTime());
            } else {
                log.error("Error while sending message {} to topic {}", message, topicName, ex);
            }
        };
    }
}
