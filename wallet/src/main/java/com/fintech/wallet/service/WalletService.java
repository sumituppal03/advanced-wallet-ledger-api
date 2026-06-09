package com.fintech.wallet.service;

import com.fintech.wallet.entity.LedgerEntry;
import com.fintech.wallet.entity.Wallet;
import com.fintech.wallet.repository.LedgerEntryRepository;
import com.fintech.wallet.repository.WalletRepository;
import com.fintech.wallet.service.exception.InsufficientFundsException;
import com.fintech.wallet.service.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WalletService(WalletRepository walletRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public void transferFunds(TransferRequest request){
        
        // Safety validation check: Enforce that you can't send money to yourself
        if (request.getSenderUserId().equals(request.getReceiverUserId())) {
            throw new IllegalArgumentException("Sender and receiver cannot be the same user account.");
        }

        // CURRENCY CHECK
        Wallet senderWallet = walletRepository.findByUserIdForUpdate(request.getSenderUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Sender wallet not found for user: " + request.getSenderUserId()));
        
        Wallet receiverWallet = walletRepository.findByUserIdForUpdate(request.getReceiverUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Receiver wallet not found for user: " + request.getReceiverUserId()));

        //BALANCE COMPLIANCE CHECK 
        if (senderWallet.getCurrentBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Available: " + senderWallet.getCurrentBalance() + ", Requested: " + request.getAmount());
        }  

        //GENERATE SHARED TRANSACTION UUID
        String transactionId = UUID.randomUUID().toString();

        //UPDATE CACHED BALANCES
        senderWallet.setCurrentBalance(senderWallet.getCurrentBalance().subtract(request.getAmount()));
        receiverWallet.setCurrentBalance(receiverWallet.getCurrentBalance().add(request.getAmount()));

        // Save wallet state changes back to MySQL
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        // Row A: The Debit Entry (Deducting from Sender)
        LedgerEntry debitEntry = new LedgerEntry(
                transactionId, 
                senderWallet, 
                LedgerEntry.EntryType.DEBIT, 
                request.getAmount(), 
                request.getDescription() + " (To User " + request.getReceiverUserId() + ")"
        );

        // Row B: The Credit Entry (Adding to Receiver)
        LedgerEntry creditEntry = new LedgerEntry(
                transactionId, 
                receiverWallet, 
                LedgerEntry.EntryType.CREDIT, 
                request.getAmount(), 
                request.getDescription() + " (From User " + request.getSenderUserId() + ")"
        );

        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);
    }    
}

