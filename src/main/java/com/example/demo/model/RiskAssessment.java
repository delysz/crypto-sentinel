package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String portfolioName;
    private double riskScore; // 0-100 scale
    private String riskLevel; // "low", "medium", "high"
    @Column(length = 1000)
    private String recommendation;
    private LocalDateTime assessmentDate;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "risk_assessment_distribution", joinColumns = @JoinColumn(name = "assessment_id"))
    @MapKeyColumn(name = "asset_class")
    @Column(name = "percentage")
    private Map<String, Double> assetDistribution; // asset symbol -> percentage
    
    // Constructors
    public RiskAssessment() {
        this.assessmentDate = LocalDateTime.now();
    }
    
    public RiskAssessment(String portfolioName, double riskScore, String riskLevel, 
                         String recommendation, Map<String, Double> assetDistribution) {
        this();
        this.portfolioName = portfolioName;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.recommendation = recommendation;
        this.assetDistribution = assetDistribution;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPortfolioName() {
        return portfolioName;
    }
    
    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }
    
    public double getRiskScore() {
        return riskScore;
    }
    
    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
    
    public LocalDateTime getAssessmentDate() {
        return assessmentDate;
    }
    
    public void setAssessmentDate(LocalDateTime assessmentDate) {
        this.assessmentDate = assessmentDate;
    }
    
    public Map<String, Double> getAssetDistribution() {
        return assetDistribution;
    }
    
    public void setAssetDistribution(Map<String, Double> assetDistribution) {
        this.assetDistribution = assetDistribution;
    }
}