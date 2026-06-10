package com.fintech.wallet.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
// We add an optimized UNIQUE index right here over your transactionId to catch double-submits
@Table(name = "ledger_entries", indexes = {
    @Index(name = "idx_transaction_id", columnList = "transaction_id", unique = true)
})
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Made unique = true to enforce idempotency at the database constraint layer
    @Column(name = "transaction_id", nullable = false, unique = true, length = 36)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 6)
    private EntryType type; 

    @Column(nullable = false, precision = 19, scale = 4) // Added explicit financial precision
    private BigDecimal amount;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum EntryType {
        DEBIT, CREDIT
    }

    public LedgerEntry() {}

    public LedgerEntry(String transactionId, Wallet wallet, EntryType type, BigDecimal amount, String description) {
        this.transactionId = transactionId;
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { 
        return id; 
    }
    public String getTransactionId() { 
        return transactionId; 
    }
    public Wallet getWallet() { 
        return wallet; 
    }
    public EntryType getType() { 
        return type; 
    }
    public BigDecimal getAmount() { 
        return amount; 
    }
    public String getDescription() { 
        return description; 
    }
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }

    // Added setters so our business logic service can instantiate and populate rows smoothly
    public void setTransactionId(String transactionId) { 
        this.transactionId = transactionId; 
    }
    public void setWallet(Wallet wallet) { 
        this.wallet = wallet; 
    }
    public void setType(EntryType type) { 
        this.type = type; 
    }
    public void setAmount(BigDecimal amount) { 
        this.amount = amount; 
    }
    public void setDescription(String description) { 
        this.description = description; 
    }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
}