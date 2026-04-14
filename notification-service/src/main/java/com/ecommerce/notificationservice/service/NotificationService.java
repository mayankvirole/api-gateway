package com.ecommerce.notificationservice.service;
import com.ecommerce.notificationservice.entity.NotificationLog;
import com.ecommerce.notificationservice.event.PaymentProcessedEvent;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "payment-processed-topic", groupId = "notification-group")
    @Transactional
    public void consumePaymentEvent(PaymentProcessedEvent event) {
        log.info("Received Payment Event for Order ID: {} with Status: {}", event.getOrderId(), event.getStatus());
        
        NotificationLog notification = NotificationLog.builder()
                .orderId(event.getOrderId())
                .message("Your order has been placed and payment is " + event.getStatus())
                .sentAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
        
        log.info("Email simulated and recorded in notification_logs database.");
    }
}
