package com.cryptoapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity model for cryptocurrency data with historical tracking
 * Each price update creates a new record for historical analysis
 */
@Entity
@Table(name = "cryptocurrencies")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoCurrency {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "coinmarketcap_id", nullable = true)
  @JsonProperty("coinmarketcap_id")
  private Long coinMarketCapId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String symbol;

  @Column(name = "current_price")
  @JsonProperty("currentPrice")
  private Double currentPrice;

  @Column(name = "market_cap")
  private Double marketCap;

  @Column(name = "volume_24h")
  private Double volume24h;

  @Column(name = "percent_change_1h")
  private Double percentChange1h;

  @Column(name = "percent_change_24h")
  private Double percentChange24h;

  @Column(name = "percent_change_7d")
  private Double percentChange7d;

  @Column(name = "cmc_rank")
  @JsonProperty("cmc_rank")
  private Integer cmcRank;

  @Column(name = "last_updated")
  private LocalDateTime lastUpdated;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Constructors
  public CryptoCurrency() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public CryptoCurrency(String name, String symbol) {
    this();
    this.name = name;
    this.symbol = symbol;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getCoinMarketCapId() {
    return coinMarketCapId;
  }

  public void setCoinMarketCapId(Long coinMarketCapId) {
    this.coinMarketCapId = coinMarketCapId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public Double getCurrentPrice() {
    return currentPrice;
  }

  public void setCurrentPrice(Double currentPrice) {
    this.currentPrice = currentPrice;
  }

  public Double getMarketCap() {
    return marketCap;
  }

  public void setMarketCap(Double marketCap) {
    this.marketCap = marketCap;
  }

  public Double getVolume24h() {
    return volume24h;
  }

  public void setVolume24h(Double volume24h) {
    this.volume24h = volume24h;
  }

  public Double getPercentChange1h() {
    return percentChange1h;
  }

  public void setPercentChange1h(Double percentChange1h) {
    this.percentChange1h = percentChange1h;
  }

  public Double getPercentChange24h() {
    return percentChange24h;
  }

  public void setPercentChange24h(Double percentChange24h) {
    this.percentChange24h = percentChange24h;
  }

  public Double getPercentChange7d() {
    return percentChange7d;
  }

  public void setPercentChange7d(Double percentChange7d) {
    this.percentChange7d = percentChange7d;
  }

  public Integer getCmcRank() {
    return cmcRank;
  }

  public void setCmcRank(Integer cmcRank) {
    this.cmcRank = cmcRank;
  }

  public LocalDateTime getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(LocalDateTime lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  @Override
  public String toString() {
    return String.format("CryptoCurrency{name='%s', symbol='%s', price=%.2f, rank=%d}",
        name, symbol, currentPrice, cmcRank);
  }
}