package com.example.demo.dto;

import com.example.demo.model.AssetType;

/**
 * Vista de una posición de la cartera (un activo) con su valor de mercado y P&L.
 * {@code averageBuyPrice}, {@code currentPrice} e {@code invested} pueden ser null
 * cuando no se conocen; en ese caso profitLoss/profitLossPercentage valen 0.
 */
public class HoldingView {
    private String symbol;
    private String name;
    private AssetType type;
    private double quantity;
    private Double averageBuyPrice;
    private Double currentPrice;
    private double marketValue;
    private Double invested;
    private double profitLoss;
    private double profitLossPercentage;

    public HoldingView() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AssetType getType() { return type; }
    public void setType(AssetType type) { this.type = type; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public Double getAverageBuyPrice() { return averageBuyPrice; }
    public void setAverageBuyPrice(Double averageBuyPrice) { this.averageBuyPrice = averageBuyPrice; }
    public Double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }
    public double getMarketValue() { return marketValue; }
    public void setMarketValue(double marketValue) { this.marketValue = marketValue; }
    public Double getInvested() { return invested; }
    public void setInvested(Double invested) { this.invested = invested; }
    public double getProfitLoss() { return profitLoss; }
    public void setProfitLoss(double profitLoss) { this.profitLoss = profitLoss; }
    public double getProfitLossPercentage() { return profitLossPercentage; }
    public void setProfitLossPercentage(double profitLossPercentage) { this.profitLossPercentage = profitLossPercentage; }
}
