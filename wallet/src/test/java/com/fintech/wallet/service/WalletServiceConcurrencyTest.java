package com.fintech.wallet.service;

import com.fintech.wallet.entity.Wallet;
import com.fintech.wallet.repository.LedgerEntryRepository;
import com.fintech.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test-env") // Points Spring to a clean properties file
@AutoConfigureMockMvc(addFilters = false)
public class WalletServiceConcurrencyTest {

    @MockitoBean
    private ChatModel chatModel;

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

        walletRepository.saveAndFlush(new Wallet(1L, "USD", new BigDecimal("100.00")));
        walletRepository.saveAndFlush(new Wallet(2L, "USD", new BigDecimal("0.00")));

        int numberOfThreads = 12; 
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startingGun = new CountDownLatch(1);
        CountDownLatch transferTracker = new CountDownLatch(numberOfThreads);
        
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("10.00"), "Concurrent Transfer Test");
        SecurityContext context = SecurityContextHolder.getContext();

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    SecurityContextHolder.setContext(context);
                    startingGun.await();
                    walletService.transferFunds(request, java.util.UUID.randomUUID().toString());
                } catch (Exception e) {
                    System.out.println("Thread rejected safely: " + e.getMessage());
                } finally {
                    SecurityContextHolder.clearContext();
                    transferTracker.countDown();
                }
            });
        }

        startingGun.countDown();
        transferTracker.await(15, TimeUnit.SECONDS);
        executorService.shutdown();

        Wallet updatedSender = walletRepository.findByUserId(1L).orElseThrow();
        assertTrue(updatedSender.getCurrentBalance().compareTo(BigDecimal.ZERO) >= 0, 
                "Fintech Safety Check Failed: Sender balance went negative!");
    }
}