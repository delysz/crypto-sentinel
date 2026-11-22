package com.example.demo.config;

import com.example.demo.model.AssetType;
import com.example.demo.service.price.FinancePriceProvider;
import com.example.demo.service.price.PriceProvider;
import com.example.demo.service.price.CryptoPriceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PriceProviderConfig {

    @Bean
    public Map<AssetType, PriceProvider> priceProviderMap(CryptoPriceProvider cryptoProvider, FinancePriceProvider financeProvider) {
        Map<AssetType, PriceProvider> map = new HashMap<>();
        map.put(AssetType.CRYPTO, cryptoProvider);
        map.put(AssetType.ETF, financeProvider);
        map.put(AssetType.STOCK, financeProvider);
        return map;
    }
}
