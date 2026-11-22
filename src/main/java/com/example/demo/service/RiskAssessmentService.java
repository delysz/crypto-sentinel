package com.example.demo.service;

import com.example.demo.model.RiskAssessment;
import com.example.demo.repository.RiskAssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RiskAssessmentService {
    
    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;
    
    @Autowired
    private PortfolioService portfolioService;
    
    /**
     * Performs a risk assessment for the portfolio
     */
    public RiskAssessment performRiskAssessment(String portfolioName) {
        // Datos actuales de la cartera: valores de mercado (cantidad x precio) por tipo.
        // Se usa el total del propio resumen para que los porcentajes sean coherentes entre sí.
        var summary = portfolioService.getPortfolioSummary();
        double totalValue = summary.getTotalValue();
        
        if (totalValue <= 0) {
            return null;
        }
        
        // Calculate risk score based on various factors
        double riskScore = calculateRiskScore(summary, totalValue);
        String riskLevel = determineRiskLevel(riskScore);
        String recommendation = generateRecommendation(riskLevel, summary);
        
        // Create asset distribution map
        Map<String, Double> assetDistribution = new HashMap<>();
        assetDistribution.put("crypto", (summary.getCryptoValue() / totalValue) * 100);
        assetDistribution.put("etf", (summary.getEtfValue() / totalValue) * 100);
        assetDistribution.put("stock", (summary.getStockValue() / totalValue) * 100);
        
        RiskAssessment assessment = new RiskAssessment(
            portfolioName,
            riskScore,
            riskLevel,
            recommendation,
            assetDistribution
        );
        
        return riskAssessmentRepository.save(assessment);
    }
    
    /**
     * Calculates risk score based on portfolio composition and volatility factors
     */
    private double calculateRiskScore(com.example.demo.service.PortfolioService.PortfolioSummary summary, 
                                     double totalValue) {
        double cryptoPercentage = (summary.getCryptoValue() / totalValue) * 100;
        double etfPercentage = (summary.getEtfValue() / totalValue) * 100;
        double stockPercentage = (summary.getStockValue() / totalValue) * 100;
        
        // Base score calculation
        double riskScore = 0;
        
        // Crypto assets are considered high risk (weight higher)
        riskScore += cryptoPercentage * 2.0;  // Weighted higher for crypto
        
        // ETFs are moderate risk
        riskScore += etfPercentage * 1.0;
        
        // Stocks are moderate risk (but typically less volatile than crypto)
        riskScore += stockPercentage * 0.8;
        
        // Add some volatility factor based on portfolio diversity
        double diversification = calculateDiversification(cryptoPercentage, etfPercentage, stockPercentage);
        riskScore = riskScore * diversification;
        
        // Normalize to 0-100 scale
        return Math.min(100.0, Math.max(0.0, riskScore));
    }
    
    /**
     * Calculates diversification factor (higher is better)
     */
    private double calculateDiversification(double cryptoPerc, double etfPerc, double stockPerc) {
        // Simple diversification metric: lower concentration = higher diversification
        double maxConcentration = Math.max(Math.max(cryptoPerc, etfPerc), stockPerc);
        
        // If any single asset class takes more than 70% of portfolio, 
        // diversification is low (score between 0.5-1.0)
        if (maxConcentration > 70) {
            return 0.5 + (30.0 / 70.0) * (100 - maxConcentration) / 100.0;
        }
        
        // If well diversified, score approaches 1.0
        return 1.0;
    }
    
    /**
     * Determines risk level based on score
     */
    private String determineRiskLevel(double score) {
        if (score <= 30) {
            return "low";
        } else if (score <= 70) {
            return "medium";
        } else {
            return "high";
        }
    }
    
    /**
     * Generates recommendation based on risk level
     */
    private String generateRecommendation(String riskLevel, 
                                        com.example.demo.service.PortfolioService.PortfolioSummary summary) {
        switch (riskLevel) {
            case "low":
                return "Your portfolio is well-balanced and low-risk. Consider gradual diversification into other asset classes for potential growth.";
            case "medium":
                return "Your portfolio has moderate risk. Consider rebalancing to align with your risk tolerance and financial goals.";
            case "high":
                return "Your portfolio is high-risk. Consider reducing exposure to volatile assets or consulting a financial advisor for risk mitigation strategies.";
            default:
                return "Risk assessment completed. Review your asset allocation carefully.";
        }
    }
    
    /**
     * Gets recent risk assessments for a portfolio
     */
    public List<RiskAssessment> getPortfolioRiskHistory(String portfolioName) {
        return riskAssessmentRepository.findByPortfolioNameOrderByAssessmentDateDesc(portfolioName);
    }
    
    /**
     * Gets latest risk assessment for a portfolio
     */
    public RiskAssessment getLatestRiskAssessment(String portfolioName) {
        List<RiskAssessment> assessments = getPortfolioRiskHistory(portfolioName);
        return assessments.isEmpty() ? null : assessments.get(0);
    }
}