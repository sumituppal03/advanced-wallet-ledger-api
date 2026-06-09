package com.fintech.wallet.controller;

public class LoginRequest {
    private Long userId;

    public LoginRequest() {}

    public LoginRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() { 
        return userId; 
    }

    public void setUserId(Long userId) { 
        this.userId = userId; 
    }
}
