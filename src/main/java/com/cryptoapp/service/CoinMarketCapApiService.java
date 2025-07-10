package com.cryptoapp.service;

import com.cryptoapp.model.CoinMarketCapResponse;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import jakarta.annotation.PostConstruct;

/**
 * Service for making API calls to CoinMarketCap
 * 
 * Uses Spring WebFlux WebClient for non-blocking HTTP requests
 * Loads API key from environment variables using dotenv
 */
@Service
public class CoinMarketCapApiService {

  private final WebClient webClient;
  private String apiKey;

  @Value("${coinmarketcap.api.base-url:https://pro-api.coinmarketcap.com/v1}")
  private String baseUrl;

  @Value("${coinmarketcap.api.timeout:5000}")
  private int timeoutMs;

  public CoinMarketCapApiService() {
    this.webClient = WebClient.builder()
        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB buffer
        .build();
  }

  /**
   * Initialize the API key from environment variables after bean construction
   * Using @PostConstruct to load from .env file
   */
  @PostConstruct
  public void init() {
    try {
      // Load .env file if it exists
      Dotenv dotenv = Dotenv.configure()
          .ignoreIfMissing() // Don't fail if .env doesn't exist
          .load();

      // Try to get API key from .env first, then fall back to system environment
      this.apiKey = dotenv.get("COINMARKET_API_KEY");
      if (this.apiKey == null) {
        this.apiKey = System.getenv("COINMARKET_API_KEY");
      }

      if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
        System.err.println("⚠️  WARNING: COINMARKET_API_KEY not found in .env file or environment variables!");
        System.err.println("   Add your API key to .env file: COINMARKET_API_KEY=your_api_key_here");
      } else {
        System.out.println("✅ CoinMarketCap API key loaded successfully");
      }
    } catch (Exception e) {
      System.err.println("❌ Error loading environment variables: " + e.getMessage());
    }
  }

  /**
   * Get latest cryptocurrency listings (top cryptocurrencies by market cap)
   * 
   * @param limit   Number of results to return (1-5000, default 100)
   * @param convert Currency to convert prices to (default USD)
   * @return Mono<CoinMarketCapResponse> for reactive processing
   */
  public Mono<CoinMarketCapResponse> getLatestListings(Integer limit, String convert) {
    if (apiKey == null || apiKey.trim().isEmpty()) {
      return Mono.error(new RuntimeException("CoinMarketCap API key not configured"));
    }

    return webClient
        .get()
        .uri(uriBuilder -> uriBuilder
            .scheme("https")
            .host("pro-api.coinmarketcap.com")
            .path("/v1/cryptocurrency/listings/latest")
            .queryParam("limit", limit != null ? limit : 10)
            .queryParam("convert", convert != null ? convert : "USD")
            .build())
        .header("X-CMC_PRO_API_KEY", apiKey)
        .header("Accept", "application/json")
        .retrieve()
        .bodyToMono(CoinMarketCapResponse.class)
        .doOnNext(
            response -> System.out.println("✅ Received " + response.getData().size() + " cryptocurrencies from API"))
        .doOnError(error -> {
          if (error instanceof WebClientResponseException) {
            WebClientResponseException webError = (WebClientResponseException) error;
            System.err.println(
                "❌ CoinMarketCap API Error: " + webError.getStatusCode() + " - " + webError.getResponseBodyAsString());
          } else {
            System.err.println("❌ Error calling CoinMarketCap API: " + error.getMessage());
          }
        });
  }

  /**
   * Get latest quotes for specific cryptocurrencies by symbol
   * 
   * @param symbols Comma-separated list of cryptocurrency symbols (e.g.,
   *                "BTC,ETH,ADA")
   * @param convert Currency to convert prices to (default USD)
   * @return Mono<CoinMarketCapResponse> for reactive processing
   */
  public Mono<CoinMarketCapResponse> getLatestQuotes(String symbols, String convert) {
    if (apiKey == null || apiKey.trim().isEmpty()) {
      return Mono.error(new RuntimeException("CoinMarketCap API key not configured"));
    }

    return webClient
        .get()
        .uri(uriBuilder -> uriBuilder
            .scheme("https")
            .host("pro-api.coinmarketcap.com")
            .path("/v1/cryptocurrency/quotes/latest")
            .queryParam("symbol", symbols)
            .queryParam("convert", convert != null ? convert : "USD")
            .build())
        .header("X-CMC_PRO_API_KEY", apiKey)
        .header("Accept", "application/json")
        .retrieve()
        .bodyToMono(CoinMarketCapResponse.class)
        .doOnNext(response -> System.out.println("✅ Received quotes for: " + symbols))
        .doOnError(error -> {
          if (error instanceof WebClientResponseException) {
            WebClientResponseException webError = (WebClientResponseException) error;
            System.err.println(
                "❌ CoinMarketCap API Error: " + webError.getStatusCode() + " - " + webError.getResponseBodyAsString());
          } else {
            System.err.println("❌ Error calling CoinMarketCap API: " + error.getMessage());
          }
        });
  }

  /**
   * Check if API key is configured
   */
  public boolean isApiKeyConfigured() {
    return apiKey != null && !apiKey.trim().isEmpty();
  }
}