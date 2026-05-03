package com.ecommerce.common.event;

import java.util.UUID;

public class PaymentProcessedEvent {
    private int schemaVersion = 1;
    private String eventId = UUID.randomUUID().toString();
    private String idempotencyKey;
    private Long orderId;
    private Long paymentId;
    private String status;
    private String failureReason;

    public PaymentProcessedEvent() {
    }

    public PaymentProcessedEvent(Long orderId, Long paymentId, String status, String failureReason) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.status = status;
        this.failureReason = failureReason;
        this.idempotencyKey = "payment-" + orderId;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
