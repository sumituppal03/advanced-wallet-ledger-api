package com.fintech.wallet.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionHistoryResponse {
    private String transactionId;
    private String type;
    private BigDecimal amount;
    private String description;
    private String category;
    private LocalDateTime createdAt;

    public TransactionHistoryResponse(String transactionId, String type, BigDecimal amount, String description, String category, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.createdAt = createdAt;
    }

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

    public String getCategory() { 
        return category; 
    }
    public void setCategory(String category) { 
        this.category = category; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}