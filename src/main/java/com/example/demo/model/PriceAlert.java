package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_alerts")
public class PriceAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String assetName;
    private String assetSymbol;
    private double targetPrice;
    private String alertType; // "above" or "below"
    private boolean active = true;
    private LocalDateTime createdDate;
    private LocalDateTime triggeredDate;
    
    // Constructors
    public PriceAlert() {
        this.createdDate = LocalDateTime.now();
    }
    
    public PriceAlert(String assetName, String assetSymbol, double targetPrice, String alertType) {
        this();
        this.assetName = assetName;
        this.assetSymbol = assetSymbol;
        this.targetPrice = targetPrice;
        this.alertType = alertType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAssetName() {
        return assetName;
    }
    
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
    
    public String getAssetSymbol() {
        return assetSymbol;
    }
    
    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }
    
    public double getTargetPrice() {
        return targetPrice;
    }
    
    public void setTargetPrice(double targetPrice) {
        this.targetPrice = targetPrice;
    }
    
    public String getAlertType() {
        return alertType;
    }
    
    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getTriggeredDate() {
        return triggeredDate;
    }
    
    public void setTriggeredDate(LocalDateTime triggeredDate) {
        this.triggeredDate = triggeredDate;
    }
}