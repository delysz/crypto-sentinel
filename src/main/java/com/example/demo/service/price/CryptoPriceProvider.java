package com.example.demo.service.price;

import com.example.demo.service.CoinGeckoService;
import org.springframework.stereotype.Component;

@Component
public class CryptoPriceProvider implements PriceProvider {
    private final CoinGeckoService coinGeckoService;

    public CryptoPriceProvider(CoinGeckoService coinGeckoService) {
        this.coinGeckoService = coinGeckoService;
    }

    @Override
    public Double getPrice(String symbol) {
        try {
            var data = coinGeckoService.getMultiplePrices(symbol.toLowerCase());
            return data != null && data.containsKey(symbol.toLowerCase()) ? data.get(symbol.toLowerCase()).getPrice() : null;
        } catch (Exception e) {
            System.err.println("❌ CoinGeckoProvider error for " + symbol + ": " + e.getMessage());
            return null;
        }
    }
}
