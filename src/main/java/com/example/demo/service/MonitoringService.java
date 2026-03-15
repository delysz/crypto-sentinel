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

    @Scheduled(fixedRate = 60000)
    public void checkAllCryptos() {
        List<CryptoAsset> assetsInDb = repository.findAll();
        if (assetsInDb.isEmpty()) return;

        // Aseguramos que los IDs vayan en minúsculas a la API
        String idsToQuery = assetsInDb.stream()
                .map(asset -> asset.getSymbol().toLowerCase().trim())
                .collect(Collectors.joining(","));

        System.out.println("🔍 Consultando a CoinGecko: " + idsToQuery);

        Map<String, PriceResponse> prices = cryptoService.getMultiplePrices(idsToQuery);

        // LOG DE DEPURACIÓN: Vamos a ver qué nos ha llegado de verdad
        System.out.println("📦 Respuesta de la API: " + prices);

        if (prices != null && !prices.isEmpty()) {
            assetsInDb.forEach(asset -> {
                // Buscamos en el mapa usando el símbolo en minúsculas
                PriceResponse data = prices.get(asset.getSymbol().toLowerCase());

                if (data != null && data.getPrice() != null) {
                    Double precioActual = data.getPrice();
                    Double precioAnterior = asset.getPrice();

                    asset.setLastPrice(precioAnterior);
                    asset.setPrice(precioActual);
                    asset.setLastUpdated(LocalDateTime.now());

                    asset.getPriceHistory().add(precioActual);
                    // Si la lista tiene más de 15 puntos, borramos el más antiguo
                    if (asset.getPriceHistory().size() > 15) {
                        asset.getPriceHistory().remove(0);
                    }

                    repository.save(asset);
                    System.out.println("✅ Actualizado " + asset.getName() + " a $" + precioActual);

                    // Lógica de alerta
                    if (precioAnterior != null && precioAnterior > 0) {


                        double variacion = (precioActual - precioAnterior) / precioAnterior;
                        if (variacion <= alertThreshold) {
                            // 1. Enviar Telegram (lo que ya haces)
                            telegramService.sendMessage("🚨 Alerta en " + asset.getName());

                            // 2. Guardar en el historial
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
        } else {
            System.out.println("❌ La API devolvió un mapa vacío o nulo. ¿Te han bloqueado por exceso de peticiones (429)?");
        }
    }
}