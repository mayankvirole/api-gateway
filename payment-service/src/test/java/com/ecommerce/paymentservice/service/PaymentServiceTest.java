package com.ecommerce.paymentservice.service;

import com.ecommerce.common.event.OrderPlacedEvent;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private KafkaOperations<String, Object> kafkaTemplate;

    @Test
    void duplicateOrderEventIsIgnored() {
        PaymentService service = paymentService();
        OrderPlacedEvent event = new OrderPlacedEvent(1L, 2L, BigDecimal.TEN, "PENDING", List.of());
        when(paymentRepository.findByIdempotencyKey("order-1"))
                .thenReturn(Optional.of(new Payment(1L, BigDecimal.TEN, "SUCCESS", "order-1", LocalDateTime.now())));

        service.consumeOrderEvent(event);

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(kafkaTemplate, never()).send(any(String.class), any(Object.class));
    }

    @Test
    void amountOverLimitCreatesFailedPayment() {
        PaymentService service = paymentService();
        OrderPlacedEvent event = new OrderPlacedEvent(1L, 2L, BigDecimal.valueOf(101), "PENDING", List.of());
        when(paymentRepository.findByIdempotencyKey("order-1")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.consumeOrderEvent(event);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals("FAILED", captor.getValue().getStatus());
    }

    private PaymentService paymentService() {
        PaymentService service = new PaymentService(paymentRepository, kafkaTemplate);
        ReflectionTestUtils.setField(service, "maxAutoApprovalAmount", BigDecimal.valueOf(100));
        return service;
    }
}
