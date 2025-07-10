package com.cryptoapp;

import com.cryptoapp.component.CryptoValidator;
import com.cryptoapp.config.CryptoConfig;
import com.cryptoapp.service.CryptoService;
import com.cryptoapp.model.CryptoCurrency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test demonstrates ALL the Spring annotations working together!
 * 
 * @SpringBootTest loads the complete application context,
 *                 allowing us to test all our beans and their interactions
 */
@SpringBootTest
class AnnotationDemoTest {

    /**
     * DEPENDENCY INJECTION IN TESTS!
     * 
     * All these @Autowired annotations work because:
     * 1. @SpringBootTest loads the full application context
     * 2. Spring finds all our annotated classes
     * (@Service, @Component, @Configuration)
     * 3. Spring creates instances and wires them together
     * 4. Spring injects them into our test
     */

    @Autowired
    private CryptoService cryptoService; // @Service bean

    @Autowired
    private CryptoValidator cryptoValidator; // @Component bean

    @Autowired
    private RestTemplate restTemplate; // @Bean from @Configuration

    @Autowired
    private DateTimeFormatter cryptoDateFormatter; // @Bean from @Configuration

    @Autowired
    private ThreadPoolExecutor cryptoExecutor; // @Bean with custom name

    @Autowired
    private CryptoConfig.CryptoApiClient cryptoApiClient; // @Bean with dependencies

    /**
     * Test that all beans are properly injected (not null)
     */
    @Test
    void allBeansAreInjected() {
        assertNotNull(cryptoService, "CryptoService should be injected");
        assertNotNull(cryptoValidator, "CryptoValidator should be injected");
        assertNotNull(restTemplate, "RestTemplate should be injected");
        assertNotNull(cryptoDateFormatter, "DateTimeFormatter should be injected");
        assertNotNull(cryptoExecutor, "ThreadPoolExecutor should be injected");
        assertNotNull(cryptoApiClient, "CryptoApiClient should be injected");

        System.out.println("✅ All beans successfully injected!");
    }

    /**
     * Test @Service annotation - business logic works
     */
    @Test
    void serviceAnnotationWorks() {
        List<CryptoCurrency> cryptos = cryptoService.getPopularCryptos();

        assertNotNull(cryptos);
        assertTrue(cryptos.size() > 0);
        assertTrue(cryptos.size() <= 10); // Should respect @Value maxResults

        System.out.println("✅ @Service annotation works! Got " + cryptos.size() + " cryptos");
    }

    /**
     * Test @Component annotation - utility component works
     */
    @Test
    void componentAnnotationWorks() {
        assertTrue(cryptoValidator.isValidCryptoSymbol("BTC"));
        assertFalse(cryptoValidator.isValidCryptoSymbol("INVALID"));

        assertEquals("BTC", cryptoValidator.normalizeCryptoSymbol(" btc "));

        System.out.println("✅ @Component annotation works! Validator functioning");
    }

    /**
     * Test @Autowired working within @Service (CryptoService uses CryptoValidator)
     */
    @Test
    void autowiredDependenciesWork() {
        // This will use the validator internally via @Autowired
        double btcPrice = cryptoService.getCryptoPrice("BTC");
        double invalidPrice = cryptoService.getCryptoPrice("INVALID");

        assertTrue(btcPrice > 0);
        assertEquals(0.0, invalidPrice);

        System.out.println("✅ @Autowired dependencies work! BTC price: $" + btcPrice);
    }

    /**
     * Test @Value annotation - properties are injected correctly
     */
    @Test
    void valueAnnotationWorks() {
        Object config = cryptoService.getServiceConfig();
        assertNotNull(config);

        System.out.println("✅ @Value annotation works! Config: " + config);
    }

    /**
     * Test @Configuration and @Bean annotations
     */
    @Test
    void configurationAndBeanAnnotationsWork() {
        // Test RestTemplate bean
        assertNotNull(restTemplate);

        // Test DateTimeFormatter bean
        assertNotNull(cryptoDateFormatter);
        String formatted = cryptoDateFormatter.format(java.time.LocalDateTime.now());
        assertNotNull(formatted);

        // Test ThreadPoolExecutor bean
        assertNotNull(cryptoExecutor);
        assertEquals(2, cryptoExecutor.getCorePoolSize());

        // Test CryptoApiClient bean (which depends on other beans)
        assertNotNull(cryptoApiClient);
        String clientInfo = cryptoApiClient.getClientInfo();
        assertTrue(clientInfo.contains("CryptoApiClient ready"));

        System.out.println("✅ @Configuration and @Bean annotations work!");
        System.out.println("   RestTemplate: " + restTemplate.getClass().getSimpleName());
        System.out.println("   Formatted time: " + formatted);
        System.out.println("   Thread pool: " + cryptoExecutor.getCorePoolSize() + " core threads");
        System.out.println("   API Client: " + clientInfo);
    }

    /**
     * Test the complete flow - all annotations working together
     */
    @Test
    void completeAnnotationFlow() {
        System.out.println("\n🚀 COMPLETE ANNOTATION FLOW TEST");
        System.out.println("==================================");

        // 1. @Service with @Value and @Autowired dependencies
        System.out.println("1. Getting popular cryptos via @Service...");
        List<CryptoCurrency> cryptos = cryptoService.getPopularCryptos();
        System.out.println("   Result: " + cryptos.size() + " cryptocurrencies");

        // 2. @Component validation
        System.out.println("2. Validating crypto symbol via @Component...");
        String testSymbol = "ETH";
        boolean isValid = cryptoValidator.isValidCryptoSymbol(testSymbol);
        System.out.println("   " + testSymbol + " is valid: " + isValid);

        // 3. @Service using injected @Component
        System.out.println("3. Getting price via @Service (using @Component internally)...");
        double price = cryptoService.getCryptoPrice(testSymbol);
        System.out.println("   " + testSymbol + " price: $" + price);

        // 4. @Bean usage
        System.out.println("4. Using @Bean components...");
        String apiResponse = cryptoApiClient.fetchExternalData("https://api.crypto.com/prices");
        System.out.println("   API Client response: " + apiResponse);

        System.out.println("\n✅ ALL ANNOTATIONS WORKING TOGETHER PERFECTLY! 🎉");
    }
}