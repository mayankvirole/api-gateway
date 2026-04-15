package com.ecommerce.notificationservice.event;

public class PaymentProcessedEvent {
    private Long orderId;
    private Long paymentId;
    private String status;

    public PaymentProcessedEvent() {}
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
