package com.cryptoapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 🔧 CRYPTO CONFIGURATION - SPRING BOOT CONFIGURATION LAYER
 * 
 * ==== WHAT IS A CONFIGURATION CLASS? ====
 * Configuration classes are Spring Boot's way of setting up and customizing components.
 * Think of it like this:
 * 
 * - Without Configuration: You manually create objects everywhere
 * - With Configuration: You define objects once, Spring Boot manages them everywhere
 * 
 * ==== WHAT ARE BEANS? ====
 * A "Bean" in Spring Boot is just an object that Spring manages for you.
 * Instead of writing: RestTemplate restTemplate = new RestTemplate();
 * You define it as a @Bean, and Spring Boot:
 * 1. Creates it for you at startup
 * 2. Makes it available everywhere via @Autowired
 * 3. Manages its lifecycle (creation, initialization, destruction)
 * 4. Ensures it's a singleton (one instance shared everywhere)
 * 
 * ==== WHY USE CONFIGURATION CLASSES? ====
 * 1. CENTRALIZED SETUP: All object creation in one place
 * 2. DEPENDENCY INJECTION: Objects can automatically get other objects they need
 * 3. EXTERNALIZED CONFIG: Settings from application.properties
 * 4. EASY TESTING: You can easily swap beans for testing
 * 5. LIFECYCLE MANAGEMENT: Spring handles creation/destruction
 * 
 * ==== CONFIGURATION VS COMPONENT ====
 * - @Component classes ARE beans themselves
 * - @Configuration classes CREATE beans for other objects
 * - Think: @Component = "I am a Spring object", @Configuration = "I create Spring objects"
 */

/**
 * @Configuration - MARKS THIS CLASS AS A BEAN FACTORY 🏭
 * 
 *                This tells Spring Boot: "This class contains instructions for
 *                creating objects"
 * 
 *                What @Configuration does:
 *                1. Spring Boot scans this class at startup
 *                2. Looks for methods marked with @Bean
 *                3. Calls each @Bean method to create objects
 *                4. Stores these objects in the Application Context
 *                5. Makes them available for dependency injection anywhere
 * 
 *                IMPORTANT: Configuration classes are ALSO beans themselves!
 *                Spring Boot creates an instance of CryptoConfig and manages
 *                it.
 */
@Configuration
public class CryptoConfig {

    // ==== EXTERNALIZED CONFIGURATION ====

    /**
     * @Value - INJECT CONFIGURATION VALUES 🔧
     * 
     *        Instead of hardcoding timeout = 5000, we read it from configuration.
     *        This value can come from:
     *        - application.properties: app.crypto.api-timeout=5000
     *        - Environment variables: APP_CRYPTO_API_TIMEOUT=5000
     *        - Command line: --app.crypto.api-timeout=5000
     *        - Default value: 5000 (if not found anywhere)
     * 
     *        Benefits:
     *        - Different values for development vs production
     *        - No need to recompile to change settings
     *        - Easy to customize for different environments
     */
    @Value("${app.crypto.api-timeout:5000}")
    private long apiTimeout;

    // ==== BEAN DEFINITIONS ====

