package com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.repository;

import com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.entity.ApprovalOutboxEntity;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalOutboxJpaRepository extends JpaRepository<ApprovalOutboxEntity, UUID> {

    Optional<List<ApprovalOutboxEntity>> findByTypeAndOutboxStatusAndSagaStatusIn(
        String type,
        OutboxStatus outboxStatus,
        List<SagaStatus> sagaStatus
    );

    Optional<ApprovalOutboxEntity> findByTypeAndSagaIdAndSagaStatusIn(
        String type,
        UUID sagaId,
        List<SagaStatus> sagaStatus
    );

    void deleteByTypeAndOutboxStatusAndSagaStatusIn(
        String type,
        OutboxStatus outboxStatus,
        List<SagaStatus> sagaStatus
    );
}
