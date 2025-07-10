package com.cryptoapp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This tests our CryptoController without starting the full application.
 * 
 * @WebMvcTest focuses only on web layer testing:
 *             - Loads only the web layer (controllers, filters, etc.)
 *             - Provides MockMvc for testing HTTP requests
 *             - Much faster than full integration tests
 */
@WebMvcTest(CryptoController.class)
class CryptoControllerTest {

    /**
     * MockMvc simulates HTTP requests without starting a real server.
     * Spring automatically injects this for us in @WebMvcTest.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Test the /api/crypto/health endpoint
     */
    @Test
    void healthCheck_ShouldReturnHealthyMessage() throws Exception {
        mockMvc.perform(get("/api/crypto/health"))
                .andExpect(status().isOk()) // Expects HTTP 200
                .andExpect(content().string("Crypto Backend is healthy! 💪"));
    }

    /**
     * Test the /api/crypto/info endpoint
     */
    @Test
    void getAppInfo_ShouldReturnJsonWithAppDetails() throws Exception {
        mockMvc.perform(get("/api/crypto/info"))
                .andExpect(status().isOk()) // Expects HTTP 200
                .andExpect(content().contentType("application/json")) // Expects JSON response
                .andExpect(jsonPath("$.appName").value("Crypto Backend")) // Check JSON fields
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.description").value("Learning Spring Boot with crypto data"));
    }
}