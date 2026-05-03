package com.ecommerce.notificationservice.service;

import com.ecommerce.common.event.PaymentProcessedEvent;
import com.ecommerce.notificationservice.entity.NotificationLog;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;

    @Test
    void failedPaymentNotificationIncludesReason() {
        NotificationService service = new NotificationService(notificationRepository);

        service.consumePaymentEvent(new PaymentProcessedEvent(44L, 9L, "FAILED", "declined"));

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals("Your order payment failed. Reason: declined", captor.getValue().getMessage());
    }
}
