package com.ecommerce.paymentservice.service;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.event.OrderPlacedEvent;
import com.ecommerce.paymentservice.event.PaymentProcessedEvent;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-placed-topic", groupId = "payment-group")
    @Transactional
    public void consumeOrderEvent(OrderPlacedEvent event) {
        // Idempotency / Dummy Payment Processing
        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .amount(event.getAmount())
                .status("SUCCESS")
                .processedAt(LocalDateTime.now())
                .build();
        Payment saved = paymentRepository.save(payment);

        PaymentProcessedEvent outEvent = new PaymentProcessedEvent(saved.getOrderId(), saved.getId(), "SUCCESS");
        kafkaTemplate.send("payment-processed-topic", outEvent);
    }
}
