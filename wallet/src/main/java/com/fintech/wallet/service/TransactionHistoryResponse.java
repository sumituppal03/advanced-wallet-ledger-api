package com.fintech.wallet.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionHistoryResponse {
    private String transactionId;
    private String type; // DEBIT or CREDIT
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;

    public TransactionHistoryResponse(String transactionId, String type, BigDecimal amount, String description, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getTransactionId() { 
        return transactionId; 
    }
    public void setTransactionId(String transactionId) { 
        this.transactionId = transactionId; 
    }

    public String getType() { 
        return type; 
    }
    public void setType(String type) { 
        this.type = type; 
    }

    public BigDecimal getAmount() { 
        return amount; 
    }
    public void setAmount(BigDecimal amount) { 
        this.amount = amount; 
    }

    public String getDescription() { 
        return description; 
    }
    public void setDescription(String description) { 
        this.description = description; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
}
