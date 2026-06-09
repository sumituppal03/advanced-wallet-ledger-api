package com.fintech.wallet.service;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public class TransferRequest {
    @NotNull(message = "Sender user ID cannot be null")
    private Long senderUserId;
    @NotNull(message = "Receiver user ID cannot be null")
    private Long receiverUserId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Description cannot be null")
    private String description;

    public TransferRequest() {}

    public TransferRequest(Long senderUserId, Long receiverUserId, BigDecimal amount, String description) {
        this.senderUserId = senderUserId;
        this.receiverUserId = receiverUserId;
        this.amount = amount;
        this.description = description;
    }

    public Long getSenderUserId() { 
        return senderUserId; 
    }
    public void setSenderUserId(Long senderUserId) { 
        this.senderUserId = senderUserId; 
    }
    public Long getReceiverUserId() { 
        return receiverUserId; 
    }
    public void setReceiverUserId(Long receiverUserId) { 
        this.receiverUserId = receiverUserId; 
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
}
