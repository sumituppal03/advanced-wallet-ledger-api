package com.fintech.wallet.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TransactionCategorizationService {
    private final ChatClient chatClient;

    private static final List<String> VALID_CATEGORIES= List.of("Groceries","Transfer","Entertainment","Utilities",
        "Dining","Transport","Shopping","Income","Other"
    );
    public static final String DEFAULT_CATEGORY = "Uncategorized";
    public TransactionCategorizationService(ChatModel chatModel){
        this.chatClient=ChatClient.builder(chatModel).build();
    }
    public String categorizeTransaction(String description){
        String systemPrompt="""
                You are a transaction categorizer for a banking app.
                Classify the transaction description into EXACTLY ONE of these categories:
                Groceries, Transfer, Entertainment, Utilities, Dining, Transport, Shopping, Income, Other.
                Respond with ONLY the category word, nothing else. No punctuation, no explanation.
                """;
        try {
            String result=chatClient.prompt().system(systemPrompt)
            .user(description)
            .call()
            .content()
            .trim();
            if(VALID_CATEGORIES.contains(result)){
                return result;
            }else{
                System.out.println("Categorization guardrail triggered. LLM returned: '" + result
                        + "'. Defaulting to " + DEFAULT_CATEGORY + ".");
                return DEFAULT_CATEGORY;        
            }
        } catch (Exception e) {
           System.out.println("Categorization failed: " + e.getMessage()
                    + ". Defaulting to " + DEFAULT_CATEGORY + ".");
            return DEFAULT_CATEGORY;
        }        
    }
}
