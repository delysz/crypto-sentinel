package com.example.demo.scheduler;

import com.example.demo.service.PortfolioService;
import com.example.demo.service.RiskAssessmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Guarda cada día a las 23:55 el snapshot de la cartera y una evaluación de riesgo.
 */
@Component
public class PortfolioSnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(PortfolioSnapshotScheduler.class);

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private RiskAssessmentService riskAssessmentService;

    @Scheduled(cron = "0 55 23 * * *")
    public void takeDailySnapshot() {
        try {
            portfolioService.savePortfolioSnapshot();
            log.info("Daily portfolio snapshot saved");
        } catch (Exception e) {
            log.error("Error saving daily portfolio snapshot", e);
        }
        try {
            riskAssessmentService.performRiskAssessment("default");
            log.info("Daily risk assessment saved");
        } catch (Exception e) {
            log.error("Error performing daily risk assessment", e);
        }
    }
}
