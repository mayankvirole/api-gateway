package com.ecommerce.orderservice.event;
import java.math.BigDecimal;

public class OrderPlacedEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String status;

    public OrderPlacedEvent() {}
    public OrderPlacedEvent(Long orderId, Long userId, BigDecimal amount, String status) {
        this.orderId = orderId; this.userId = userId; this.amount = amount; this.status = status;
    }
    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
}
