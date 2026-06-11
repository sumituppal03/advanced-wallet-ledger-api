package com.fintech.wallet.service;

import com.fintech.wallet.entity.LedgerEntry;
import com.fintech.wallet.entity.Wallet;
import com.fintech.wallet.repository.LedgerEntryRepository;
import com.fintech.wallet.repository.WalletRepository;
import com.fintech.wallet.service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialAdvisorServiceTest {

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private WalletRepository walletRepository;

    // Remove the simple @Mock annotation from chatClient so we can initialize it with deep stubs
    private ChatClient chatClient;

    @InjectMocks
    private FinancialAdvisorService financialAdvisorService;

    @BeforeEach
    void setUp() {
        // 1. Initialize ChatClient with deep stubs to handle any variations of the fluent chain automatically
        chatClient = mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);

        // 2. Explicitly wire the stubbed client into the service
        ReflectionTestUtils.setField(financialAdvisorService, "chatClient", chatClient);

        // 3. Use lenient() so tests that don't call the AI won't trigger UnnecessaryStubbing exceptions
        lenient().when(chatClient.prompt().user(anyString()).call().content())
                .thenReturn("AI says: You spend too much on coffee.");
                
        // Alternative fallback stubbing just in case your code calls .system() right before .user()
        lenient().when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("AI says: You spend too much on coffee.");
    }

    @Test
    void generateSpendingInsights_Success_WithTransactions() {
        Long userId = 2L;
        String userQuestion = "Should I stop buying coffee?";
        
        Wallet mockWallet = new Wallet();
        ReflectionTestUtils.setField(mockWallet, "id", 10L);
        ReflectionTestUtils.setField(mockWallet, "userId", userId);

        LedgerEntry transaction = new LedgerEntry();
        ReflectionTestUtils.setField(transaction, "type", LedgerEntry.EntryType.DEBIT);
        ReflectionTestUtils.setField(transaction, "amount", new BigDecimal("5.50"));
        ReflectionTestUtils.setField(transaction, "description", "Coffee Shop");
        ReflectionTestUtils.setField(transaction, "createdAt", LocalDateTime.now());
        
        List<LedgerEntry> transactionsList = List.of(transaction);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(mockWallet));
        when(ledgerEntryRepository.findByWalletIdOrderByCreatedAtDesc(eq(10L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(transactionsList));

        // Act
        String result = financialAdvisorService.generateSpendingInsights(userId, userQuestion);

        // Assert
        assertNotNull(result, "The service returned null! Verify your ChatClient configuration matches.");
        assertTrue(result.contains("coffee"));
        verify(walletRepository, times(1)).findByUserId(userId);
    }

    @Test
    void generateSpendingInsights_WalletNotFound_ThrowsException() {
        Long invalidUserId = 99L;
        when(walletRepository.findByUserId(invalidUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            financialAdvisorService.generateSpendingInsights(invalidUserId, "How is my balance?");
        });
    }
}