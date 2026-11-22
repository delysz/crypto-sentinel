package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "portfolio_performance")
public class PortfolioPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDate date;
    private double totalValue;
    private double cryptoValue;
    private double etfValue;
    private double stockValue;
    private double dailyChange;
    private double dailyChangePercentage;
    
    // Constructors
    public PortfolioPerformance() {}
    
    public PortfolioPerformance(LocalDate date, double totalValue, double cryptoValue, 
                               double etfValue, double stockValue) {
        this.date = date;
        this.totalValue = totalValue;
        this.cryptoValue = cryptoValue;
        this.etfValue = etfValue;
        this.stockValue = stockValue;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public double getTotalValue() {
        return totalValue;
    }
    
    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }
    
    public double getCryptoValue() {
        return cryptoValue;
    }
    
    public void setCryptoValue(double cryptoValue) {
        this.cryptoValue = cryptoValue;
    }
    
    public double getEtfValue() {
        return etfValue;
    }
    
    public void setEtfValue(double etfValue) {
        this.etfValue = etfValue;
    }
    
    public double getStockValue() {
        return stockValue;
    }
    
    public void setStockValue(double stockValue) {
        this.stockValue = stockValue;
    }
    
    public double getDailyChange() {
        return dailyChange;
    }
    
    public void setDailyChange(double dailyChange) {
        this.dailyChange = dailyChange;
    }
    
    public double getDailyChangePercentage() {
        return dailyChangePercentage;
    }
    
    public void setDailyChangePercentage(double dailyChangePercentage) {
        this.dailyChangePercentage = dailyChangePercentage;
    }
}