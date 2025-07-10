package com.cryptoapp.service;

import com.cryptoapp.component.CryptoValidator;
import com.cryptoapp.model.CryptoCurrency;
import com.cryptoapp.model.CoinMarketCapResponse;
import com.cryptoapp.repository.CryptoCurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 🏢 CRYPTO SERVICE - THE BUSINESS LOGIC LAYER OF OUR APPLICATION
 * 
 * ==== WHAT IS A SERVICE IN SPRING BOOT? ====
 * A Service is the "brain" of your application - it contains all the business logic.
 * Think of it like this:
 * 
 * - Controller = Receptionist (handles requests, talks to customers)
 * - Service = Manager (makes business decisions, coordinates work)  
 * - Repository = File Clerk (stores and retrieves data)
 * 
 * The Service layer is where you put:
 * - Complex calculations
 * - Business rules and validation
 * - Coordination between different data sources
 * - Transaction management
 * - Integration with external APIs
 * 
 * ==== SEPARATION OF CONCERNS ====
 * This is a key Spring Boot principle:
 * 1. CONTROLLER: "What HTTP endpoint was called?"
 * 2. SERVICE: "What business logic should happen?" 
 * 3. REPOSITORY: "How do I store/retrieve data?"
 * 
 * Each layer has ONE responsibility. This makes code:
 * - Easier to test (you can test business logic separately from web layer)
 * - Easier to maintain (changes in one layer don't break others)
 * - More reusable (multiple controllers can use the same service)
 * 
 * ==== DEPENDENCY INJECTION IN ACTION ====
 * This service uses several other components:
 * - CryptoValidator: Validates cryptocurrency data
 * - CoinMarketCapApiService: Calls external API
 * - CryptoCurrencyRepository: Database operations
 * 
 * Spring Boot automatically creates and "injects" these dependencies!
 */

// ==== SPRING BOOT ANNOTATIONS EXPLAINED ====

/**
 * @Service - MARKS THIS AS A BUSINESS LOGIC COMPONENT 🏢
 * 
 *          This tells Spring Boot: "This class contains business logic"
 * 
 *          What @Service does:
 *          1. Spring Boot automatically creates an instance of this class
 *          2. Makes it available for dependency injection (@Autowired)
 *          3. Manages its lifecycle (creation, destruction)
 *          4. Enables transaction management
 *          5. Enables AOP (Aspect-Oriented Programming) features
 * 
 * @Service is actually a specialized form of @Component
 *          It's the same as @Component but tells other developers: "This is
 *          business logic"
 */
@Service

/**
 * @Transactional - DATABASE TRANSACTION MANAGEMENT 💾
 * 
 *                This is SUPER IMPORTANT for database operations!
 * 
 *                What is a transaction?
 *                A transaction is a group of database operations that either:
 *                - ALL succeed together, OR
 *                - ALL fail together (and get rolled back)
 * 
 *                Example: Transferring money between bank accounts
 *                1. Subtract $100 from Account A
 *                2. Add $100 to Account B
 *                If step 2 fails, step 1 gets rolled back automatically!
 * 
 * @Transactional at class level means:
 *                - Every public method in this class runs in a transaction
 *                - If any exception occurs, changes get rolled back
 *                - Database connections are managed automatically
 */
@Transactional
public class CryptoService {

    // ==== CONFIGURATION VALUES - EXTERNALIZED CONFIGURATION ====

    /**
     * @Value - INJECTING CONFIGURATION VALUES 🔧
     * 
     *        Instead of hardcoding values in our code, we can configure them
     *        externally!
     *        These values come from:
     *        - application.properties file
     *        - Environment variables
     *        - Command line arguments
     *        - External configuration servers
     * 
     *        Format: @Value("${property.name:default-value}")
     *        - ${app.crypto.max-results} = get this property
     *        - :10 = if not found, use default value 10
     * 
     *        This makes our application configurable without recompiling!
     *        Production might use maxResults=50, while development uses 10.
     */
    @Value("${app.crypto.max-results:10}")
    private int maxResults;

    @Value("${app.crypto.api-timeout:5000}")
    private long apiTimeout;

    @Value("${app.crypto.default-currency:USD}")
    private String defaultCurrency;

    @Value("${app.crypto.cache-enabled:true}")
    private boolean cacheEnabled;

    @Value("${app.crypto.sync-interval:300000}")
    private long syncInterval;

    // ==== DEPENDENCY INJECTION - SPRING BOOT'S MAGIC ====

    /**
     * @Autowired - AUTOMATIC DEPENDENCY INJECTION 🪄
     * 
     *            These are our "dependencies" - other objects this service needs to
     *            work.
     *            Instead of creating them manually:
     *            cryptoValidator = new CryptoValidator();
     * 
     *            Spring Boot does it automatically:
     *            1. Finds classes marked with @Component, @Service, @Repository
     *            2. Creates instances of them
     *            3. "Injects" them into classes that need them
     *            4. Handles all the complex wiring between objects
     * 
     *            This is called "Inversion of Control" - we don't control object
     *            creation,
     *            Spring Boot does! This makes testing and maintenance much easier.
     */
    @Autowired
    private CryptoValidator cryptoValidator;

    @Autowired
    private CoinMarketCapApiService coinMarketCapApiService;

    @Autowired
    private CryptoCurrencyRepository cryptoCurrencyRepository;

    // ==== BUSINESS LOGIC METHODS ====

    /**
     * GET POPULAR CRYPTOCURRENCIES - MAIN BUSINESS LOGIC 💰
     * 
     * This method demonstrates several key Spring Boot patterns:
     * 1. FALLBACK STRATEGY: Try database first, then API, then mock data
     * 2. ERROR HANDLING: Graceful degradation when things fail
     * 3. CACHING: Use database as cache for API data
     * 4. CONFIGURATION: Behavior controlled by properties
     * 
     * Business Rules:
     * - Return cached data if available (fast)
     * - Fall back to API if cache is empty (slower but fresh)
     * - Fall back to mock data if API fails (always works)
     */
    public List<CryptoCurrency> getPopularCryptos() {
        System.out.println("📊 Fetching popular cryptos (max results: " + maxResults + ")");

        // ==== STRATEGY 1: TRY DATABASE CACHE FIRST ====
        // Check if database is available and caching is enabled
        if (cryptoCurrencyRepository != null && cacheEnabled) {
            try {
                // Call the repository layer to get cached data
                List<CryptoCurrency> fromDatabase = cryptoCurrencyRepository.findTop10LatestByOrderByCmcRankAsc();

                if (!fromDatabase.isEmpty()) {
                    System.out.println("✅ Returning " + fromDatabase.size() + " cryptos from database cache");

                    // DEBUG: Log what data we're actually returning to frontend
                    System.out.println("🔍 Data being returned to frontend:");
                    fromDatabase.stream().limit(3).forEach(crypto -> {
                        System.out.println("  " + crypto.getSymbol() + ": $" + crypto.getCurrentPrice()
                                + " (ID: " + crypto.getId() + ", created: " + crypto.getCreatedAt() + ")");
                    });

                    // Use Java 8 Streams to limit results and convert to List
                    return fromDatabase.stream()
                            .limit(maxResults)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                // Graceful degradation: if database fails, continue to next strategy
                System.out.println("⚠️ Database not available, using API only: " + e.getMessage());
            }
        }

        // ==== STRATEGY 2: TRY EXTERNAL API ====
        // Check if API key is configured
        if (coinMarketCapApiService.isApiKeyConfigured()) {
            try {
                System.out.println("🔄 Fetching from CoinMarketCap API...");

                // Call external API using reactive programming (Mono)
                // Mono = asynchronous, non-blocking operation
                Mono<CoinMarketCapResponse> responseMono = coinMarketCapApiService.getLatestListings(maxResults,
                        defaultCurrency);

                // .block() = wait for the async operation to complete and get result
                CoinMarketCapResponse response = responseMono.block();

                if (response != null && response.getData() != null) {
                    // Convert API response to our internal model using Stream API
                    List<CryptoCurrency> cryptos = response.getData().stream()
                            .map(this::convertToCryptoCurrency) // Convert each API object to our model
                            .limit(maxResults) // Limit to configured max
                            .collect(Collectors.toList()); // Collect into List

                    System.out.println("✅ Returning " + cryptos.size() + " cryptocurrencies from API");
                    return cryptos;
                }
            } catch (Exception e) {
                // Log error but continue to fallback strategy
                System.err.println("❌ Error fetching from API: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ CoinMarketCap API key not configured");
        }

        // ==== STRATEGY 3: FALLBACK TO MOCK DATA ====
        // Always works - ensures our application never completely fails
        System.out.println("📝 Using mock data (API key not configured)");
        return getMockCryptoData();
    }

    /**
     * GET CRYPTOCURRENCY PRICE BY SYMBOL - FLEXIBLE PRICE LOOKUP 💲
     * 
     * This method demonstrates:
     * 1. INPUT VALIDATION: Using injected validator component
     * 2. MULTIPLE DATA SOURCES: Database → API → Mock data
     * 3. BUSINESS RULES: What constitutes a valid price
     * 4. ERROR HANDLING: Graceful fallback when lookups fail
     */
    public double getCryptoPrice(String symbol) {
        // Use injected validator to normalize and validate input
        String normalizedSymbol = cryptoValidator.normalizeCryptoSymbol(symbol);

        if (!cryptoValidator.isValidCryptoSymbol(normalizedSymbol)) {
            System.out.println("❌ Invalid crypto symbol: " + symbol);
            return 0.0;
        }

        // ==== STRATEGY 1: CHECK DATABASE CACHE ====
        if (cryptoCurrencyRepository != null) {
            try {
                // Repository method returns Optional<T> to handle "not found" safely
                Optional<CryptoCurrency> fromDb = cryptoCurrencyRepository
                        .findLatestBySymbolIgnoreCase(normalizedSymbol);

                if (fromDb.isPresent()) {
                    CryptoCurrency crypto = fromDb.get();
                    double price = crypto.getCurrentPrice() != null ? crypto.getCurrentPrice() : 0.0;

                    // Business rule: validate price before returning
                    if (cryptoValidator.isValidPrice(price)) {
                        System.out.println("💰 Found " + normalizedSymbol + " price in database: $" + price);
                        return price;
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ Database not available for price lookup: " + e.getMessage());
            }
        }

        // ==== STRATEGY 2: CALL EXTERNAL API ====
        if (coinMarketCapApiService.isApiKeyConfigured()) {
            try {
                System.out.println("🔍 " + normalizedSymbol + " checking API...");

                // Reactive call to external API
                Mono<CoinMarketCapResponse> responseMono = coinMarketCapApiService.getLatestQuotes(normalizedSymbol,
                        defaultCurrency);
                CoinMarketCapResponse response = responseMono.block();

                if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                    CoinMarketCapResponse.CoinMarketCapCrypto crypto = response.getData().get(0);
                    if (crypto.getQuote() != null && crypto.getQuote().containsKey("USD")) {
                        double price = crypto.getQuote().get("USD").getPrice();
                        System.out.println("✅ Retrieved " + normalizedSymbol + " price from API: $" + price);
                        return price;
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Error fetching " + normalizedSymbol + " from API: " + e.getMessage());
            }
        }

        // ==== STRATEGY 3: FALLBACK TO MOCK PRICES ====
        double mockPrice = getMockPrice(normalizedSymbol);
        if (mockPrice > 0) {
            System.out.println("📝 Using mock price for " + normalizedSymbol + ": $" + mockPrice);
            return mockPrice;
        }

        System.out.println("❌ Could not find price for " + normalizedSymbol);
        return 0.0;
    }

    /**
     * CALCULATE PORTFOLIO VALUE - BUSINESS LOGIC EXAMPLE 💼
     * 
     * This demonstrates how business logic works in services:
     * 1. Take a list of crypto symbols
     * 2. Get the price for each one
     * 3. Sum them up to get total portfolio value
     * 
     * Uses Java 8 Stream API for functional programming:
     * - .stream() = convert List to Stream for processing
     * - .mapToDouble() = transform each symbol to its price
     * - .sum() = add up all the prices
     */
    public double calculatePortfolioValue(List<String> cryptoSymbols) {
        return cryptoSymbols.stream()
                .mapToDouble(this::getCryptoPrice) // Method reference: same as symbol -> getCryptoPrice(symbol)
                .sum();
    }

    /**
     * SEARCH CRYPTOCURRENCIES - FLEXIBLE SEARCH FUNCTIONALITY 🔍
     * 
     * This method shows database fallback pattern:
     * 1. Try database search first (fast, supports complex queries)
     * 2. Fall back to in-memory filtering (slower but always works)
     */
    public List<CryptoCurrency> searchCryptos(String searchTerm) {
        // Try database search if available
        if (cryptoCurrencyRepository != null) {
            try {
                if (searchTerm == null || searchTerm.trim().isEmpty()) {
                    // Return top cryptos if no search term
                    return cryptoCurrencyRepository.findTop10LatestByOrderByCmcRankAsc();
                }
                // Use repository's search method
                return cryptoCurrencyRepository.searchLatestByNameOrSymbol(searchTerm.trim());
            } catch (Exception e) {
                System.out.println("⚠️ Database not available for search: " + e.getMessage());
            }
        }

        // Fallback: filter popular cryptos in memory
        List<CryptoCurrency> allCryptos = getPopularCryptos();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return allCryptos;
        }

        // Use Stream API to filter based on name or symbol
        String lowerSearchTerm = searchTerm.toLowerCase();
        return allCryptos.stream()
                .filter(crypto -> crypto.getName().toLowerCase().contains(lowerSearchTerm) ||
                        crypto.getSymbol().toLowerCase().contains(lowerSearchTerm))
                .collect(Collectors.toList());
    }

    // ==== SCHEDULED TASKS - BACKGROUND AUTOMATION ====

    /**
     * SYNC WITH COINMARKETCAP API - MANUAL SYNC OPERATION 🔄
     * 
     * This method can be called manually (from controller endpoints)
     * to force a fresh sync with the external API.
     * 
     * Business Logic:
     * 1. Check if API is configured
     * 2. Fetch fresh data from CoinMarketCap
     * 3. Save to database for caching
     * 4. Handle errors gracefully
     */
    public void syncWithCoinMarketCap() {
        if (!coinMarketCapApiService.isApiKeyConfigured()) {
            System.out.println("⚠️ Cannot sync - CoinMarketCap API key not configured");
            return;
        }

        if (cryptoCurrencyRepository == null) {
            System.out.println("⚠️ Cannot sync - Database not available");
            return;
        }

        try {
            System.out.println("🔄 Starting manual sync with CoinMarketCap API...");

            // Fetch fresh data from API
            Mono<CoinMarketCapResponse> responseMono = coinMarketCapApiService.getLatestListings(50, "USD");
            CoinMarketCapResponse response = responseMono.block();

            if (response != null && response.getData() != null) {
                System.out.println("📥 Received " + response.getData().size() + " cryptos from API");

                int savedCount = 0;
                // Process each crypto from API
                for (CoinMarketCapResponse.CoinMarketCapCrypto apiCrypto : response.getData()) {
                    try {
                        // Convert API model to our domain model
                        CryptoCurrency crypto = convertToCryptoCurrency(apiCrypto);

                        // Save individually to handle partial failures
                        if (saveCryptoCurrencyIndividually(crypto)) {
                            savedCount++;
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error saving " + apiCrypto.getSymbol() + ": " + e.getMessage());
                    }
                }

                System.out.println("✅ Manual sync completed: " + savedCount + " cryptocurrencies saved");
            } else {
                System.out.println("❌ No data received from CoinMarketCap API");
            }
        } catch (Exception e) {
            System.err.println("❌ Manual sync failed: " + e.getMessage());
            throw new RuntimeException("Sync operation failed", e);
        }
    }

    /**
     * AUTOMATIC BACKGROUND SYNC - SCHEDULED TASK ⏰
     * 
     * @Scheduled - AUTOMATIC TASK EXECUTION
     * 
     *            This method runs automatically in the background!
     *            fixedRateString = "${app.crypto.sync-interval:300000}"
     *            - Reads interval from configuration (default 300000ms = 5 minutes)
     *            - Spring Boot calls this method automatically at that interval
     *            - Runs in a separate thread, doesn't block the main application
     * 
     * @Transactional(propagation = Propagation.NOT_SUPPORTED)
     *                            - This overrides the class-level @Transactional
     *                            - NOT_SUPPORTED = don't run this in a transaction
     *                            - Why? Long-running background tasks shouldn't
     *                            hold database transactions
     */
    @Scheduled(fixedRateString = "${app.crypto.sync-interval:300000}")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void automaticBackgroundSync() {
        try {
            System.out.println("⏰ Starting automatic background sync...");
            syncWithCoinMarketCap();
        } catch (Exception e) {
            // Don't let background task failures crash the application
            System.err.println("❌ Automatic background sync failed: " + e.getMessage());
        }
    }

    // ==== HELPER METHODS - PRIVATE BUSINESS LOGIC ====

    /**
     * SAVE CRYPTOCURRENCY INDIVIDUALLY - TRANSACTION MANAGEMENT 💾
     * 
     * @Transactional(propagation = Propagation.REQUIRES_NEW)
     *                            - REQUIRES_NEW = always create a new transaction
     *                            for this method
     *                            - Even if called from another transaction, start a
     *                            fresh one
     *                            - This allows individual saves to succeed/fail
     *                            independently
     * 
     *                            Why is this important?
     *                            If we're saving 50 cryptos and #25 fails, we don't
     *                            want to lose 1-24!
     *                            Each crypto gets its own transaction, so failures
     *                            are isolated.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean saveCryptoCurrencyIndividually(CryptoCurrency crypto) {
        try {
            // Use repository to save to database
            cryptoCurrencyRepository.save(crypto);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to save " + crypto.getSymbol() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * CONVERT API MODEL TO DOMAIN MODEL - DATA TRANSFORMATION 🔄
     * 
     * This is a common pattern in Spring Boot applications:
     * External APIs use different data structures than our internal models.
     * We need to convert between them.
     * 
     * Why not use the API model directly?
     * 1. API models change when external APIs change
     * 2. Our domain model represents OUR business needs
     * 3. We might combine data from multiple APIs
     * 4. We might need different validation rules
     */
    private CryptoCurrency convertToCryptoCurrency(CoinMarketCapResponse.CoinMarketCapCrypto apiCrypto) {
        CryptoCurrency crypto = new CryptoCurrency();

        // Basic mapping from API fields to our model
        crypto.setCoinMarketCapId(apiCrypto.getId());
        crypto.setName(apiCrypto.getName());
        crypto.setSymbol(apiCrypto.getSymbol());
        crypto.setCmcRank(apiCrypto.getCmcRank());

        // Extract USD quote data if available
        if (apiCrypto.getQuote() != null && apiCrypto.getQuote().containsKey("USD")) {
            CoinMarketCapResponse.Quote usdQuote = apiCrypto.getQuote().get("USD");
            crypto.setCurrentPrice(usdQuote.getPrice());
            crypto.setVolume24h(usdQuote.getVolume24h());
            crypto.setPercentChange1h(usdQuote.getPercentChange1h());
            crypto.setPercentChange24h(usdQuote.getPercentChange24h());
            crypto.setPercentChange7d(usdQuote.getPercentChange7d());
            crypto.setMarketCap(usdQuote.getMarketCap());
        }

        // Set metadata
        crypto.setCreatedAt(LocalDateTime.now());
        crypto.setUpdatedAt(LocalDateTime.now());

        return crypto;
    }

    /**
     * GET MOCK CRYPTO DATA - FALLBACK DATA 📝
     * 
     * This provides fallback data when:
     * - API is not configured
     * - API is down
     * - Database is not available
     * 
     * This ensures our application always has something to show users!
     */
    private List<CryptoCurrency> getMockCryptoData() {
        List<CryptoCurrency> mockData = new ArrayList<>();

        // Create mock data for popular cryptocurrencies
        mockData.add(createMockCrypto("BTC", "Bitcoin", 50000.00, 1));
        mockData.add(createMockCrypto("ETH", "Ethereum", 3000.00, 2));
        mockData.add(createMockCrypto("BNB", "BNB", 400.00, 3));
        mockData.add(createMockCrypto("XRP", "XRP", 0.60, 4));
        mockData.add(createMockCrypto("ADA", "Cardano", 1.20, 5));
        mockData.add(createMockCrypto("DOGE", "Dogecoin", 0.30, 6));
        mockData.add(createMockCrypto("SOL", "Solana", 100.00, 7));
        mockData.add(createMockCrypto("TRX", "TRON", 0.08, 8));
        mockData.add(createMockCrypto("TON", "Toncoin", 5.50, 9));
        mockData.add(createMockCrypto("AVAX", "Avalanche", 25.00, 10));

        return mockData;
    }

    /**
     * CREATE MOCK CRYPTO - HELPER METHOD 🛠️
     */
    private CryptoCurrency createMockCrypto(String symbol, String name, double price, int rank) {
        CryptoCurrency crypto = new CryptoCurrency();
        crypto.setSymbol(symbol);
        crypto.setName(name);
        crypto.setCurrentPrice(price);
        crypto.setCmcRank(rank);
        crypto.setVolume24h(price * 1000000); // Mock volume
        crypto.setMarketCap(price * 21000000); // Mock market cap
        crypto.setPercentChange24h((Math.random() - 0.5) * 10); // Random change
        crypto.setCreatedAt(LocalDateTime.now());
        crypto.setUpdatedAt(LocalDateTime.now());
        return crypto;
    }

    /**
     * GET MOCK PRICE - FALLBACK PRICE LOOKUP 💲
     */
    private double getMockPrice(String symbol) {
        // Simple mock prices for common cryptocurrencies
        Map<String, Double> mockPrices = Map.of(
                "BTC", 50000.00,
                "ETH", 3000.00,
                "BNB", 400.00,
                "XRP", 0.60,
                "ADA", 1.20,
                "DOGE", 0.30,
                "SOL", 100.00,
                "TRX", 0.08,
                "TON", 5.50,
                "AVAX", 25.00);

        return mockPrices.getOrDefault(symbol.toUpperCase(), 0.0);
    }

    /**
     * GET SERVICE CONFIGURATION - DIAGNOSTIC INFORMATION 🔧
     * 
     * This method returns configuration information for debugging and monitoring.
     * It creates an anonymous inner class with all the current configuration
     * values.
     * 
     * Why anonymous inner class?
     * - Bundles related configuration into one object
     * - Makes it easy to serialize to JSON
     * - Provides snapshot of current configuration
     */
    public Object getServiceConfig() {
        return new Object() {
            public final int maxResults = CryptoService.this.maxResults;
            public final long apiTimeout = CryptoService.this.apiTimeout;
            public final String defaultCurrency = CryptoService.this.defaultCurrency;
            public final boolean cacheEnabled = CryptoService.this.cacheEnabled;
            public final long syncInterval = CryptoService.this.syncInterval;
            public final boolean apiKeyConfigured = coinMarketCapApiService.isApiKeyConfigured();
            public final boolean databaseAvailable = cryptoCurrencyRepository != null;
            public final long totalCryptosInDatabase = cryptoCurrencyRepository != null ? safeGetDatabaseCount() : 0;
        };
    }

    /**
     * SAFE GET DATABASE COUNT - ERROR-SAFE OPERATION 🛡️
     */
    private long safeGetDatabaseCount() {
        try {
            return cryptoCurrencyRepository.count();
        } catch (Exception e) {
            System.err.println("❌ Error getting database count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * GET CRYPTO ANALYTICS - COMPLEX BUSINESS LOGIC 📊
     * 
     * This method demonstrates complex business logic that:
     * 1. Retrieves data from repository
     * 2. Performs statistical calculations
     * 3. Builds comprehensive analytics response
     * 4. Handles error cases gracefully
     */
    public Map<String, Object> getCryptoAnalytics(String symbol) {
        try {
            System.out.println("📊 Generating analytics for " + symbol);

            // Get historical data for this cryptocurrency
            List<CryptoCurrency> history = cryptoCurrencyRepository.findHistoryBySymbol(symbol.toUpperCase());

            if (history.isEmpty()) {
                System.out.println("❌ No historical data found for " + symbol);
                return null;
            }

            // Get current/latest data
            CryptoCurrency latest = history.get(0);

            // Extract prices for statistical analysis
            List<Double> prices = history.stream()
                    .map(CryptoCurrency::getCurrentPrice)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (prices.isEmpty()) {
                System.out.println("❌ No price data found for " + symbol);
                return null;
            }

            // ==== STATISTICAL CALCULATIONS ====

            // Basic statistics
            double minPrice = prices.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            double maxPrice = prices.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double avgPrice = prices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double priceRange = maxPrice - minPrice;

            // Calculate standard deviation and coefficient of variation
            double variance = prices.stream()
                    .mapToDouble(price -> Math.pow(price - avgPrice, 2))
                    .average()
                    .orElse(0.0);
            double standardDeviation = Math.sqrt(variance);
            double coefficientOfVariation = avgPrice > 0 ? (standardDeviation / avgPrice) * 100 : 0.0;

            // ==== PERFORMANCE METRICS ====

            // Calculate total change since tracking began
            CryptoCurrency oldest = history.get(history.size() - 1);
            double totalChange = latest.getCurrentPrice() - oldest.getCurrentPrice();
            double totalChangePercent = oldest.getCurrentPrice() > 0
                    ? ((latest.getCurrentPrice() - oldest.getCurrentPrice()) / oldest.getCurrentPrice()) * 100
                    : 0.0;

            // Create statistics object
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("min", minPrice);
            statistics.put("max", maxPrice);
            statistics.put("average", avgPrice);
            statistics.put("priceRange", priceRange);
            statistics.put("standardDeviation", standardDeviation);
            statistics.put("coefficientOfVariation", coefficientOfVariation);
            statistics.put("dataPoints", prices.size());

            // Create performance object
            Map<String, Object> performance = new HashMap<>();
            performance.put("totalChange", totalChange);
            performance.put("totalChangePercent", totalChangePercent);
            performance.put("change1h", latest.getPercentChange1h());
            performance.put("change24h", latest.getPercentChange24h());
            performance.put("change7d", latest.getPercentChange7d());
            performance.put("timespan", "From " + oldest.getCreatedAt() + " to " + latest.getCreatedAt());

            // Prepare chart data (limit to reasonable amount for frontend)
            List<Map<String, Object>> chartData = history.stream()
                    .limit(100) // Limit for performance
                    .map(crypto -> {
                        Map<String, Object> point = new HashMap<>();
                        point.put("timestamp", crypto.getCreatedAt().toString());
                        point.put("price", crypto.getCurrentPrice());
                        point.put("volume", crypto.getVolume24h());
                        point.put("marketCap", crypto.getMarketCap());
                        return point;
                    })
                    .collect(Collectors.toList());

            // Prepare table data (more recent entries first)
            List<Map<String, Object>> tableData = history.stream()
                    .limit(50) // Limit for frontend performance
                    .map(crypto -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("id", crypto.getId());
                        row.put("price", crypto.getCurrentPrice());
                        row.put("marketCap", crypto.getMarketCap());
                        row.put("volume24h", crypto.getVolume24h());
                        row.put("percentChange1h", crypto.getPercentChange1h());
                        row.put("percentChange24h", crypto.getPercentChange24h());
                        row.put("percentChange7d", crypto.getPercentChange7d());
                        row.put("timestamp", crypto.getCreatedAt().toString());
                        return row;
                    })
                    .collect(Collectors.toList());

            // Build comprehensive response
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("symbol", latest.getSymbol());
            analytics.put("name", latest.getName());
            analytics.put("currentPrice", latest.getCurrentPrice());
            analytics.put("cmcRank", latest.getCmcRank());
            analytics.put("statistics", statistics);
            analytics.put("performance", performance);
            analytics.put("chartData", chartData);
            analytics.put("tableData", tableData);

            System.out.println("✅ Analytics generated for " + symbol + " with " + history.size() + " data points");
            return analytics;

        } catch (Exception e) {
            System.err.println("❌ Error generating analytics for " + symbol + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * GET HEALTH STATUS - SIMPLE DIAGNOSTIC 🏥
     */
    public String getHealthStatus() {
        boolean apiConfigured = coinMarketCapApiService.isApiKeyConfigured();
        boolean dbAvailable = cryptoCurrencyRepository != null;

        if (apiConfigured && dbAvailable) {
            return "Healthy - API and Database available";
        } else if (apiConfigured) {
            return "Partial - API available, Database unavailable";
        } else if (dbAvailable) {
            return "Partial - Database available, API not configured";
        } else {
            return "Limited - Using mock data only";
        }
    }
}

/**
 * 🎓 KEY SPRING BOOT SERVICE LAYER CONCEPTS DEMONSTRATED:
 * 
 * 1. BUSINESS LOGIC SEPARATION
 * - Service contains all business rules and calculations
 * - Controller just handles HTTP, delegates to Service
 * - Repository just handles data access
 * 
 * 2. DEPENDENCY INJECTION
 * - @Autowired automatically injects dependencies
 * - @Value injects configuration from properties
 * - No manual object creation or configuration management
 * 
 * 3. TRANSACTION MANAGEMENT
 * - @Transactional ensures database consistency
 * - Different propagation levels for different needs
 * - Automatic rollback on exceptions
 * 
 * 4. SCHEDULED TASKS
 * - @Scheduled enables background automation
 * - Configurable intervals from properties
 * - Non-blocking background execution
 * 
 * 5. ERROR HANDLING & RESILIENCE
 * - Multiple fallback strategies (database → API → mock)
 * - Graceful degradation when components fail
 * - Individual transaction isolation for bulk operations
 * 
 * 6. CONFIGURATION EXTERNALIZATION
 * - @Value allows external configuration
 * - Same code works in different environments
 * - No hardcoded values
 * 
 * 7. REACTIVE PROGRAMMING
 * - Uses Mono for non-blocking API calls
 * - Better resource utilization
 * - Improved scalability
 * 
 * This is the Spring Boot way: Write business logic, let the framework handle
 * the infrastructure! 🚀
 */