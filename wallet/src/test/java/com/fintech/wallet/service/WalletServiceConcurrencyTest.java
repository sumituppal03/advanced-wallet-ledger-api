package com.fintech.wallet.service;

import com.fintech.wallet.entity.Wallet;
import com.fintech.wallet.repository.LedgerEntryRepository;
import com.fintech.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // For Spring Boot 3.4+ compat
import org.springframework.ai.chat.model.ChatModel; // New Import
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:wallet_concurrency_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.ai.openai.api-key=mock-key-for-ci-pipeline"
})
@AutoConfigureMockMvc(addFilters = false)
public class WalletServiceConcurrencyTest {

    @MockitoBean
    private ChatModel chatModel; // Safely bypasses the Groq/OpenAI initialization blocks

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Test
    @WithMockUser
    public void testConcurrentTransfersPreventDoubleSpending() throws InterruptedException {
        ledgerEntryRepository.deleteAll();
        walletRepository.deleteAll();

        // 1. Arrange: Give the user $100.00
        walletRepository.saveAndFlush(new Wallet(1L, "USD", new BigDecimal("100.00")));
        walletRepository.saveAndFlush(new Wallet(2L, "USD", new BigDecimal("0.00")));

        // Fire 12 threads (Attempting to spend $120.00 total, which exceeds the balance!)
        int numberOfThreads = 12; 
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startingGun = new CountDownLatch(1);
        CountDownLatch transferTracker = new CountDownLatch(numberOfThreads);
        
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("10.00"), "Concurrent Transfer Test");

        // Capture security context to pass safely down to background workers
        SecurityContext context = SecurityContextHolder.getContext();

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    SecurityContextHolder.setContext(context); // Share authentication context to worker threads
                    startingGun.await();
                    walletService.transferFunds(request, java.util.UUID.randomUUID().toString());
                } catch (Exception e) {
                    System.out.println("Thread rejected safely as expected: " + e.getMessage());
                } finally {
                    SecurityContextHolder.clearContext();
                    transferTracker.countDown();
                }
            });
        }

        // 2. Act: Fire all execution workers simultaneously
        startingGun.countDown();
        transferTracker.await(15, TimeUnit.SECONDS);
        executorService.shutdown();

        // 3. Assert: Verify balance consistency bounds
        Wallet updatedSender = walletRepository.findByUserId(1L).orElseThrow();
        
        // Assert that the balance NEVER drops below zero, proving pessimistic locks blocked over-spending
        assertTrue(updatedSender.getCurrentBalance().compareTo(BigDecimal.ZERO) >= 0, 
                "Fintech Safety Check Failed: Sender balance went negative! Over-spent detected.");
    }
}