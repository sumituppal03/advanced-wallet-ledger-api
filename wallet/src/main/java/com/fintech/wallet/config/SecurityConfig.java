package com.fintech.wallet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF protection so Swagger/Postman can send POST requests
            .csrf(csrf -> csrf.disable())
            
            // 2. Authorize incoming requests
            .authorizeHttpRequests(auth -> auth
                // Allow anyone to access Swagger UI, OpenAPI docs, and H2 console without logging in
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/h2-console/**"
                ).permitAll()
                
                // Secure your transfer API, but require basic authentication (username/password)
                .requestMatchers("/api/v1/wallets/**").authenticated()
                
                // Any other random URL requires authentication
                .anyRequest().authenticated()
            )
            
            // 3. Enable standard HTTP Basic Authentication (the popup login screen)
            .httpBasic(org.springframework.security.config.Customizer.withDefaults());

        return http.build();
    }
}
