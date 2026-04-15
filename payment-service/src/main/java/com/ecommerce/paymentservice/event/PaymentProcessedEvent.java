package com.ecommerce.paymentservice.event;

public class PaymentProcessedEvent {
    private Long orderId;
    private Long paymentId;
    private String status;

    public PaymentProcessedEvent() {}
    public PaymentProcessedEvent(Long orderId, Long paymentId, String status) {
        this.orderId = orderId; this.paymentId = paymentId; this.status = status;
    }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
