package com.fintech.wallet.service;

import com.fintech.wallet.entity.Wallet;
import com.fintech.wallet.repository.LedgerEntryRepository;
import com.fintech.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser; // <-- ADD THIS

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Bypasses filters for the HTTP layer if needed
public class WalletServiceConcurrencyTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Test
    @WithMockUser // <-- ADD THIS: Simulates an authenticated user context for the service layer threads
    public void testConcurrentTransfersPreventDoubleSpending() throws InterruptedException {
        ledgerEntryRepository.deleteAll();
        walletRepository.deleteAll();

        walletRepository.saveAndFlush(new Wallet(1L, "USD", new BigDecimal("100.00")));
        walletRepository.saveAndFlush(new Wallet(2L, "USD", new BigDecimal("0.00")));

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startingGun = new CountDownLatch(1);
        CountDownLatch transferTracker = new CountDownLatch(numberOfThreads);
        
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("10.00"), "Concurrent Transfer Test");

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    startingGun.await();
                    walletService.transferFunds(request);
                } catch (Exception e) {
                    System.out.println("Thread rejected safely: " + e.getMessage());
                } finally {
                    transferTracker.countDown();
                }
            });
        }

        startingGun.countDown();
        transferTracker.await(15, TimeUnit.SECONDS);
        executorService.shutdown();

        Wallet updatedSender = walletRepository.findByUserId(1L).orElseThrow();
        Wallet updatedReceiver = walletRepository.findByUserId(2L).orElseThrow();

        int senderCompareResult = updatedSender.getCurrentBalance().compareTo(new BigDecimal("0.00"));
        int receiverCompareResult = updatedReceiver.getCurrentBalance().compareTo(new BigDecimal("100.00"));

        assertEquals(0, senderCompareResult, "Sender balance should be exactly 0.00!");
        assertEquals(0, receiverCompareResult, "Receiver balance should match perfectly!");
    }
}