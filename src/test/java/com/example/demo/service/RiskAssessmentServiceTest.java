package com.example.demo.service;

import com.example.demo.model.RiskAssessment;
import com.example.demo.repository.RiskAssessmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RiskAssessmentServiceTest {

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private PortfolioService portfolioService;

    @InjectMocks
    private RiskAssessmentService riskAssessmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void mockPortfolio(double crypto, double etf, double stock) {
        PortfolioService.PortfolioSummary summary = new PortfolioService.PortfolioSummary();
        summary.setCryptoValue(crypto);
        summary.setEtfValue(etf);
        summary.setStockValue(stock);
        summary.setTotalValue(crypto + etf + stock);
        when(portfolioService.getPortfolioSummary()).thenReturn(summary);
        when(portfolioService.getTotalPortfolioValue()).thenReturn(crypto + etf + stock);
    }

    @Test
    void emptyPortfolioReturnsNull() {
        mockPortfolio(0, 0, 0);

        assertNull(riskAssessmentService.performRiskAssessment("default"));
        verify(riskAssessmentRepository, never()).save(any());
    }

    @Test
    void allCryptoIsHighRisk() {
        mockPortfolio(1000, 0, 0);

        RiskAssessment result = riskAssessmentService.performRiskAssessment("default");

        assertEquals("high", result.getRiskLevel());
        assertEquals(100.0, result.getAssetDistribution().get("crypto"), 0.01);
    }

    @Test
    void allStocksIsMediumRisk() {
        mockPortfolio(0, 0, 1000);

        RiskAssessment result = riskAssessmentService.performRiskAssessment("default");

        assertEquals("medium", result.getRiskLevel());
        assertTrue(result.getRiskScore() >= 0 && result.getRiskScore() <= 100);
    }

    @Test
    void latestAssessmentIsFirstOfHistory() {
        RiskAssessment newest = new RiskAssessment();
        RiskAssessment older = new RiskAssessment();
        when(riskAssessmentRepository.findByPortfolioNameOrderByAssessmentDateDesc("default"))
                .thenReturn(List.of(newest, older));

        assertSame(newest, riskAssessmentService.getLatestRiskAssessment("default"));
    }
}
