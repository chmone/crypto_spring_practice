package com.cryptoapp.component;

import org.springframework.stereotype.Component;

/**
 * @Component is the GENERIC annotation for Spring-managed beans.
 * 
 * @Component vs other annotations:
 *            - @Service: Business logic layer (semantic meaning)
 *            - @Repository: Data access layer (semantic meaning)
 *            - @Controller/@RestController: Web layer (semantic meaning)
 *            - @Component: Generic component (when others don't fit)
 * 
 *            All of these (@Service, @Repository, @Controller) are actually
 *            specialized versions of @Component!
 */
@Component
public class CryptoValidator {

    /**
     * Utility method to validate crypto symbols
     */
    public boolean isValidCryptoSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }

        // Check if it's a known crypto symbol (simplified validation)
        String[] validSymbols = { "BTC", "ETH", "ADA", "SOL", "DOT", "LINK", "XRP" };

        for (String validSymbol : validSymbols) {
            if (validSymbol.equalsIgnoreCase(symbol.trim())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validate if price is reasonable (not negative, not impossibly high)
     */
    public boolean isValidPrice(double price) {
        return price >= 0 && price <= 1_000_000; // Max $1M per crypto
    }

    /**
     * Clean and normalize crypto symbol
     */
    public String normalizeCryptoSymbol(String symbol) {
        if (symbol == null)
            return "";
        return symbol.trim().toUpperCase();
    }
}