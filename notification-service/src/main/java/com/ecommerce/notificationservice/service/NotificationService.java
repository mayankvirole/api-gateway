package com.ecommerce.notificationservice.service;
import com.ecommerce.notificationservice.entity.NotificationLog;
import com.ecommerce.notificationservice.event.PaymentProcessedEvent;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = "payment-processed-topic", groupId = "notification-group")
    @Transactional
    public void consumePaymentEvent(PaymentProcessedEvent event) {
        log.info("Received Payment Event for Order ID: {} with Status: {}", event.getOrderId(), event.getStatus());
        
        NotificationLog notification = new NotificationLog(event.getOrderId(), "Your order has been placed and payment is " + event.getStatus(), LocalDateTime.now());
        notificationRepository.save(notification);
        
        log.info("Email simulated and recorded in notification_logs database.");
    }
}
