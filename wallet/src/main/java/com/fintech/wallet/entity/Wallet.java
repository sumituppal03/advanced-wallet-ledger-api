package com.fintech.wallet.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance;

    // Required Default Constructor
    public Wallet() {}

    // Parametrized Constructor
    public Wallet(Long userId, String currency, BigDecimal currentBalance) {
        this.userId = userId;
        this.currency = currency;
        this.currentBalance = currentBalance;
    }

    // Getters and Setters
    public Long getId() { 
        return id; 
    }
    public Long getUserId() { 
        return userId; 
    }
    public void setUserId(Long userId) { 
        this.userId = userId; 
    }
    public String getCurrency() { 
        return currency; 
    }
    public void setCurrency(String currency) { 
        this.currency = currency; 
    }
    public BigDecimal getCurrentBalance() { 
        return currentBalance; 
    }
    public void setCurrentBalance(BigDecimal currentBalance) { 
        this.currentBalance = currentBalance; 
    }
}