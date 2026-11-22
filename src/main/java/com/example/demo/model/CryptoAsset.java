package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
// Lombok removed – explicit getters/setters added below
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class CryptoAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private String name;
    private Double price;
    private Double lastPrice;
    private LocalDateTime lastUpdated;
    // NEW: Tipo de activo (Crypto, ETF, Stock, etc.)
    @Enumerated(EnumType.STRING)
    private AssetType type = AssetType.CRYPTO;

    // ESTO ES LO QUE HACE QUE LA GRÁFICA EXISTA
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Double> priceHistory = new ArrayList<>();

    // Posición: unidades en cartera y precio medio de compra (null = desconocido)
    private Double quantity = 0.0;
    private Double averageBuyPrice;

    // ----- Getters & Setters -----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Double getLastPrice() { return lastPrice; }
    public void setLastPrice(Double lastPrice) { this.lastPrice = lastPrice; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public AssetType getType() { return type; }
    public void setType(AssetType type) { this.type = type; }
    public List<Double> getPriceHistory() { return priceHistory; }
    public void setPriceHistory(List<Double> priceHistory) { this.priceHistory = priceHistory; }
    /** Unidades en cartera; nunca null (filas antiguas sin columna se leen como 0). */
    public Double getQuantity() { return quantity != null ? quantity : 0.0; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public Double getAverageBuyPrice() { return averageBuyPrice; }
    public void setAverageBuyPrice(Double averageBuyPrice) { this.averageBuyPrice = averageBuyPrice; }

    /** Valor de mercado = cantidad x precio actual (0 si falta alguno). */
    @Transient
    public double getMarketValue() {
        if (price == null || quantity == null) {
            return 0.0;
        }
        return quantity * price;
    }
}
