// Crypto Backend Frontend JavaScript
// Handles API calls to Spring Boot backend and UI updates

const API_BASE = window.location.origin + '/api/crypto';

// State management
let cryptoData = [];
let previousPrices = {}; // Track previous prices for change indicators
let isLoading = false;

// DOM elements
const elements = {
  loading: document.getElementById('loading'),
  errorMessage: document.getElementById('errorMessage'),
  errorText: document.getElementById('errorText'),
  cryptoGrid: document.getElementById('cryptoGrid'),
  searchInput: document.getElementById('searchInput'),
  portfolioInput: document.getElementById('portfolioInput'),
  portfolioResult: document.getElementById('portfolioResult'),
  portfolioValue: document.getElementById('portfolioValue'),
  portfolioBreakdown: document.getElementById('portfolioBreakdown'),
  apiStatusText: document.getElementById('api-status-text'),
  dbStatusText: document.getElementById('db-status-text')
};

// Initialize the application
document.addEventListener('DOMContentLoaded', function () {
  console.log('🚀 Crypto Frontend initialized');
  checkHealth();
  loadPopularCryptos();
});

// API Helper Functions
async function apiCall(endpoint, options = {}) {
  try {
    showLoading(true);
    hideError();

    // Add cache-busting parameter to ensure fresh data
    const separator = endpoint.includes('?') ? '&' : '?';
    const cacheBuster = `${separator}t=${Date.now()}`;
    const urlWithCacheBuster = `${API_BASE}${endpoint}${cacheBuster}`;

    console.log('🔗 API Call:', urlWithCacheBuster);

    const response = await fetch(urlWithCacheBuster, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        'Cache-Control': 'no-cache',
        'Pragma': 'no-cache',
        ...options.headers
      }
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error(`API call failed for ${endpoint}:`, error);
    showError(`Failed to ${endpoint}: ${error.message}`);
    throw error;
  } finally {
    showLoading(false);
  }
}

// UI Helper Functions
function showLoading(show = true) {
  // Loading popup disabled - no visual loading indicator
  isLoading = show;
}

function showError(message) {
  elements.errorText.textContent = message;
  elements.errorMessage.style.display = 'flex';
  setTimeout(() => {
    elements.errorMessage.style.display = 'none';
  }, 5000);
}

function hideError() {
  elements.errorMessage.style.display = 'none';
}

function formatPrice(price) {
  if (price === null || price === undefined) return '$0.00';
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 8
  }).format(price);
}

function formatPercent(percent) {
  if (percent === null || percent === undefined) return '0.00%';
  const formatted = Math.abs(percent).toFixed(2);
  const sign = percent >= 0 ? '+' : '-';
  return `${sign}${formatted}%`;
}

function formatMarketCap(marketCap) {
  if (!marketCap) return 'N/A';
  if (marketCap >= 1e12) return `$${(marketCap / 1e12).toFixed(2)}T`;
  if (marketCap >= 1e9) return `$${(marketCap / 1e9).toFixed(2)}B`;
  if (marketCap >= 1e6) return `$${(marketCap / 1e6).toFixed(2)}M`;
  return formatPrice(marketCap);
}

