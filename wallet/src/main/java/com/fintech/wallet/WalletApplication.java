package com.fintech.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
    // Prevents the modern Spring Cloud Function transitive dependency crash
    org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration.class,
    // Prevents the strict, early startup validation check for standard OpenAI keys
    org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class,
    org.springframework.ai.autoconfigure.chat.client.ChatClientAutoConfiguration.class
})
public class WalletApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletApplication.class, args);
    }
}