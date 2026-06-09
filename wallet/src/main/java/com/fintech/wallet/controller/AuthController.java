package com.fintech.wallet.controller;

import com.fintech.wallet.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        // Generate a production-ready signed cryptographic token
        String token = tokenProvider.generateToken(loginRequest.getUserId());

        Map<String, String> response = new HashMap<>();
        response.put("token_type", "Bearer");
        response.put("access_token", token);

        return ResponseEntity.ok(response);
    }
}
