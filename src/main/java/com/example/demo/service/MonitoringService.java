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
import org.springframework.web.reactive.function.client.WebClient;

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
    // Map of providers per asset type
    @Autowired private java.util.Map<com.example.demo.model.AssetType, com.example.demo.service.price.PriceProvider> providers;

    @Scheduled(fixedRate = 300000) // 5 minutos
    public void checkAllCryptos() {
        List<CryptoAsset> assetsInDb = repository.findAll();
        if (assetsInDb.isEmpty()) return;

        for (CryptoAsset asset : assetsInDb) {
            String symbol = asset.getSymbol().toLowerCase();
            com.example.demo.service.price.PriceProvider provider = providers.get(asset.getType());
            if (provider == null) {
                System.out.println("⚠️ No hay proveedor configurado para tipo: " + asset.getType());
                continue;
            }
            try {
                Double precioActual = provider.getPrice(symbol);
                if (precioActual == null) {
                    System.out.println("⚠️ Precio no disponible para " + symbol + " (tipo: " + asset.getType() + ")");
                    continue;
                }
                updateAssetPrice(asset, precioActual);
            } catch (Exception e) {
                System.err.println("❌ Error al obtener precio para " + symbol + ": " + e.getMessage());
            }
        }
    }

    // The following methods were part of the legacy implementation and are no longer used.
    // They have been removed to keep the codebase clean.

    private void updateAssetPrice(CryptoAsset asset, Double precioActual) {
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
                System.out.println("🔎 Variación de " + asset.getName() + ": " + String.format("%.2f%%", variacion * 100));
                if (variacion <= alertThreshold) {
                    String msg = "🚨 Alerta en " + asset.getName() + " ha caído un " + String.format("%.2f", Math.abs(variacion * 100)) + "%";
                    System.out.println("📩 Enviando mensaje: " + msg);
                    telegramService.sendMessage(msg);

                AlertLog log = new AlertLog();
                log.setCryptoName(asset.getName());
                log.setPriceAtAlert(precioActual);
                log.setDropPercentage(variacion * 100);
                log.setTimestamp(LocalDateTime.now());
                alertLogRepository.save(log);
            }
        }
    }
}