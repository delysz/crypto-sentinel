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
                processAndSavePrices(assetsInDb, prices);
            }
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                System.out.println("⏳ CoinGecko 429 detectado. ¡Activando plan de rescate con Binance API! 🛟");
                fallbackToBinance(assetsInDb);
            } else {
                System.out.println("❌ Error al consultar CoinGecko: " + e.getMessage());
            }
        }
    }

    private void processAndSavePrices(List<CryptoAsset> assetsInDb, Map<String, PriceResponse> prices) {
        assetsInDb.forEach(asset -> {
            PriceResponse data = prices.get(asset.getSymbol().toLowerCase());
            if (data != null && data.getPrice() != null) {
                updateAssetPrice(asset, data.getPrice());
            } else {
                System.out.println("⚠️ No se encontraron datos en CoinGecko para: " + asset.getSymbol());
            }
        });
    }

    // 🛟 PLAN DE RESCATE: Consulta uno a uno a Binance (Inmune a bloqueos por IP de Render)
    private void fallbackToBinance(List<CryptoAsset> assetsInDb) {
        WebClient binanceClient = WebClient.builder().baseUrl("https://api.binance.com/api/v3").build();

        assetsInDb.forEach(asset -> {
            try {
                // Traducimos el símbolo a formato Binance estándar (ej: link -> LINKUSDT, bitcoin -> BTCUSDT)
                String symbol = asset.getSymbol().equalsIgnoreCase("bitcoin") ? "BTC" :
                        asset.getSymbol().equalsIgnoreCase("ethereum") ? "ETH" :
                                asset.getSymbol().toUpperCase();

                String binancePair = symbol + "USDT";

                Map<String, String> response = binanceClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/ticker/price").queryParam("symbol", binancePair).build())
                        .retrieve()
                        .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, String>>() {})
                        .block();

                if (response != null && response.containsKey("price")) {
                    Double precioActual = Double.parseDouble(response.get("price"));
                    updateAssetPrice(asset, precioActual);
                    System.out.println("🎰 Binance rescató con éxito: " + binancePair + " = $" + precioActual);
                }
            } catch (Exception e) {
                System.out.println("❌ Tampoco se pudo obtener de Binance el activo: " + asset.getSymbol());
            }
        });
    }

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
            if (variacion <= alertThreshold) {
                telegramService.sendMessage("🚨 Alerta en " + asset.getName() + " ha caído un " + String.format("%.2f", Math.abs(variacion * 100)) + "%");

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