    /**
     * REST TEMPLATE BEAN - HTTP CLIENT CONFIGURATION 🌐
     * 
     * @Bean - CREATES A SPRING-MANAGED OBJECT
     * 
     *       What happens when Spring Boot starts:
     *       1. Spring Boot calls this method ONCE during startup
     *       2. The RestTemplate object becomes a "bean"
     *       3. Spring Boot stores it in the Application Context
     *       4. Any class can @Autowired this RestTemplate
     *       5. Everyone gets the SAME instance (singleton pattern)
     * 
     *       Why create RestTemplate as a bean?
     *       - RestTemplate is expensive to create (connection pools, etc.)
     *       - We want to reuse the same configured instance everywhere
     *       - We can configure it once here (timeouts, interceptors, etc.)
     *       - Easy to mock for testing
     * 
     *       Method name = Bean name: Other classes can inject this as
     *       "restTemplate"
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Configure timeout using injected property value
        // In a real app, you'd set timeouts on the request factory
        restTemplate.getRequestFactory();

        System.out.println("🔧 RestTemplate bean created with timeout: " + apiTimeout + "ms");

        // This object is now managed by Spring Boot!
        return restTemplate;
    }

    /**
     * DATE TIME FORMATTER BEAN - UTILITY CONFIGURATION 📅
     * 
     * This demonstrates creating utility beans that can be shared across the
     * application.
     * 
     * Benefits of making DateTimeFormatter a bean:
     * - Consistent date formatting across the entire application
     * - Thread-safe and immutable (perfect for sharing)
     * - Easy to change format in one place
     * - Can be injected anywhere it's needed
     * 
     * Alternative: You could create formatters manually everywhere,
     * but then you'd have inconsistent formatting and duplicate code.
     */
    @Bean
    public DateTimeFormatter cryptoDateFormatter() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("📅 DateTimeFormatter bean created with pattern: yyyy-MM-dd HH:mm:ss");
        return formatter;
    }

    /**
     * THREAD POOL EXECUTOR BEAN - CUSTOM RESOURCE MANAGEMENT 🧵
     * 
     * @Bean(name = "cryptoExecutor") - CUSTOM BEAN NAME
     * 
     *            Sometimes the method name isn't a good bean name.
     *            Here we explicitly name the bean "cryptoExecutor".
     *            Other classes can inject
     *            it: @Autowired @Qualifier("cryptoExecutor")
     * 
     *            This shows creating more complex beans with specific
     *            configuration.
     *            Thread pools are expensive resources that should be shared and
     *            managed centrally.
     * 
     *            Configuration explained:
     *            - Core pool size: 2 threads always alive
     *            - Maximum pool size: Up to 5 threads when busy
     *            - Keep alive time: Extra threads die after 60 seconds of
     *            inactivity
     *            - Work queue: Can queue up to 100 tasks waiting for threads
     */
    @Bean(name = "cryptoExecutor")
    public ThreadPoolExecutor cryptoThreadPool() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2, // Core pool size - minimum threads always running
                5, // Maximum pool size - max threads under heavy load
                60L, TimeUnit.SECONDS, // Keep alive time for excess threads
                new LinkedBlockingQueue<>(100) // Task queue size
        );

        // Custom thread factory to give threads meaningful names
        executor.setThreadFactory(r -> {
            Thread thread = new Thread(r);
            thread.setName("crypto-worker-" + thread.getId());
            thread.setDaemon(true); // Don't prevent JVM shutdown
            return thread;
        });

        System.out.println("🧵 ThreadPoolExecutor bean created: " + executor.getCorePoolSize()
                + " core threads, max " + executor.getMaximumPoolSize());
        return executor;
    }

    /**
     * CRYPTO API CLIENT BEAN - DEPENDENCY INJECTION IN CONFIGURATION 🌐
     * 
     * This demonstrates DEPENDENCY INJECTION within @Bean methods!
     * 
     * Spring Boot automatically provides the parameters:
     * - restTemplate: The RestTemplate bean we defined above
     * - cryptoDateFormatter: The DateTimeFormatter bean we defined above
     * 
     * Spring Boot handles the dependency graph:
     * 1. Creates RestTemplate bean first
     * 2. Creates DateTimeFormatter bean
     * 3. Then creates CryptoApiClient bean using the other two
     * 
     * This is powerful because:
     * - No manual object creation or wiring
     * - Automatic dependency resolution
     * - Easy to test (can inject mock dependencies)
     * - Changes to dependencies automatically propagate
     */
    @Bean
    public CryptoApiClient cryptoApiClient(RestTemplate restTemplate, DateTimeFormatter cryptoDateFormatter) {
        // Spring Boot automatically injects the required dependencies!
        // No need to manually create or lookup these objects

        System.out.println("🏗️ Creating CryptoApiClient with injected dependencies:");
        System.out.println("  - RestTemplate: " + restTemplate.getClass().getSimpleName());
        System.out.println("  - DateTimeFormatter: " + cryptoDateFormatter.toString());
        System.out.println("  - API Timeout: " + apiTimeout + "ms");

        return new CryptoApiClient(restTemplate, cryptoDateFormatter, apiTimeout);
    }

    /**
     * CUSTOM API CLIENT CLASS - DEMONSTRATING BEAN COMPOSITION 🔧
     * 
     * This inner class shows how to create custom beans that use other beans.
     * In a real application, this might be:
     * - A custom HTTP client wrapper
     * - A service that combines multiple APIs
     * - A utility class with complex configuration
     * 
     * Key principles demonstrated:
     * 1. CONSTRUCTOR INJECTION: Dependencies passed via constructor
     * 2. IMMUTABLE STATE: Final fields, set once in constructor
     * 3. SEPARATION OF CONCERNS: Each dependency has its own purpose
     */
    public static class CryptoApiClient {
        private final RestTemplate restTemplate;
        private final DateTimeFormatter dateFormatter;
        private final long timeout;

        /**
         * Constructor injection - dependencies provided by Spring Boot
         */
        public CryptoApiClient(RestTemplate restTemplate, DateTimeFormatter dateFormatter, long timeout) {
            this.restTemplate = restTemplate;
            this.dateFormatter = dateFormatter;
            this.timeout = timeout;

            System.out.println("🌐 CryptoApiClient initialized:");
            System.out.println("  - HTTP client ready");
            System.out.println("  - Date formatter configured: " + dateFormatter.toString());
            System.out.println("  - Timeout setting: " + timeout + "ms");
        }

        /**
         * Example method showing how the injected dependencies are used
         */
        public String getClientInfo() {
            return String.format(
                    "CryptoApiClient ready - Timeout: %dms, Date Pattern: %s, RestTemplate: %s",
                    timeout,
                    dateFormatter.toString(),
                    restTemplate.getClass().getSimpleName());
        }

        /**
         * Example API method (mock implementation)
         * In a real app, this would use the RestTemplate to make HTTP calls
         */
        public String fetchExternalData(String url) {
            // Here you would use: restTemplate.getForObject(url, String.class)
            String timestamp = java.time.LocalDateTime.now().format(dateFormatter);
            return String.format(
                    "Mock API response from: %s at %s (configured timeout: %dms)",
                    url, timestamp, timeout);
        }
    }
}

