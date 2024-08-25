package com.food.ordering.system.order.service.dataaccess.outbox.payment.adapter;

import com.food.ordering.system.order.service.dataaccess.outbox.payment.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.exception.PaymentOutboxNotFoundException;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.mapper.PaymentOutboxDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.repository.PaymentOutboxJpaRepository;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxRepositoryImpl implements PaymentOutboxRepository {

    private final PaymentOutboxJpaRepository paymentOutboxJpaRepository;
    private final PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper;

    public PaymentOutboxRepositoryImpl(
        PaymentOutboxJpaRepository paymentOutboxJpaRepository,
        PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper
    ) {
        this.paymentOutboxJpaRepository = paymentOutboxJpaRepository;
        this.paymentOutboxDataAccessMapper = paymentOutboxDataAccessMapper;
    }

    @Override
    public OrderPaymentOutboxMessage save(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
        PaymentOutboxEntity entity = paymentOutboxDataAccessMapper
            .orderPaymentOutboxMessageToOutboxEntity(orderPaymentOutboxMessage);

        PaymentOutboxEntity savedEntity = paymentOutboxJpaRepository.save(entity);
        return paymentOutboxDataAccessMapper.paymentOutboxEntityToOrderPaymentOutboxMessage(savedEntity);
    }

    @Override
    public Optional<List<OrderPaymentOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(
        String sagaType,
        OutboxStatus outboxStatus,
        SagaStatus... sagaStatus
    ) {
        List<PaymentOutboxEntity> paymentOutboxEntities = paymentOutboxJpaRepository
            .findByTypeAndOutboxStatusAndSagaStatusIn(sagaType, outboxStatus, Arrays.asList(sagaStatus))
            .orElseThrow(() -> new PaymentOutboxNotFoundException("Payment outbox object could not be found for saga type " + sagaType));

        List<OrderPaymentOutboxMessage> orderPaymentOutboxMessages = paymentOutboxEntities.stream()
            .map(paymentOutboxDataAccessMapper::paymentOutboxEntityToOrderPaymentOutboxMessage)
            .toList();

        return Optional.of(orderPaymentOutboxMessages);
    }

    @Override
    public Optional<OrderPaymentOutboxMessage> findByTypeAndSagaIdAndSagaStatus(
        String type,
        UUID sagaId,
        SagaStatus... sagaStatus
    ) {
        return paymentOutboxJpaRepository
            .findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, Arrays.asList(sagaStatus))
            .map(paymentOutboxDataAccessMapper::paymentOutboxEntityToOrderPaymentOutboxMessage);
    }

    @Override
    public void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
        paymentOutboxJpaRepository
            .deleteByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, Arrays.asList(sagaStatus));
    }
}
