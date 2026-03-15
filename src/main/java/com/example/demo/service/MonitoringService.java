package com.example.demo.service;

import com.example.demo.dto.PriceResponse;
import com.example.demo.model.AlertLog;
import com.example.demo.model.CryptoAsset;
import com.example.demo.repository.AlertLogRepository;
import com.example.demo.repository.CryptoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MonitoringService {

    @Value("${sentinel.alert.threshold}")
    private Double alertThreshold;

    @Autowired private CoinGeckoService cryptoService;
    @Autowired private CryptoRepository repository;
    @Autowired private TelegramService telegramService;
    @Autowired private AlertLogRepository alertLogRepository;

    @Scheduled(fixedRate = 300000) // 5 minutos
    public void checkAllCryptos() {
        List<CryptoAsset> assetsInDb = repository.findAll();
        if (assetsInDb.isEmpty()) return;

        String idsToQuery = assetsInDb.stream()
                .map(asset -> asset.getSymbol().toLowerCase().trim())
                .collect(Collectors.joining(","));

        System.out.println("🔍 Consultando a CoinGecko: " + idsToQuery);

        try {
            Map<String, PriceResponse> prices = cryptoService.getMultiplePrices(idsToQuery);
            System.out.println("📦 Respuesta de la API: " + prices);

            if (prices != null && !prices.isEmpty()) {
                assetsInDb.forEach(asset -> {
                    PriceResponse data = prices.get(asset.getSymbol().toLowerCase());

                    if (data != null && data.getPrice() != null) {
                        Double precioActual = data.getPrice();
                        Double precioAnterior = asset.getPrice();

                        asset.setLastPrice(precioAnterior);
                        asset.setPrice(precioActual);
                        asset.setLastUpdated(LocalDateTime.now());

                        asset.getPriceHistory().add(precioActual);
                        if (asset.getPriceHistory().size() > 15) {
                            asset.getPriceHistory().remove(0);
                        }

                        repository.save(asset);
                        System.out.println("✅ Actualizado " + asset.getName() + " a $" + precioActual);

                        if (precioAnterior != null && precioAnterior > 0) {
                            double variacion = (precioActual - precioAnterior) / precioAnterior;
                            if (variacion <= alertThreshold) {
                                telegramService.sendMessage("🚨 Alerta en " + asset.getName());

                                AlertLog log = new AlertLog();
                                log.setCryptoName(asset.getName());
                                log.setPriceAtAlert(precioActual);
                                log.setDropPercentage(variacion * 100);
                                log.setTimestamp(LocalDateTime.now());
                                alertLogRepository.save(log);
                            }
                        }
                    } else {
                        System.out.println("⚠️ No se encontraron datos para: " + asset.getSymbol());
                    }
                });
            }
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                System.out.println("⏳ CoinGecko 429 — esperando al siguiente ciclo (5 min)");
            } else {
                System.out.println("❌ Error al consultar CoinGecko: " + e.getMessage());
            }
        }
    }
}