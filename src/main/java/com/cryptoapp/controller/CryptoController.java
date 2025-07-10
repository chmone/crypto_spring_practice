package com.cryptoapp.controller;

import com.cryptoapp.model.CryptoCurrency;
import com.cryptoapp.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 🌐 CRYPTO CONTROLLER - THE WEB LAYER OF OUR APPLICATION
 * 
 * ==== WHAT IS A CONTROLLER? ====
 * A Controller in Spring Boot is like a "receptionist" for your application.
 * When someone makes an HTTP request (like from a web browser or mobile app),
 * the Controller receives that request and decides what to do with it.
 * 
 * Think of it like this:
 * 1. User clicks a button on the webpage → HTTP request is sent
 * 2. Spring Boot receives the request → Routes it to the correct Controller method
 * 3. Controller processes the request → Calls business logic (Service layer)
 * 4. Controller returns a response → Gets sent back to the user
 * 
 * ==== REST API EXPLAINED ====
 * REST = Representational State Transfer
 * It's a way for different applications to talk to each other over HTTP.
 * Our frontend (HTML/JavaScript) talks to our backend (Spring Boot) using REST APIs.
 * 
 * HTTP Methods we use:
 * - GET: Retrieve data (like getting cryptocurrency prices)
 * - POST: Create new data (like adding a new crypto to watchlist)
 * - PUT: Update existing data
 * - DELETE: Remove data
 * 
 * ==== HOW SPRING BOOT MAGIC WORKS HERE ====
 * 1. Spring Boot scans this class and sees @RestController
 * 2. It automatically registers all the @GetMapping, @PostMapping methods as endpoints
 * 3. When HTTP requests come in, Spring Boot automatically:
 *    - Converts JSON to Java objects
 *    - Calls the right method
 *    - Converts Java objects back to JSON
 *    - Handles errors
 */

// ==== SPRING BOOT ANNOTATIONS EXPLAINED ====

/**
 * @RestController - THE MAIN WEB ANNOTATION 🌐
 * 
 *                 This tells Spring Boot: "This class handles HTTP requests and
 *                 returns data as JSON"
 * 
 *                 It's actually a combination of two annotations:
 *                 1. @Controller - Marks this as a web controller
 *                 2. @ResponseBody - Automatically converts return values to
 *                 JSON
 * 
 *                 Without @RestController, our methods would try to return HTML
 *                 pages instead of JSON data!
 */
@RestController

/**
 * @RequestMapping("/api/crypto") - THE BASE URL PATH 🛣️
 * 
 * This tells Spring Boot: "All methods in this class should start with
 * /api/crypto"
 * 
 * So if we have a method mapped to "/popular", the full URL becomes:
 * http://localhost:8080/api/crypto/popular
 * 
 * This helps organize our API endpoints logically:
 * - /api/crypto/popular
 * - /api/crypto/price/bitcoin
 * - /api/crypto/analytics/ethereum
 */
@RequestMapping("/api/crypto")

/**
 * @CrossOrigin(origins = "*") - ALLOWS FRONTEND TO CONNECT 🔓
 * 
 *                      This solves a common problem called "CORS" (Cross-Origin
 *                      Resource Sharing)
 * 
 *                      Web browsers block requests from one domain to another
 *                      for security.
 *                      Our frontend (localhost:8080/index.html) needs to call
 *                      our API (localhost:8080/api/crypto)
 *                      Even though it's the same server, browsers can sometimes
 *                      block this.
 * 
 * @CrossOrigin tells Spring Boot: "It's okay for browsers to call these
 *              endpoints"
 * 
 *              In production, you'd be more specific: @CrossOrigin(origins =
 *              "https://yourapp.com")
 */
@CrossOrigin(origins = "*")
public class CryptoController {

    // ==== DEPENDENCY INJECTION EXPLAINED ====

