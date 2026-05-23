package org.example.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String symbol;

    // BUY or SELL
    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    // total = price * quantity (in USDT)
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal total;

    @Column(nullable = false)
    private LocalDateTime transactedAt;

    public Transaction() {}

    public Transaction(Long userId, String symbol, String type,
                       BigDecimal price, BigDecimal quantity, BigDecimal total,
                       LocalDateTime transactedAt) {
        this.userId = userId;
        this.symbol = symbol;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.total = total;
        this.transactedAt = transactedAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public LocalDateTime getTransactedAt() { return transactedAt; }
    public void setTransactedAt(LocalDateTime transactedAt) { this.transactedAt = transactedAt; }
}
