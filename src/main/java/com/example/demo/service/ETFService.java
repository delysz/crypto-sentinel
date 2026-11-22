package com.example.demo.service;

import com.example.demo.model.AssetType;
import com.example.demo.model.CryptoAsset;
import com.example.demo.repository.CryptoRepository;
import com.example.demo.service.price.FinancePriceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * Periodically ensures that the ETF list defined in {@code sentinel.etf.symbols}
 * is present in the database. If an ETF is missing, it is added with the
 * latest price fetched from Yahoo Finance.
 */
@Component
public class ETFService {
    @Value("${sentinel.etf.symbols:SPY,IVV,VTI,VOO,QQQ}")
    private String etfSymbolsConfig;
    @Value("${sentinel.stock.symbols:IBM,AAPL,MSFT}")
    private String stockSymbolsConfig;

    private final CryptoRepository cryptoRepository;
    private final FinancePriceProvider financeProvider;

    public ETFService(CryptoRepository cryptoRepository, FinancePriceProvider financeProvider) {
        this.cryptoRepository = cryptoRepository;
        this.financeProvider = financeProvider;
    }

    @PostConstruct
    public void init() {
        loadEtfList();
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledLoad() {
        loadEtfList();
    }

    private void loadEtfList() {
        List<String> etfSymbols = Arrays.stream(etfSymbolsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .toList();
        for (String symbol : etfSymbols) {
            cryptoRepository.findBySymbol(symbol).orElseGet(() -> {
                CryptoAsset asset = new CryptoAsset();
                asset.setSymbol(symbol);
                asset.setName(symbol); // Simplified name
                asset.setType(AssetType.ETF);
                Double price = financeProvider.getPrice(symbol);
                asset.setPrice(price);
                asset.setLastPrice(price);
                asset.setLastUpdated(java.time.LocalDateTime.now());
                return cryptoRepository.save(asset);
        }
        // Process stocks
        List<String> stockSymbols = Arrays.stream(stockSymbolsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .toList();
        for (String symbol : stockSymbols) {
            cryptoRepository.findBySymbol(symbol).orElseGet(() -> {
                CryptoAsset asset = new CryptoAsset();
                asset.setSymbol(symbol);
                asset.setName(symbol);
                asset.setType(AssetType.STOCK);
                Double price = financeProvider.getPrice(symbol);
                asset.setPrice(price);
                asset.setLastPrice(price);
                asset.setLastUpdated(java.time.LocalDateTime.now());
                return cryptoRepository.save(asset);
            });
        }
    }
}
