package com.example.demo.service;

import com.example.demo.model.CryptoAsset;
import com.example.demo.model.PriceAlert;
import com.example.demo.repository.CryptoRepository;
import com.example.demo.repository.PriceAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PriceAlertService {

    @Autowired
    private PriceAlertRepository priceAlertRepository;

    @Autowired
    private CryptoRepository cryptoRepository;

    /**
     * Creates a new price alert after validating and normalizing the input.
     *
     * @throws IllegalArgumentException if the input is invalid
     */
    public PriceAlert createPriceAlert(PriceAlert alert) {
        if (alert == null) {
            throw new IllegalArgumentException("La alerta no puede estar vacía");
        }

        String type = alert.getAlertType() == null ? "" : alert.getAlertType().trim().toLowerCase(Locale.ROOT);
        if (!"above".equals(type) && !"below".equals(type)) {
            throw new IllegalArgumentException("Tipo de alerta inválido: debe ser 'above' o 'below'");
        }

        double target = alert.getTargetPrice();
        if (Double.isNaN(target) || Double.isInfinite(target) || target <= 0) {
            throw new IllegalArgumentException("El precio objetivo debe ser mayor que 0");
        }

        String symbol = alert.getAssetSymbol() == null ? "" : alert.getAssetSymbol().trim();
        if (symbol.isEmpty()) {
            throw new IllegalArgumentException("El símbolo del activo es obligatorio");
        }

        CryptoAsset asset = findAsset(symbol)
                .orElseThrow(() -> new IllegalArgumentException("El activo '" + symbol + "' no existe"));

        alert.setId(null);
        alert.setAlertType(type);
        alert.setAssetSymbol(asset.getSymbol());
        if (alert.getAssetName() == null || alert.getAssetName().isBlank()) {
            alert.setAssetName(asset.getName() != null ? asset.getName() : asset.getSymbol());
        } else {
            alert.setAssetName(alert.getAssetName().trim());
        }
        alert.setActive(true);
        alert.setCreatedDate(LocalDateTime.now());
        alert.setTriggeredDate(null);

        return priceAlertRepository.save(alert);
    }

    private Optional<CryptoAsset> findAsset(String symbol) {
        return cryptoRepository.findBySymbol(symbol)
                .or(() -> cryptoRepository.findBySymbol(symbol.toUpperCase(Locale.ROOT)))
                .or(() -> cryptoRepository.findBySymbol(symbol.toLowerCase(Locale.ROOT)));
    }

    /**
     * Gets all active price alerts
     */
    public List<PriceAlert> getActiveAlerts() {
        return priceAlertRepository.findByActiveTrue();
    }

    /**
     * Gets all alerts (active and triggered/inactive), newest first
     */
    public List<PriceAlert> getAllAlerts() {
        return priceAlertRepository.findAllByOrderByCreatedDateDesc();
    }

    /**
     * Gets active alerts for a specific asset (case-insensitive symbol)
     */
    public List<PriceAlert> getAlertsForAsset(String assetSymbol) {
        if (assetSymbol == null || assetSymbol.isBlank()) {
            return List.of();
        }
        return priceAlertRepository.findByAssetSymbolIgnoreCaseAndActiveTrue(assetSymbol.trim());
    }

    /**
     * Deletes an alert by ID.
     *
     * @return true if the alert existed and was deleted, false otherwise
     */
    public boolean deleteAlert(Long id) {
        if (id == null || !priceAlertRepository.existsById(id)) {
            return false;
        }
        priceAlertRepository.deleteById(id);
        return true;
    }

    /**
     * Gets an alert by ID
     */
    public Optional<PriceAlert> getAlertById(Long id) {
        return priceAlertRepository.findById(id);
    }

    /**
     * Updates an alert status (e.g., deactivate when triggered)
     */
    public PriceAlert updateAlertStatus(Long id, boolean active) {
        Optional<PriceAlert> alertOptional = priceAlertRepository.findById(id);
        if (alertOptional.isPresent()) {
            PriceAlert alert = alertOptional.get();
            alert.setActive(active);
            return priceAlertRepository.save(alert);
        }
        return null;
    }

    /**
     * Marks an existing alert as triggered: deactivates it and stamps triggeredDate.
     * Use this (not createPriceAlert) when persisting a triggered alert.
     *
     * @return the updated alert, or null if it does not exist
     */
    public PriceAlert markAsTriggered(Long id) {
        if (id == null) {
            return null;
        }
        Optional<PriceAlert> alertOptional = priceAlertRepository.findById(id);
        if (alertOptional.isPresent()) {
            PriceAlert alert = alertOptional.get();
            alert.setActive(false);
            alert.setTriggeredDate(LocalDateTime.now());
            return priceAlertRepository.save(alert);
        }
        return null;
    }
}
