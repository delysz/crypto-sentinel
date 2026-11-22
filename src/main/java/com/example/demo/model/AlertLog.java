package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
// Lombok removed – explicit getters/setters added


import java.time.LocalDateTime;

@Entity
public class AlertLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cryptoName;
    private Double priceAtAlert;
    private Double dropPercentage;
    private LocalDateTime timestamp;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCryptoName() { return cryptoName; }
    public void setCryptoName(String cryptoName) { this.cryptoName = cryptoName; }
    public Double getPriceAtAlert() { return priceAtAlert; }
    public void setPriceAtAlert(Double priceAtAlert) { this.priceAtAlert = priceAtAlert; }
    public Double getDropPercentage() { return dropPercentage; }
    public void setDropPercentage(Double dropPercentage) { this.dropPercentage = dropPercentage; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}