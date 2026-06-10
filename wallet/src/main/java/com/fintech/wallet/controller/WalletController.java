package com.fintech.wallet.controller;

import com.fintech.wallet.service.TransferRequest;
import com.fintech.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader; // Added this import
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    // Injecting our core business logic engine via constructor injection
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> executeTransfer(
            @RequestHeader("X-Transaction-Id") String transactionId, // Extracted tracking token from headers
            @Valid @RequestBody TransferRequest request) {
        
        // Guard Rail: Reject instantly if the client application didn't supply an idempotency key
        if (transactionId == null || transactionId.isBlank()) {
            return ResponseEntity.badRequest()
                .body("Error: Missing required 'X-Transaction-Id' header for idempotency protection.");
        }

        // Hand off the data payload AND the unique token identifier to our transactional service engine
        walletService.transferFunds(request, transactionId);
        
        // Return a clean 200 OK success message if the full database ledger transaction succeeds
        return ResponseEntity.ok("Transaction completed successfully. Ledger records updated.");
    }
}