    /**
     * @Autowired - SPRING BOOT'S MAGIC DEPENDENCY INJECTION 🪄
     * 
     *            This is one of Spring Boot's most powerful features!
     *            Instead of writing: private CryptoService cryptoService = new
     *            CryptoService();
     * 
     *            We just say @Autowired and Spring Boot:
     *            1. Automatically creates a CryptoService instance
     *            2. Handles all the complex setup (database connections, API
     *            clients, etc.)
     *            3. "Injects" it into this field
     *            4. Manages the lifecycle (creation, destruction, etc.)
     * 
     *            This is called "Inversion of Control" - we don't control object
     *            creation,
     *            Spring Boot does it for us! This makes our code:
     *            - Easier to test (we can inject mock objects)
     *            - Easier to maintain (Spring handles all the wiring)
     *            - More flexible (easy to swap implementations)
     */
    @Autowired
    private CryptoService cryptoService;

    // ==== HTTP ENDPOINT METHODS ====

    /**
     * HEALTH CHECK ENDPOINT - IS OUR APPLICATION WORKING? 🏥
     * 
     * @GetMapping("/health") - This creates an HTTP GET endpoint at
     * /api/crypto/health
     * 
     * HTTP GET is used for retrieving data (like asking "how are you?")
     * When someone visits: http://localhost:8080/api/crypto/health
     * This method gets called automatically!
     * 
     * ResponseEntity<T> - This is Spring Boot's way of controlling HTTP responses
     * - We can set the HTTP status code (200 OK, 404 Not Found, 500 Error)
     * - We can set custom headers
     * - We can control the response body
     * 
     * Map<String, Object> - This gets automatically converted to JSON
     * Java Map becomes JSON object: {"status": "healthy", "message": "All good!"}
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            // Check if our CoinMarketCap API is configured
            boolean apiConfigured = cryptoService.getServiceConfig() != null;

            // Check if our database is working
            long dbCount = 0;
            boolean dbAvailable = false;
            try {
                List<CryptoCurrency> cryptos = cryptoService.getPopularCryptos();
                dbAvailable = true;

                // Try to get actual count from service config
                Object config = cryptoService.getServiceConfig();
                if (config != null) {
                    try {
                        java.lang.reflect.Field field = config.getClass().getField("totalCryptosInDatabase");
                        dbCount = (Long) field.get(config);
                    } catch (Exception e) {
                        dbCount = cryptos.size();
                    }
                }
            } catch (Exception e) {
                dbAvailable = false;
            }

            // Create a comprehensive health response
            // Map.of() creates an immutable map that gets converted to JSON
            Map<String, Object> healthResponse = Map.of(
                    "status", "healthy",
                    "message", "Crypto Backend is healthy! 💪",
                    "database", Map.of(
                            "available", dbAvailable,
                            "cryptocurrencies", dbCount,
                            "status", dbAvailable ? "✅ Connected" : "❌ Not connected"),
                    "api", Map.of(
                            "configured", apiConfigured,
                            "status", apiConfigured ? "✅ Configured" : "⚠️ Not configured"),
                    "cache", Map.of(
                            "enabled", dbAvailable,
                            "status", dbAvailable ? "✅ Enabled" : "❌ Disabled"),
                    "timestamp", System.currentTimeMillis());

            // ResponseEntity.ok() returns HTTP 200 status with the data
            return ResponseEntity.ok(healthResponse);

        } catch (Exception e) {
            // If anything goes wrong, return HTTP 500 (Internal Server Error)
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", "❌ Health check failed: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * GET POPULAR CRYPTOCURRENCIES - THE MAIN DATA ENDPOINT 💰
     * 
     * This endpoint returns the top cryptocurrencies (Bitcoin, Ethereum, etc.)
     * Our frontend dashboard calls this to display the crypto cards.
     * 
     * URL: GET /api/crypto/popular
     * Returns: JSON array of cryptocurrency objects
     * 
     * Example response:
     * [
     * {"symbol": "BTC", "name": "Bitcoin", "price": 50000.00, ...},
     * {"symbol": "ETH", "name": "Ethereum", "price": 3000.00, ...}
     * ]
     */
    @GetMapping("/popular")
    public ResponseEntity<List<CryptoCurrency>> getPopularCryptos() {
        try {
            System.out.println("🌐 Frontend requested /popular at " + java.time.LocalDateTime.now());

            // Call the service layer to get business logic
            // The service handles: database queries, API calls, caching, etc.
            List<CryptoCurrency> cryptos = cryptoService.getPopularCryptos();

            System.out.println("📤 Sending " + cryptos.size() + " cryptos to frontend");

            // Spring Boot automatically converts List<CryptoCurrency> to JSON array
            return ResponseEntity.ok(cryptos);

        } catch (Exception e) {
            System.err.println("❌ Error fetching popular cryptos: " + e.getMessage());
            // Return HTTP 500 with null body (frontend will handle this as an error)
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * GET FRESH DATA - BYPASS CACHE AND GET LATEST FROM API 🔄
     * 
     * Sometimes we want to force refresh the data instead of using cached data.
     * This endpoint forces a sync with CoinMarketCap API first, then returns data.
     */
    @GetMapping("/popular-fresh")
    public ResponseEntity<List<CryptoCurrency>> getPopularCryptosFresh() {
        try {
            System.out.println("🔄 Frontend requested FRESH data - forcing API call");

            // Force sync with external API first
            cryptoService.syncWithCoinMarketCap();

            // Now get the updated data from database
            List<CryptoCurrency> cryptos = cryptoService.getPopularCryptos();

            System.out.println("📤 Sending " + cryptos.size() + " FRESH cryptos to frontend");
            return ResponseEntity.ok(cryptos);

        } catch (Exception e) {
            System.err.println("❌ Error fetching fresh cryptos: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * GET SPECIFIC CRYPTO PRICE - USING PATH VARIABLES 📊
     * 
     * @PathVariable - EXTRACTING DATA FROM THE URL PATH
     * 
     *               This demonstrates how to get data from the URL itself.
     *               URL: GET /api/crypto/price/bitcoin
     *               The "bitcoin" part becomes the {symbol} parameter.
     * 
     * @PathVariable String symbol - tells Spring Boot to extract the {symbol}
     *               part from the URL and pass it as a method parameter.
     * 
     *               So if user calls: /api/crypto/price/ethereum
     *               Spring Boot automatically calls: getCryptoPrice("ethereum")
     */
    @GetMapping("/price/{symbol}")
    public ResponseEntity<Map<String, Object>> getCryptoPrice(@PathVariable String symbol) {
        try {
            // Ask the service layer for the price
            double price = cryptoService.getCryptoPrice(symbol);

            if (price > 0) {
                // Create a response object with price data
                return ResponseEntity.ok(Map.of(
                        "symbol", symbol.toUpperCase(),
                        "price", price,
                        "currency", "USD",
                        "timestamp", System.currentTimeMillis()));
            } else {
                // HTTP 404 - Not Found
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.err.println("❌ Error fetching price for " + symbol + ": " + e.getMessage());
            // HTTP 500 - Internal Server Error with error details
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to fetch price for " + symbol,
                    "message", e.getMessage()));
        }
    }

    /**
     * SEARCH CRYPTOCURRENCIES - USING QUERY PARAMETERS 🔍
     * 
     * @RequestParam - EXTRACTING DATA FROM QUERY PARAMETERS
     * 
     *               Query parameters are the ?key=value parts after the URL:
     *               URL: GET /api/crypto/search?q=bitcoin
     * 
     * @RequestParam(required = false) String q
     *                        - Extracts the "q" parameter from URL
     *                        - required = false means the parameter is optional
     *                        - If not provided, q will be null
     * 
     *                        So if user calls: /api/crypto/search?q=bit
     *                        Spring Boot automatically calls: searchCryptos("bit")
     */
    @GetMapping("/search")
    public ResponseEntity<List<CryptoCurrency>> searchCryptos(
            @RequestParam(required = false) String q) {
        try {
            // Call service layer to search for cryptocurrencies
            List<CryptoCurrency> results = cryptoService.searchCryptos(q);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            System.err.println("❌ Error searching cryptos: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * FORCE SYNC WITH API - USING POST METHOD 🔄
     * 
     * @PostMapping - HANDLES HTTP POST REQUESTS
     * 
     *              POST is typically used for actions that change something on the
     *              server.
     *              Here we're triggering a sync operation (which updates our
     *              database).
     * 
     *              URL: POST /api/crypto/sync
     *              This forces our application to fetch fresh data from
     *              CoinMarketCap API.
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncWithAPI() {
        return performSync();
    }

    /**
     * FORCE SYNC WITH API - USING GET METHOD (FOR EASY TESTING) 🔄
     * 
     * Sometimes it's convenient to trigger actions with GET requests
     * (easier to test in browser), even though POST is more "correct".
     */
    @GetMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncWithAPIGet() {
        return performSync();
    }

    /**
     * COMMON SYNC LOGIC - DRY PRINCIPLE 🛠️
     * 
     * DRY = Don't Repeat Yourself
     * Since both POST and GET sync methods do the same thing,
     * we extract the common logic into a separate method.
     */
    private ResponseEntity<Map<String, Object>> performSync() {
        try {
            // Trigger the actual sync operation in the service layer
            cryptoService.syncWithCoinMarketCap();

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "✅ Successfully synced with CoinMarketCap API",
                    "timestamp", System.currentTimeMillis()));

        } catch (Exception e) {
            System.err.println("❌ Error syncing with API: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "error", "Failed to sync with API",
                    "message", e.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        }
    }

    /**
     * GET APPLICATION CONFIGURATION 🔧
     * 
     * This endpoint returns information about how our application is configured.
     * Useful for debugging and monitoring.
     */
    @GetMapping("/config")
    public ResponseEntity<Object> getConfig() {
        try {
            Object config = cryptoService.getServiceConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to get configuration",
                    "message", e.getMessage()));
        }
    }

    /**
     * GET TOP 5 CRYPTOCURRENCIES - SPECIFIC SUBSET 🏆
     * 
     * Sometimes we want just the top few instead of all popular cryptos.
     */
    @GetMapping("/top5")
    public ResponseEntity<List<CryptoCurrency>> getTop5Cryptos() {
        try {
            // Get popular cryptos and limit to first 5
            List<CryptoCurrency> allPopular = cryptoService.getPopularCryptos();
            List<CryptoCurrency> top5 = allPopular.stream()
                    .limit(5)
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(top5);
        } catch (Exception e) {
            System.err.println("❌ Error fetching top 5 cryptos: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * CALCULATE PORTFOLIO VALUE - USING REQUEST BODY 💼
     * 
     * @RequestBody - EXTRACTING DATA FROM HTTP REQUEST BODY
     * 
     *              Sometimes we need to send complex data (not just simple
     *              parameters).
     *              The frontend sends a JSON array in the request body:
     *              ["BTC", "ETH", "ADA"]
     * 
     * @RequestBody List<String> cryptoSymbols
     *              - Spring Boot automatically converts the JSON array to Java
     *              List<String>
     *              - This is the reverse of what happens with responses (Java →
     *              JSON)
     */
    @PostMapping("/portfolio/value")
    public ResponseEntity<Map<String, Object>> calculatePortfolioValue(
            @RequestBody List<String> cryptoSymbols) {
        try {
            double totalValue = cryptoService.calculatePortfolioValue(cryptoSymbols);

            return ResponseEntity.ok(Map.of(
                    "symbols", cryptoSymbols,
                    "totalValue", totalValue,
                    "currency", "USD",
                    "timestamp", System.currentTimeMillis()));

        } catch (Exception e) {
            System.err.println("❌ Error calculating portfolio value: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to calculate portfolio value",
                    "message", e.getMessage()));
        }
    }

    /**
     * GET CRYPTO DETAILS - SPECIFIC CRYPTO INFORMATION 📋
     * 
     * Returns detailed information about a specific cryptocurrency.
     */
    @GetMapping("/details/{symbol}")
    public ResponseEntity<CryptoCurrency> getCryptoDetails(@PathVariable String symbol) {
        try {
            // Search for the crypto by symbol and return the first match
            List<CryptoCurrency> results = cryptoService.searchCryptos(symbol);

            if (!results.isEmpty()) {
                return ResponseEntity.ok(results.get(0));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.err.println("❌ Error fetching details for " + symbol + ": " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * GET CRYPTO ANALYTICS - ADVANCED DATA ANALYSIS 📈
     * 
     * This endpoint provides detailed analytics for the analytics dashboard.
     * It returns statistical data, performance metrics, and historical charts.
     */
    @GetMapping("/analytics/{symbol}")
    public ResponseEntity<Map<String, Object>> getCryptoAnalytics(@PathVariable String symbol) {
        try {
            Map<String, Object> analytics = cryptoService.getCryptoAnalytics(symbol);

            if (analytics != null && !analytics.isEmpty()) {
                return ResponseEntity.ok(analytics);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.err.println("❌ Error fetching analytics for " + symbol + ": " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to fetch analytics for " + symbol,
                    "message", e.getMessage()));
        }
    }

    /**
     * GET APPLICATION INFO - METADATA ABOUT OUR APP ℹ️
     * 
     * Returns general information about our application.
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getAppInfo() {
        return ResponseEntity.ok(Map.of(
                "appName", "Crypto Analytics Backend",
                "version", "2.0.0",
                "description", "Spring Boot application with real CoinMarketCap API integration",
                "features", List.of(
                        "Real-time cryptocurrency data",
                        "Supabase database storage",
                        "CoinMarketCap API integration",
                        "Automatic data synchronization",
                        "Search functionality",
                        "Analytics dashboard",
                        "Portfolio calculations"),
                "endpoints", Map.of(
                        "health", "/api/crypto/health",
                        "popular", "/api/crypto/popular",
                        "search", "/api/crypto/search?q=bitcoin",
                        "price", "/api/crypto/price/{symbol}",
                        "analytics", "/api/crypto/analytics/{symbol}",
                        "sync", "/api/crypto/sync"),
                "timestamp", System.currentTimeMillis()));
    }
}

/**
 * 🎓 KEY SPRING BOOT CONCEPTS DEMONSTRATED IN THIS CONTROLLER:
 * 
 * 1. ANNOTATION-DRIVEN DEVELOPMENT
 * - @RestController, @RequestMapping, @GetMapping, @PostMapping
 * - Annotations tell Spring Boot what to do without complex configuration
 * 
 * 2. DEPENDENCY INJECTION
 * - @Autowired automatically provides dependencies
 * - We don't manually create objects, Spring Boot manages them
 * 
 * 3. REQUEST MAPPING
 * - Different HTTP methods (GET, POST) mapped to Java methods
 * - URL paths with variables: /price/{symbol}
 * 
 * 4. PARAMETER BINDING
 * - @PathVariable: Extract data from URL path
 * - @RequestParam: Extract data from query parameters (?key=value)
 * - @RequestBody: Extract data from HTTP request body (JSON)
 * 
 * 5. RESPONSE HANDLING
 * - ResponseEntity: Control HTTP status codes and headers
 * - Automatic JSON conversion: Java objects → JSON responses
 * 
 * 6. ERROR HANDLING
 * - Try-catch blocks with appropriate HTTP status codes
 * - Consistent error response format
 * 
 * 7. SEPARATION OF CONCERNS
 * - Controller handles HTTP requests/responses
 * - Service layer handles business logic
 * - Repository layer handles database operations
 * 
 * This is the Spring Boot way: minimal code, maximum functionality! 🚀
 */