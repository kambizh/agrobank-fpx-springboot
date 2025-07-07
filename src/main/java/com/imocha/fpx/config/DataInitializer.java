package com.imocha.fpx.config;

import com.imocha.fpx.service.AgrobankFpxEndpointAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    
    private final AgrobankFpxEndpointAuthService authService;
    
    @Bean
    CommandLineRunner init() {
        return args -> {
            try {
                // Save test encryption key
                authService.saveEncryptionKey("mySecretKey123");
                
                // Save test user with password "password"
                authService.saveAuthUserPwd("testuser", "password");
                
                log.info("Test data initialized successfully");
                log.info("Test user: testuser");
                log.info("Test password: password");
                
            } catch (Exception e) {
                log.error("Error initializing test data", e);
            }
        };
    }
}
