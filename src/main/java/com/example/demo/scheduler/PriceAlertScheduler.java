package com.example.demo.scheduler;

import com.example.demo.model.PriceAlert;
import com.example.demo.service.PriceAlertService;
import com.example.demo.repository.CryptoRepository;
import com.example.demo.service.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PriceAlertScheduler {
    
    @Autowired
    private PriceAlertService priceAlertService;
    
    @Autowired
    private CryptoRepository cryptoRepository;
    
    @Autowired
    private TelegramService telegramService;
    
    /**
     * Checks for triggered price alerts every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void checkPriceAlerts() {
        List<PriceAlert> activeAlerts = priceAlertService.getActiveAlerts();
        
        for (PriceAlert alert : activeAlerts) {
            try {
                // Último precio conocido (MonitoringService lo actualiza cada 5 min)
                Double currentPrice = cryptoRepository.findBySymbol(alert.getAssetSymbol())
                        .or(() -> cryptoRepository.findBySymbol(alert.getAssetSymbol().toUpperCase()))
                        .or(() -> cryptoRepository.findBySymbol(alert.getAssetSymbol().toLowerCase()))
                        .map(asset -> asset.getPrice())
                        .orElse(null);
                
                if (currentPrice != null) {
                    // Check if alert condition is met
                    boolean shouldTrigger = false;
                    
                    if ("above".equals(alert.getAlertType()) && currentPrice >= alert.getTargetPrice()) {
                        shouldTrigger = true;
                    } else if ("below".equals(alert.getAlertType()) && currentPrice <= alert.getTargetPrice()) {
                        shouldTrigger = true;
                    }
                    
                    if (shouldTrigger) {
                        String msg = "🎯 Alerta de precio: " + alert.getAssetName() +
                                " está en $" + currentPrice +
                                " (objetivo " + alert.getAlertType() + " $" + alert.getTargetPrice() + ")";
                        System.out.println(msg);
                        telegramService.sendMessage(msg);
                        
                        // Desactivar la alerta tras dispararse
                        priceAlertService.markAsTriggered(alert.getId());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error checking alert for " + alert.getAssetName() + ": " + e.getMessage());
            }
        }
    }
}