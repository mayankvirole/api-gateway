package com.ecommerce.paymentservice.service;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.event.OrderPlacedEvent;
import com.ecommerce.paymentservice.event.PaymentProcessedEvent;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentService(PaymentRepository paymentRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order-placed-topic", groupId = "payment-group")
    @Transactional
    public void consumeOrderEvent(OrderPlacedEvent event) {
        Payment payment = new Payment(event.getOrderId(), event.getAmount(), "SUCCESS", LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        PaymentProcessedEvent outEvent = new PaymentProcessedEvent(saved.getOrderId(), saved.getId(), "SUCCESS");
        kafkaTemplate.send("payment-processed-topic", outEvent);
    }
}
