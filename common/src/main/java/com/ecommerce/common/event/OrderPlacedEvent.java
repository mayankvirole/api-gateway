package com.ecommerce.common.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class OrderPlacedEvent {
    private int schemaVersion = 1;
    private String eventId = UUID.randomUUID().toString();
    private String idempotencyKey;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String status;
    private List<OrderLineItemEvent> lineItems;

    public OrderPlacedEvent() {
    }

    public OrderPlacedEvent(Long orderId, Long userId, BigDecimal amount, String status, List<OrderLineItemEvent> lineItems) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.lineItems = lineItems;
        this.idempotencyKey = "order-" + orderId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderLineItemEvent> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<OrderLineItemEvent> lineItems) {
        this.lineItems = lineItems;
    }
}
