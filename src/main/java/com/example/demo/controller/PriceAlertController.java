package com.example.demo.controller;

import com.example.demo.model.PriceAlert;
import com.example.demo.service.PriceAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/alerts")
public class PriceAlertController {

    @Autowired
    private PriceAlertService priceAlertService;

    /**
     * Creates a new price alert.
     * 201 with the created alert, 400 with {"error": "..."} on invalid input.
     */
    @PostMapping
    public ResponseEntity<?> createAlert(@RequestBody PriceAlert alert) {
        try {
            PriceAlert createdAlert = priceAlertService.createPriceAlert(alert);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAlert);
        } catch (IllegalArgumentException e) {
            String message = e.getMessage() != null ? e.getMessage() : "Datos de alerta inválidos";
            return ResponseEntity.badRequest().body(Map.of("error", message));
        }
    }

    /**
     * Gets all active alerts
     */
    @GetMapping
    public ResponseEntity<List<PriceAlert>> getActiveAlerts() {
        return ResponseEntity.ok(priceAlertService.getActiveAlerts());
    }

    /**
     * Gets all alerts (active and inactive), newest first
     */
    @GetMapping("/all")
    public ResponseEntity<List<PriceAlert>> getAllAlerts() {
        return ResponseEntity.ok(priceAlertService.getAllAlerts());
    }

    /**
     * Gets active alerts for a specific asset (case-insensitive)
     */
    @GetMapping("/asset/{symbol}")
    public ResponseEntity<List<PriceAlert>> getAlertsForAsset(@PathVariable String symbol) {
        return ResponseEntity.ok(priceAlertService.getAlertsForAsset(symbol));
    }

    /**
     * Deletes an alert. 200 if deleted, 404 if it does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        if (priceAlertService.deleteAlert(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Gets an alert by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PriceAlert> getAlert(@PathVariable Long id) {
        Optional<PriceAlert> alert = priceAlertService.getAlertById(id);
        return alert.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
