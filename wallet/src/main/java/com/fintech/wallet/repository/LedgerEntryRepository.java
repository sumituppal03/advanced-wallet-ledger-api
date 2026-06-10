package com.fintech.wallet.repository;

import com.fintech.wallet.entity.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    
    Optional<LedgerEntry> findByTransactionId(String transactionId);
    
    // This is your gold-standard working method. We will use ONLY this!
    Page<LedgerEntry> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
}