// Main API Functions
async function checkHealth() {
  try {
    console.log('🏥 Checking health status...');
    const health = await apiCall('/health');

    // Handle JSON response from updated backend
    if (health && typeof health === 'object') {
      // Update database status
      if (health.database) {
        const dbCount = health.database.cryptocurrencies || 0;
        const dbAvailable = health.database.available;

        if (dbAvailable) {
          elements.dbStatusText.textContent = `✅ ${dbCount} cryptos stored`;
          elements.dbStatusText.parentElement.style.color = '#43e97b';
        } else {
          elements.dbStatusText.textContent = '❌ Not connected';
          elements.dbStatusText.parentElement.style.color = '#f5576c';
        }
      }

      // Update API status
      if (health.api) {
        const apiConfigured = health.api.configured;

        if (apiConfigured) {
          elements.apiStatusText.textContent = '✅ Connected';
          elements.apiStatusText.parentElement.style.color = '#43e97b';
        } else {
          elements.apiStatusText.textContent = '⚠️ Not configured';
          elements.apiStatusText.parentElement.style.color = '#f5576c';
        }
      }

      // Health check successful - update status silently without popup
      console.log('✅ Health check successful! Backend is healthy and ready.');

    } else {
      // Fallback for old text-based response (shouldn't happen now)
      console.warn('Unexpected health response format:', health);
      elements.apiStatusText.textContent = 'Unknown format';
      elements.dbStatusText.textContent = 'Unknown format';
    }

    console.log('✅ Health check successful');
  } catch (error) {
    console.error('❌ Health check failed:', error);
    elements.apiStatusText.textContent = '❌ Error';
    elements.dbStatusText.textContent = '❌ Error';
    elements.apiStatusText.parentElement.style.color = '#f5576c';
    elements.dbStatusText.parentElement.style.color = '#f5576c';

    showError('❌ Health check failed: ' + error.message);
  }
}

async function loadPopularCryptos() {
  try {
    console.log('📊 Loading popular cryptocurrencies...');
    console.log('🔗 Making API call to:', `${API_BASE}/popular`);

    cryptoData = await apiCall('/popular');

    // Debug: Log ALL price data to verify freshness and detect changes
    if (cryptoData && cryptoData.length > 0) {
      console.log('📊 COMPLETE crypto data received:');
      cryptoData.forEach(crypto => {
        console.log(`  📈 ${crypto.symbol}: $${crypto.currentPrice} (ID: ${crypto.id}, created: ${crypto.createdAt}, updated: ${crypto.lastUpdated || 'unknown'})`);
      });

      // Also log the JSON structure to see what fields are available
      console.log('📋 First crypto object structure:', Object.keys(cryptoData[0]));
    } else {
      console.log('❌ No crypto data received from backend!');
    }

    displayCryptos(cryptoData);
    console.log(`✅ Loaded ${cryptoData.length} cryptocurrencies`);
  } catch (error) {
    console.error('❌ Failed to load popular cryptos:', error);
  }
}

async function syncWithAPI() {
  try {
    console.log('🔄 Syncing with CoinMarketCap API...');

    // Use GET request since we added both GET and POST endpoints
    const response = await apiCall('/sync');

    // Reload data after sync
    await loadPopularCryptos();
    await checkHealth();

    // Show success message
    showError('✅ Successfully synced with CoinMarketCap API! Data updated.');
    elements.errorMessage.style.background = 'rgba(67, 233, 123, 0.1)';
    elements.errorMessage.style.borderColor = 'rgba(67, 233, 123, 0.3)';
    elements.errorMessage.style.color = '#43e97b';

    console.log('✅ Sync completed successfully');
  } catch (error) {
    console.error('❌ Sync failed:', error);
    showError('❌ Sync failed: ' + error.message);
  }
}

async function searchCryptos(event) {
  // If Enter key was pressed or called directly
  if (event && event.type === 'keyup' && event.key !== 'Enter') {
    return;
  }

  const searchTerm = elements.searchInput.value.trim();

  try {
    console.log(`🔍 Searching for: "${searchTerm}"`);

    let results;
    if (searchTerm === '') {
      // If search is empty, show popular cryptos
      results = await apiCall('/popular');
    } else {
      // Search with query parameter
      results = await apiCall(`/search?q=${encodeURIComponent(searchTerm)}`);
    }

    displayCryptos(results);
    console.log(`✅ Found ${results.length} cryptocurrencies`);
  } catch (error) {
    console.error('❌ Search failed:', error);
  }
}

