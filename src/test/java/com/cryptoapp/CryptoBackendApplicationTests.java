package com.cryptoapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is a basic test to ensure our Spring Boot application can start up
 * correctly.
 * 
 * @SpringBootTest tells Spring to:
 *                 1. Load the complete application context
 *                 2. Start the embedded server
 *                 3. Make sure all beans can be created properly
 */
@SpringBootTest
class CryptoBackendApplicationTests {

    /**
     * This test simply checks that the Spring application context loads without
     * errors.
     * If there are any configuration problems, this test will fail.
     */
    @Test
    void contextLoads() {
        // If this test passes, it means Spring Boot started successfully!
        System.out.println("✅ Application context loaded successfully!");
    }
}