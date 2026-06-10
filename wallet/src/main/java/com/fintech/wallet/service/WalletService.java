package com.fintech.wallet.service;

import com.fintech.wallet.entity.LedgerEntry;
import com.fintech.wallet.entity.Wallet;
import com.fintech.wallet.repository.LedgerEntryRepository;
import com.fintech.wallet.service.TransferRequest;
import com.fintech.wallet.repository.WalletRepository;
import com.fintech.wallet.service.exception.InsufficientFundsException;
import com.fintech.wallet.service.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.math.BigDecimal;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WalletService(WalletRepository walletRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "balances", allEntries = true) 
    public void transferFunds(TransferRequest request, String clientTransactionId) {
        
        // 1. IDEMPOTENCY CHECK: Catch retries before doing any database execution
        var existingDebitLog = ledgerEntryRepository.findByTransactionId(clientTransactionId);
        if (existingDebitLog.isPresent()) {
            System.out.println("Idempotency Triggered! Request with transactionId " + clientTransactionId + " was already processed safely.");
            return; // Exit out gracefully without executing anything twice
        }

        // Safety validation check: Enforce that you can't send money to yourself
        if (request.getSenderUserId().equals(request.getReceiverUserId())) {
            throw new IllegalArgumentException("Sender and receiver cannot be the same user account.");
        }

        // CURRENCY CHECK WITH PESSIMISTIC ROW LOCKING
        Wallet senderWallet = walletRepository.findByUserIdForUpdate(request.getSenderUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Sender wallet not found for user: " + request.getSenderUserId()));
        
        Wallet receiverWallet = walletRepository.findByUserIdForUpdate(request.getReceiverUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Receiver wallet not found for user: " + request.getReceiverUserId()));

        // BALANCE COMPLIANCE CHECK 
        if (senderWallet.getCurrentBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Available: " + senderWallet.getCurrentBalance() + ", Requested: " + request.getAmount());
        }  

        // UPDATE BALANCES
        senderWallet.setCurrentBalance(senderWallet.getCurrentBalance().subtract(request.getAmount()));
        receiverWallet.setCurrentBalance(receiverWallet.getCurrentBalance().add(request.getAmount()));

        // Save wallet state changes back to MySQL
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        // Row A: The Debit Entry (Deducting from Sender using the CLIENT'S IDEMPOTENCY KEY)
        LedgerEntry debitEntry = new LedgerEntry(
                clientTransactionId, 
                senderWallet, 
                LedgerEntry.EntryType.DEBIT, 
                request.getAmount(), 
                request.getDescription() + " (To User " + request.getReceiverUserId() + ")"
        );

        // Row B: The Credit Entry (Adding to Receiver)
        LedgerEntry creditEntry = new LedgerEntry(
                clientTransactionId + "-CR", 
                receiverWallet, 
                LedgerEntry.EntryType.CREDIT, 
                request.getAmount(), 
                request.getDescription() + " (From User " + request.getSenderUserId() + ")"
        );

        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);
    }    

    @Cacheable(value = "balances", key = "#userId")
    public BigDecimal getWalletBalance(Long userId) {
        System.out.println("Cache Miss! Fetching fresh balance from MySQL for User: " + userId);
        return walletRepository.findByUserId(userId)
                .map(Wallet::getCurrentBalance)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
    }

    @Transactional(readOnly = true)
    public Page<TransactionHistoryResponse> getTransactionHistory(Long userId, int page, int size) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));

        Pageable pageable = PageRequest.of(page, size);

        Page<LedgerEntry> ledgerEntries = ledgerEntryRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable);
        
        return ledgerEntries.map(entry -> new TransactionHistoryResponse(
                entry.getTransactionId(),
                entry.getType().toString(), 
                entry.getAmount(),
                entry.getDescription(),
                entry.getCreatedAt()
        ));
    
    }
}