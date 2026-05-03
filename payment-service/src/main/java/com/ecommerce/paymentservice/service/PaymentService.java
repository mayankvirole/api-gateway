package com.ecommerce.paymentservice.service;
import com.ecommerce.common.event.OrderPlacedEvent;
import com.ecommerce.common.event.PaymentProcessedEvent;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final KafkaOperations<String, Object> kafkaTemplate;

    @Value("${payments.max-auto-approval-amount:1000000}")
    private java.math.BigDecimal maxAutoApprovalAmount;

    public PaymentService(PaymentRepository paymentRepository, KafkaOperations<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order-placed-topic", groupId = "payment-group")
    @Transactional
    public void consumeOrderEvent(OrderPlacedEvent event) {
        String idempotencyKey = event.getIdempotencyKey() == null ? "order-" + event.getOrderId() : event.getIdempotencyKey();
        if (paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            log.info("Skipping duplicate payment event idempotencyKey={}", idempotencyKey);
            return;
        }

        boolean approved = event.getAmount() != null && event.getAmount().compareTo(maxAutoApprovalAmount) <= 0;
        String status = approved ? "SUCCESS" : "FAILED";
        String failureReason = approved ? null : "Payment amount exceeds auto approval limit";

        Payment payment = new Payment(event.getOrderId(), event.getAmount(), status, idempotencyKey, LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        PaymentProcessedEvent outEvent = new PaymentProcessedEvent(saved.getOrderId(), saved.getId(), status, failureReason);
        kafkaTemplate.send("payment-processed-topic", outEvent);
        log.info("Published payment result orderId={} paymentId={} status={}", saved.getOrderId(), saved.getId(), status);
    }
}
