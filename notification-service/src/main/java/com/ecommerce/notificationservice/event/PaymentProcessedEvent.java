package com.ecommerce.notificationservice.event;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class PaymentProcessedEvent {
    private Long orderId;
    private Long paymentId;
    private String status;
}
