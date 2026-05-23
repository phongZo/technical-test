package org.example.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "best_price")
public class BestPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol;

    // highest bid across exchanges, used for SELL orders
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal bidPrice;

    // lowest ask across exchanges, used for BUY orders
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal askPrice;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public BestPrice() {}

    public BestPrice(String symbol, BigDecimal bidPrice, BigDecimal askPrice, LocalDateTime updatedAt) {
        this.symbol = symbol;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getBidPrice() { return bidPrice; }
    public void setBidPrice(BigDecimal bidPrice) { this.bidPrice = bidPrice; }
    public BigDecimal getAskPrice() { return askPrice; }
    public void setAskPrice(BigDecimal askPrice) { this.askPrice = askPrice; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
