package com.example.demo.integration;

import com.example.demo.CryptoCentinelaApplication;
import com.example.demo.model.CryptoAsset;
import com.example.demo.model.AssetType;
import com.example.demo.dto.HoldingView;
import com.example.demo.model.PortfolioPerformance;
import com.example.demo.repository.CryptoRepository;
import com.example.demo.repository.PortfolioPerformanceRepository;
import com.example.demo.service.PortfolioService;
import com.example.demo.scheduler.PriceAlertScheduler;
import com.example.demo.service.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CryptoCentinelaApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class PortfolioIntegrationTest {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private CryptoRepository cryptoRepository;

    @Autowired
    private PortfolioPerformanceRepository portfolioPerformanceRepository;

    // Evitamos que las tareas programadas llamen a APIs externas durante el test
    @MockitoBean
    private MonitoringService monitoringService;

    @MockitoBean
    private PriceAlertScheduler priceAlertScheduler;

    @BeforeEach
    void cleanDatabase() {
        cryptoRepository.deleteAll();
        portfolioPerformanceRepository.deleteAll();
    }

    @Test
    void testPortfolioCalculationWithRealData() {
        // Given - Create some test assets
        CryptoAsset btc = new CryptoAsset();
        btc.setSymbol("BTC");
        btc.setPrice(50000.0);
        btc.setQuantity(1.0);
        btc.setType(AssetType.CRYPTO);

        CryptoAsset eth = new CryptoAsset();
        eth.setSymbol("ETH");
        eth.setPrice(3000.0);
        eth.setQuantity(1.0);
        eth.setType(AssetType.CRYPTO);

        CryptoAsset spy = new CryptoAsset();
        spy.setSymbol("SPY");
        spy.setPrice(400.0);
        spy.setQuantity(1.0);
        spy.setType(AssetType.ETF);

        CryptoAsset aapl = new CryptoAsset();
        aapl.setSymbol("AAPL");
        aapl.setPrice(150.0);
        aapl.setQuantity(1.0);
        aapl.setType(AssetType.STOCK);

        List<CryptoAsset> assets = Arrays.asList(btc, eth, spy, aapl);
        
        // Save the test data
        cryptoRepository.saveAll(assets);

        // When
        double totalValue = portfolioService.getTotalPortfolioValue();
        PortfolioService.PortfolioSummary summary = portfolioService.getPortfolioSummary();

        // Then
        assertEquals(53550.0, totalValue, 0.01);
        assertEquals(53000.0, summary.getCryptoValue(), 0.01);
        assertEquals(400.0, summary.getEtfValue(), 0.01);
        assertEquals(150.0, summary.getStockValue(), 0.01);
        assertEquals(53550.0, summary.getTotalValue(), 0.01);
    }

    @Test
    void testEmptyPortfolio() {
        // When
        double totalValue = portfolioService.getTotalPortfolioValue();
        PortfolioService.PortfolioSummary summary = portfolioService.getPortfolioSummary();

        // Then
        assertEquals(0.0, totalValue, 0.01);
        assertEquals(0.0, summary.getCryptoValue(), 0.01);
        assertEquals(0.0, summary.getEtfValue(), 0.01);
        assertEquals(0.0, summary.getStockValue(), 0.01);
        assertEquals(0.0, summary.getTotalValue(), 0.01);
    }

    @Test
    void testUpdateHoldingPersistsAndDrivesValuesAndPnl() {
        CryptoAsset btc = new CryptoAsset();
        btc.setSymbol("BTC");
        btc.setName("Bitcoin");
        btc.setPrice(50000.0);
        btc.setType(AssetType.CRYPTO);
        CryptoAsset spy = new CryptoAsset();
        spy.setSymbol("SPY");
        spy.setPrice(400.0);
        spy.setType(AssetType.ETF);
        cryptoRepository.saveAll(Arrays.asList(btc, spy));

        // Por defecto la cantidad es 0 => valor 0
        assertEquals(0.0, portfolioService.getTotalPortfolioValue(), 0.01);

        portfolioService.updateHolding("btc", 0.5, 40000.0);
        portfolioService.updateHolding("Spy", 10.0, null);

        CryptoAsset reloaded = cryptoRepository.findBySymbol("BTC").orElseThrow();
        assertEquals(0.5, reloaded.getQuantity(), 0.0);
        assertEquals(40000.0, reloaded.getAverageBuyPrice(), 0.0);

        PortfolioService.PortfolioSummary summary = portfolioService.getPortfolioSummary();
        assertEquals(25000.0, summary.getCryptoValue(), 0.01);
        assertEquals(4000.0, summary.getEtfValue(), 0.01);
        assertEquals(29000.0, summary.getTotalValue(), 0.01);
        assertEquals(20000.0, summary.getTotalInvested(), 0.01);
        assertEquals(5000.0, summary.getProfitLoss(), 0.01);
        assertEquals(25.0, summary.getProfitLossPercentage(), 0.01);

        List<HoldingView> holdings = portfolioService.getHoldings();
        assertEquals(2, holdings.size());

        assertThrows(PortfolioService.HoldingNotFoundException.class,
                () -> portfolioService.updateHolding("DOGE", 1.0, null));
        assertThrows(IllegalArgumentException.class,
                () -> portfolioService.updateHolding("BTC", -1.0, null));
    }

    @Test
    void testSnapshotIsReplacedForSameDay() {
        CryptoAsset btc = new CryptoAsset();
        btc.setSymbol("BTC");
        btc.setPrice(100.0);
        btc.setQuantity(1.0);
        btc.setType(AssetType.CRYPTO);
        cryptoRepository.save(btc);

        portfolioService.savePortfolioSnapshot();
        portfolioService.updateHolding("BTC", 3.0, null);
        portfolioService.savePortfolioSnapshot();

        List<PortfolioPerformance> today = portfolioPerformanceRepository.findByDate(LocalDate.now());
        assertEquals(1, today.size());
        assertEquals(300.0, today.get(0).getTotalValue(), 0.01);
        assertEquals(1, portfolioPerformanceRepository.count());
    }
}
