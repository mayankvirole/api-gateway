package com.ecommerce.paymentservice.event;
import java.math.BigDecimal;

public class OrderPlacedEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String status;

    public OrderPlacedEvent() {}
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
