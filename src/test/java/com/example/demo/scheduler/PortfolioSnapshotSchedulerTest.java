package com.example.demo.scheduler;

import com.example.demo.service.PortfolioService;
import com.example.demo.service.RiskAssessmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioSnapshotSchedulerTest {

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private RiskAssessmentService riskAssessmentService;

    @InjectMocks
    private PortfolioSnapshotScheduler scheduler;

    @Test
    void runsSnapshotAndRiskAssessment() {
        scheduler.takeDailySnapshot();

        verify(portfolioService).savePortfolioSnapshot();
        verify(riskAssessmentService).performRiskAssessment("default");
    }

    @Test
    void snapshotFailureDoesNotPreventRiskAssessment() {
        doThrow(new RuntimeException("db down")).when(portfolioService).savePortfolioSnapshot();

        assertDoesNotThrow(() -> scheduler.takeDailySnapshot());
        verify(riskAssessmentService).performRiskAssessment("default");
    }

    @Test
    void cronIsDailyAt2355() throws Exception {
        Scheduled scheduled = PortfolioSnapshotScheduler.class
                .getMethod("takeDailySnapshot").getAnnotation(Scheduled.class);
        assertEquals("0 55 23 * * *", scheduled.cron());
    }
}
