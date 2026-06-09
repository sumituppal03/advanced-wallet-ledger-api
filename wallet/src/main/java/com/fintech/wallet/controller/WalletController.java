package com.fintech.wallet.controller;

import com.fintech.wallet.service.TransferRequest;
import com.fintech.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * Endpoint to execute a secure fund transfer between wallets.
     * @Valid activates the Jakarta Validation constraints inside the TransferRequest object.
     */
    @PostMapping("/transfer")
    public ResponseEntity<String> executeTransfer(@Valid @RequestBody TransferRequest request) {
        // Hand off the data payload to our transactional service engine
        walletService.transferFunds(request);
        
        // Return a clean 200 OK success message if the full database ledger transaction succeeds
        return ResponseEntity.ok("Transaction completed successfully. Ledger records updated.");
    }
}
