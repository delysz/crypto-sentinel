package com.example.demo.service.price;

public interface PriceProvider {
    /**
     * Retrieve the current market price for the given symbol.
     * @param symbol the asset symbol (e.g., BTC, AAPL, SPY)
     * @return current price in USD or {@code null} if unavailable
     */
    Double getPrice(String symbol);
}
