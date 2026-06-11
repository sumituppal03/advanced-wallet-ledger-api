package com.fintech.wallet.service;

import com.fintech.wallet.entity.LedgerEntry;
import com.fintech.wallet.repository.LedgerEntryRepository;
import com.fintech.wallet.repository.WalletRepository;
import org.springframework.data.domain.PageRequest;
import com.fintech.wallet.service.exception.ResourceNotFoundException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel; // Use core ChatModel interface
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinancialAdvisorService {

    private final ChatClient chatClient;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final WalletRepository walletRepository;

    // Spring Boot automatically configures the ChatModel bean using your application properties!
    public FinancialAdvisorService(
            ChatModel chatModel, 
            LedgerEntryRepository ledgerEntryRepository, 
            WalletRepository walletRepository) {
        
        // This sets up the ChatClient correctly for Groq out-of-the-box
        this.chatClient = ChatClient.builder(chatModel).build();
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.walletRepository = walletRepository;
    }

    public String generateSpendingInsights(Long userId, String userQuestion) {
        var wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));

        // Pulls a maximum of 15 records using your known good pageable query mapping
        List<LedgerEntry> recentTransactions = ledgerEntryRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId(), PageRequest.of(0, 15))
                .getContent();

        // Safe fallback logic for accounts with zero transactions
        String transactionContext = recentTransactions.isEmpty() 
                ? "No recent transaction history found for this account."
                : recentTransactions.stream()
                    .map(t -> String.format("- %s of $%s for %s on %s", 
                            t.getType(), t.getAmount(), t.getDescription(), t.getCreatedAt()))
                    .collect(Collectors.joining("\n"));

        String systemPrompt = """
                You are a helpful, smart AI Financial Advisor built into our banking app.
                Analyze the user's transaction ledger data below and answer their question directly.
                Keep your insights brief (2-3 sentences max) and practical.
                
                USER RECENT TRANSACTIONS:
                """ + transactionContext;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userQuestion)
                .call()
                .content();
    }
}