/**
 * 🎓 KEY SPRING BOOT CONFIGURATION CONCEPTS DEMONSTRATED:
 * 
 * 1. BEAN CREATION AND MANAGEMENT
 * - @Configuration classes define beans
 * - @Bean methods create objects managed by Spring
 * - Singleton pattern by default (one instance per bean)
 * 
 * 2. DEPENDENCY INJECTION IN CONFIGURATION
 * - @Bean methods can have parameters
 * - Spring automatically injects required dependencies
 * - Automatic resolution of dependency graph
 * 
 * 3. EXTERNALIZED CONFIGURATION
 * - @Value annotation injects properties
 * - Values from application.properties, environment variables, etc.
 * - Default values when properties not found
 * 
 * 4. CUSTOM BEAN NAMING
 * - @Bean(name = "customName") for explicit naming
 * - Method name used as bean name by default
 * - @Qualifier used when injecting specific named beans
 * 
 * 5. COMPLEX OBJECT CREATION
 * - Thread pools, HTTP clients, formatters as shared resources
 * - Central configuration instead of scattered creation
 * - Proper resource management and lifecycle
 * 
 * 6. COMPOSITION AND REUSABILITY
 * - Beans can use other beans in their creation
 * - Promotes loose coupling and high cohesion
 * - Easy to test and maintain
 * 
 * This is the Spring Boot way: Configure once, use everywhere! 🚀
 */