function displayCryptos(cryptos) {
  if (!cryptos || cryptos.length === 0) {
    elements.cryptoGrid.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 40px;">
                <i class="fas fa-search" style="font-size: 3rem; opacity: 0.3; margin-bottom: 20px;"></i>
                <h3>No cryptocurrencies found</h3>
                <p style="opacity: 0.7;">Try a different search term or sync with the API</p>
            </div>
        `;
    return;
  }

  console.log('🔄 Processing', cryptos.length, 'cryptos for display');
  console.log('📝 Current previousPrices state:', previousPrices);

  elements.cryptoGrid.innerHTML = cryptos.map(crypto => {
    const currentPrice = crypto.currentPrice || 0;
    const previousPrice = previousPrices[crypto.symbol];

    // Calculate price change - only if we have a previous price
    let priceChange = 0;
    let isFirstLoad = previousPrice === undefined;

    if (!isFirstLoad) {
      priceChange = currentPrice - previousPrice;
    }

    // Enhanced debug logging
    console.log(`💰 ${crypto.symbol}:`);
    console.log(`  📊 Current: $${currentPrice}`);
    console.log(`  📋 Previous: ${previousPrice !== undefined ? '$' + previousPrice : 'none'}`);
    console.log(`  📈 Change: $${priceChange.toFixed(6)}`);
    console.log(`  🔍 AbsChange: $${Math.abs(priceChange).toFixed(6)}`);
    console.log(`  🎯 FirstLoad: ${isFirstLoad}`);
    console.log(`  🚨 Threshold Check: ${Math.abs(priceChange)} > 0.01 = ${Math.abs(priceChange) > 0.01}`);

    // Determine price change indicator - LOWER threshold for testing
    let priceChangeClass = '';
    let priceChangeIcon = '';

    if (isFirstLoad) {
      // First load - no change indicator, but add a subtle pulse to show it's live data
      priceChangeClass = 'price-first-load';
      priceChangeIcon = '💫';
      console.log(`  ✨ ${crypto.symbol}: FIRST LOAD`);
    } else if (priceChange > 0.001) { // Much lower threshold for testing - $0.001
      priceChangeClass = 'price-up';
      priceChangeIcon = '📈';
      console.log(`  🟢 ${crypto.symbol} UP: +$${priceChange.toFixed(6)}`);
    } else if (priceChange < -0.001) { // Much lower threshold for testing - $0.001
      priceChangeClass = 'price-down';
      priceChangeIcon = '📉';
      console.log(`  🔴 ${crypto.symbol} DOWN: -$${Math.abs(priceChange).toFixed(6)}`);
    } else {
      priceChangeClass = 'price-stable';
      priceChangeIcon = '➡️';
      console.log(`  ⚪ ${crypto.symbol} STABLE: Change too small or zero`);
    }

    // Update previous price for next comparison
    previousPrices[crypto.symbol] = currentPrice;
    console.log(`  🔄 Updated previousPrices[${crypto.symbol}] = $${currentPrice}`);

    return `
        <div class="crypto-card ${priceChangeClass}" data-symbol="${crypto.symbol}" onclick="openAnalytics('${crypto.symbol}')">
            ${crypto.cmcRank ? `<div class="crypto-rank">#${crypto.cmcRank}</div>` : ''}
            
            <div class="crypto-header">
                <div>
                    <div class="crypto-name">${crypto.name || 'Unknown'}</div>
                    <div class="crypto-symbol">${crypto.symbol || 'N/A'}</div>
                </div>
                <div class="price-indicator">${priceChangeIcon}</div>
            </div>
            
            <div class="crypto-price ${priceChangeClass}">
                ${formatPrice(currentPrice)}
                ${priceChange !== 0 ? `<span class="price-change">${priceChange > 0 ? '+' : ''}${formatPrice(Math.abs(priceChange))}</span>` : ''}
            </div>
            
            <div class="crypto-stats">
                <div class="stat-item">
                    <div class="stat-label">Market Cap</div>
                    <div class="stat-value">${formatMarketCap(crypto.marketCap)}</div>
                </div>
                
                <div class="stat-item">
                    <div class="stat-label">24h Volume</div>
                    <div class="stat-value">${formatMarketCap(crypto.volume24h)}</div>
                </div>
                
                <div class="stat-item">
                    <div class="stat-label">1h Change</div>
                    <div class="stat-value ${crypto.percentChange1h >= 0 ? 'positive' : 'negative'}">
                        ${formatPercent(crypto.percentChange1h)}
                    </div>
                </div>
                
                <div class="stat-item">
                    <div class="stat-label">24h Change</div>
                    <div class="stat-value ${crypto.percentChange24h >= 0 ? 'positive' : 'negative'}">
                        ${formatPercent(crypto.percentChange24h)}
                    </div>
                </div>
            </div>
            <div class="analytics-hint">Click to view analytics 📊</div>
        </div>
    `;
  }).join('');

  // Remove existing pulse classes from all cards (from previous updates)
  document.querySelectorAll('.crypto-card').forEach(card => {
    card.classList.remove('price-pulse', 'first-load-pulse');
  });

  // Add persistent pulse animation based on price change state
  setTimeout(() => {
    // Debug: Check what cards have what classes
    document.querySelectorAll('.crypto-card').forEach(card => {
      console.log(`🔍 Card ${card.dataset.symbol}: classes = [${card.className}]`);
    });

    // Glow for price changes (up/down)
    const upDownCards = document.querySelectorAll('.crypto-card.price-up, .crypto-card.price-down');
    console.log(`🎯 Found ${upDownCards.length} cards with price-up/price-down classes`);

    upDownCards.forEach(card => {
      card.classList.add('price-pulse');
      console.log('✨ Added glow to', card.dataset.symbol, '- Final classes:', card.className);
    });

    // Subtle pulse for first load data
    const firstLoadCards = document.querySelectorAll('.crypto-card.price-first-load');
    console.log(`💫 Found ${firstLoadCards.length} cards with price-first-load class`);

    firstLoadCards.forEach(card => {
      card.classList.add('first-load-pulse');
      console.log('💫 Added first-load pulse to', card.dataset.symbol, '- Final classes:', card.className);
    });
  }, 100);
}

async function calculatePortfolio(event) {
  // If Enter key was pressed
  if (event && event.type === 'keyup' && event.key !== 'Enter') {
    return;
  }

  const input = elements.portfolioInput.value.trim();
  if (!input) {
    elements.portfolioResult.style.display = 'none';
    return;
  }

  try {
    console.log(`💰 Calculating portfolio for: ${input}`);

    // Parse symbols from input
    const symbols = input.split(',').map(s => s.trim().toUpperCase()).filter(s => s);

    if (symbols.length === 0) {
      elements.portfolioResult.style.display = 'none';
      return;
    }

    // Get individual prices for breakdown
    const breakdown = [];
    let totalValue = 0;

    for (const symbol of symbols) {
      try {
        const priceData = await apiCall(`/price/${symbol}`);
        const price = priceData.price || 0;
        breakdown.push({ symbol, price });
        totalValue += price;
      } catch (error) {
        console.warn(`Could not fetch price for ${symbol}`);
        breakdown.push({ symbol, price: 0 });
      }
    }

    // Display results
    elements.portfolioValue.textContent = formatPrice(totalValue);
    elements.portfolioBreakdown.innerHTML = breakdown.map(item => `
            <div class="portfolio-item">
                <span><strong>${item.symbol}</strong></span>
                <span>${formatPrice(item.price)}</span>
            </div>
        `).join('');

    elements.portfolioResult.style.display = 'block';

    console.log(`✅ Portfolio calculated: ${formatPrice(totalValue)}`);
  } catch (error) {
    console.error('❌ Portfolio calculation failed:', error);
    showError('Failed to calculate portfolio value');
  }
}

// Utility Functions
function refreshData() {
  checkHealth();
  loadPopularCryptos();
}

// Auto-refresh every 45 seconds to match backend sync interval
setInterval(refreshData, 45 * 1000);

// Analytics function
function openAnalytics(symbol) {
  console.log(`📊 Opening analytics for ${symbol}`);
  window.location.href = `analytics.html?symbol=${symbol}`;
}

// Global functions for HTML onclick handlers
window.loadPopularCryptos = loadPopularCryptos;
window.syncWithAPI = syncWithAPI;
window.checkHealth = checkHealth;
window.searchCryptos = searchCryptos;
window.calculatePortfolio = calculatePortfolio;
window.openAnalytics = openAnalytics;

console.log('🚀 Crypto Frontend loaded successfully!'); 