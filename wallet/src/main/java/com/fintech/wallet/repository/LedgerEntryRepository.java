package com.fintech.wallet.repository;

import com.fintech.wallet.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    // This allows our service to check if an idempotency key (transactionId) already exists
    Optional<LedgerEntry> findByTransactionId(String transactionId);
}