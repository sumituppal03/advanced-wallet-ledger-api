package com.fintech.wallet.service;

import com.fintech.wallet.entity.LedgerEntry;
import com.fintech.wallet.repository.LedgerEntryRepository;
import com.fintech.wallet.repository.WalletRepository;
import org.springframework.data.domain.PageRequest;
import com.fintech.wallet.service.exception.ResourceNotFoundException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinancialAdvisorService {

    private final ChatClient chatClient;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final WalletRepository walletRepository;

    // Inject your configuration values directly into the constructor
    public FinancialAdvisorService(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${spring.ai.openai.chat.options.model}") String model,
            LedgerEntryRepository ledgerEntryRepository, 
            WalletRepository walletRepository) {
        
        // Manual initialization skips the faulty auto-config loops entirely
        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);
        OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi);
        
        this.chatClient = ChatClient.builder(chatModel)
                .defaultOptions(org.springframework.ai.openai.OpenAiChatOptions.builder()
                        .withModel(model)
                        .build())
                .build();
                
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.walletRepository = walletRepository;
    }

    public String generateSpendingInsights(Long userId, String userQuestion) {
        var wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));

        // OPTIMIZED: Fetching sorted database entries directly by walletId using our new repository query
        // Change the repository method name inside generateSpendingInsights to match:
        List<LedgerEntry> recentTransactions = ledgerEntryRepository
        .findByWalletIdOrderByCreatedAtDesc(wallet.getId(), PageRequest.of(0, 15))
        .getContent();

        // Handle cases where a wallet exists but hasn't made any purchases yet
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