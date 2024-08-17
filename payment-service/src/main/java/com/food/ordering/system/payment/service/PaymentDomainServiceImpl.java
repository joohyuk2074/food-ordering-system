package com.food.ordering.system.payment.service;

import static com.food.ordering.system.application.handler.domain.DomainConstants.UTC;

import com.food.ordering.system.application.handler.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.application.handler.domain.valueobject.Money;
import com.food.ordering.system.application.handler.domain.valueobject.PaymentStatus;
import com.food.ordering.system.payment.service.domain.PaymentDomainService;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.payment.service.domain.valueobject.CreditHistoryId;
import com.food.ordering.system.payment.service.domain.valueobject.TransactionType;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService {

    @Override
    public PaymentEvent validateAndInitiatePayment(
        Payment payment,
        CreditEntry creditEntry,
        List<CreditHistory> creditHistories,
        List<String> failureMessages,
        DomainEventPublisher<PaymentCompletedEvent> paymentCompletedEventDomainEventPublisher,
        DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher
    ) {
        payment.validatePayment(failureMessages);
        payment.initializePayment();
        validateCreditEntry(payment, creditEntry, failureMessages);
        subtractCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistories, TransactionType.DEBIT);
        validateCreditHistory(creditEntry, creditHistories, failureMessages);

        if (failureMessages.isEmpty()) {
            log.info("Payment is initiated for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.COMPLETE);
            return new PaymentCompletedEvent(
                payment,
                ZonedDateTime.now(ZoneId.of(UTC)),
                paymentCompletedEventDomainEventPublisher
            );
        } else {
            log.info("Payment initiation is failed for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(
                payment,
                ZonedDateTime.now(ZoneId.of(UTC)),
                failureMessages,
                paymentFailedEventDomainEventPublisher
            );
        }
    }

    @Override
    public PaymentEvent validateAndCancelPayment(
        Payment payment,
        CreditEntry creditEntry,
        List<CreditHistory> creditHistories,
        List<String> failureMessages,
        DomainEventPublisher<PaymentCancelledEvent> paymentCancelledEventDomainEventPublisher,
        DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher
    ) {
        payment.validatePayment(failureMessages);
        addCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistories, TransactionType.CREDIT);

        if (failureMessages.isEmpty()) {
            log.info("Payment is cancelled for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.CANCELLED);
            return new PaymentCancelledEvent(
                payment,
                ZonedDateTime.now(ZoneId.of(UTC)),
                paymentCancelledEventDomainEventPublisher
            );
        } else {
            log.info("Payment cancellation is failed for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return  new PaymentFailedEvent(
                payment,
                ZonedDateTime.now(ZoneId.of(UTC)),
                failureMessages,
                paymentFailedEventDomainEventPublisher
            );
        }
    }

    private void validateCreditEntry(
        Payment payment,
        CreditEntry creditEntry,
        List<String> failureMessages
    ) {
        if (payment.getPrice().isGreaterThan(creditEntry.getTotalCreditAmount())) {
            log.error("Customer with id: {} doesn't have enough credit for payment!",
                payment.getCustomerId().getValue());
            failureMessages.add("Customer with id: " + payment.getCustomerId().getValue()
                + " doesn't have enough credit for payment!");
        }
    }

    private void subtractCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.subtractCreditAmount(payment.getPrice());
    }

    private void updateCreditHistory(
        Payment payment,
        List<CreditHistory> creditHistories,
        TransactionType transactionType
    ) {
        CreditHistory creditHistory = CreditHistory.builder()
            .creditHistoryId(new CreditHistoryId(UUID.randomUUID()))
            .customerId(payment.getCustomerId())
            .amount(payment.getPrice())
            .transactionType(transactionType)
            .build();
        creditHistories.add(creditHistory);
    }

    private void validateCreditHistory(
        CreditEntry creditEntry,
        List<CreditHistory> creditHistories,
        List<String> failureMessages
    ) {
        Money totalCreditHistory = getTotalHistoryAmount(creditHistories, TransactionType.CREDIT);
        Money totalDebitHistory = getTotalHistoryAmount(creditHistories, TransactionType.DEBIT);

        if (totalDebitHistory.isGreaterThan(totalCreditHistory)) {
            log.error("Customer with id: {} doesn't haver enough credit according to credit history",
                creditEntry.getCustomerId().getValue());
            failureMessages.add("Customer with id=" + creditEntry.getCustomerId().getValue() +
                " doesn't have enough credit according to credit history!");
        }

        if (!creditEntry.getTotalCreditAmount().equals(totalCreditHistory.subtract(totalCreditHistory))) {
            log.error("Credit history total is not equal to current credit for customer id: {}!",
                creditEntry.getCustomerId().getValue());
            failureMessages.add("Credit history total is not equal to current credit for customer id: " +
                creditEntry.getCustomerId().getValue() + "!");
        }
    }

    private Money getTotalHistoryAmount(List<CreditHistory> creditHistories, TransactionType credit) {
        return creditHistories.stream()
            .filter(creditHistory -> credit == creditHistory.getTransactionType())
            .map(CreditHistory::getAmount)
            .reduce(Money.ZERO, Money::add);
    }

    private void addCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.addCreditAmount(payment.getPrice());
    }
}
