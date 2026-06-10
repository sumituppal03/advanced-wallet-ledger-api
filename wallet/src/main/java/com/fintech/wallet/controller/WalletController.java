package com.fintech.wallet.controller;

import com.fintech.wallet.service.FinancialAdvisorService; 
import com.fintech.wallet.service.TransactionHistoryResponse; 
import com.fintech.wallet.service.TransferRequest;
import com.fintech.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // Imported for Swagger Lock Authentication
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader; 
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;
    private final FinancialAdvisorService financialAdvisorService; 

    public WalletController(WalletService walletService, FinancialAdvisorService financialAdvisorService) {
        this.walletService = walletService;
        this.financialAdvisorService = financialAdvisorService;
    }

    /**
     * Executes an idempotent balance transfer between two wallets.
     */
    @SecurityRequirement(name = "BearerAuth") // Tells Swagger to apply your token to this request
    @PostMapping("/transfer")
    public ResponseEntity<String> executeTransfer(
            @RequestHeader("X-Transaction-Id") String transactionId, 
            @Valid @RequestBody TransferRequest request) {
        
        if (transactionId == null || transactionId.isBlank()) {
            return ResponseEntity.badRequest()
                .body("Error: Missing required 'X-Transaction-Id' header for idempotency protection.");
        }

        walletService.transferFunds(request, transactionId);
        return ResponseEntity.ok("Transaction completed successfully. Ledger records updated.");
    }

    /**
     * Exposes a paginated bank statement / transaction audit history for a single user account.
     */
    @SecurityRequirement(name = "BearerAuth") // Tells Swagger to apply your token to this request
    @GetMapping("/{userId}/transactions")
    public ResponseEntity<Page<TransactionHistoryResponse>> getWalletTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<TransactionHistoryResponse> history = walletService.getTransactionHistory(userId, page, size);
        return ResponseEntity.ok(history);
    }

    /**
     * Exposes a free, cloud-based open-source AI Financial Advisor endpoint.
     */
    @SecurityRequirement(name = "BearerAuth") // Tells Swagger to apply your token to this request
    @GetMapping("/{userId}/ai-advisor")
    public ResponseEntity<String> getAiInsights(
            @PathVariable Long userId,
            @RequestParam String question) {
        
        String aiResponse = financialAdvisorService.generateSpendingInsights(userId, question);
        return ResponseEntity.ok(aiResponse);
    }
}