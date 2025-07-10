package com.cryptoapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Objects for CoinMarketCap API responses
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinMarketCapResponse {

  @JsonProperty("data")
  private List<CoinMarketCapCrypto> data;

  @JsonProperty("status")
  private Status status;

  public List<CoinMarketCapCrypto> getData() {
    return data;
  }

  public void setData(List<CoinMarketCapCrypto> data) {
    this.data = data;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CoinMarketCapCrypto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("cmc_rank")
    private Integer cmcRank;

    @JsonProperty("quote")
    private Map<String, Quote> quote;

    // Getters and Setters
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
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

    public Integer getCmcRank() {
      return cmcRank;
    }

    public void setCmcRank(Integer cmcRank) {
      this.cmcRank = cmcRank;
    }

    public Map<String, Quote> getQuote() {
      return quote;
    }

    public void setQuote(Map<String, Quote> quote) {
      this.quote = quote;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Quote {
    @JsonProperty("price")
    private Double price;

    @JsonProperty("market_cap")
    private Double marketCap;

    @JsonProperty("volume_24h")
    private Double volume24h;

    @JsonProperty("percent_change_1h")
    private Double percentChange1h;

    @JsonProperty("percent_change_24h")
    private Double percentChange24h;

    @JsonProperty("percent_change_7d")
    private Double percentChange7d;

    // Getters and Setters
    public Double getPrice() {
      return price;
    }

    public void setPrice(Double price) {
      this.price = price;
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
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Status {
    @JsonProperty("error_code")
    private Integer errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    public Integer getErrorCode() {
      return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
      this.errorCode = errorCode;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }
}