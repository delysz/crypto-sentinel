package com.example.demo.service;

import com.example.demo.dto.HoldingView;
import com.example.demo.model.AssetType;
import com.example.demo.model.CryptoAsset;
import com.example.demo.model.PortfolioPerformance;
import com.example.demo.repository.CryptoRepository;
import com.example.demo.repository.PortfolioPerformanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PortfolioServiceTest {

    @Mock
    private CryptoRepository cryptoRepository;

    @Mock
    private PortfolioPerformanceRepository portfolioPerformanceRepository;

    private PortfolioService portfolioService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        portfolioService = new PortfolioService();
        inject("cryptoRepository", cryptoRepository);
        inject("portfolioPerformanceRepository", portfolioPerformanceRepository);
        when(cryptoRepository.save(any(CryptoAsset.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void inject(String fieldName, Object value) throws Exception {
        var field = PortfolioService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(portfolioService, value);
    }

    // ---------------------------------------------------------------- market value

    @Test
    void testGetTotalPortfolioValue_WithMultipleAssets() {
        when(cryptoRepository.findAll()).thenReturn(Arrays.asList(
                asset("BTC", 50000.0, AssetType.CRYPTO, 1.0),
                asset("ETH", 3000.0, AssetType.CRYPTO, 1.0),
                asset("SPY", 400.0, AssetType.ETF, 1.0),
                asset("AAPL", 150.0, AssetType.STOCK, 1.0)));

        assertEquals(53550.0, portfolioService.getTotalPortfolioValue(), 0.01);
        verify(cryptoRepository, times(1)).findAll();
    }

    @Test
    void testGetTotalPortfolioValue_UsesQuantityTimesPrice() {
        when(cryptoRepository.findAll()).thenReturn(Arrays.asList(
                asset("BTC", 50000.0, AssetType.CRYPTO, 0.5),
                asset("ETH", 3000.0, AssetType.CRYPTO, 2.0),
                asset("SPY", 400.0, AssetType.ETF, 0.0)));

        // 25000 + 6000 + 0
        assertEquals(31000.0, portfolioService.getTotalPortfolioValue(), 0.01);
    }

    @Test
    void testGetTotalPortfolioValue_DefaultQuantityIsZero() {
        CryptoAsset a = new CryptoAsset();
        a.setSymbol("BTC");
        a.setPrice(50000.0);
        assertEquals(0.0, a.getQuantity(), 0.0);
        when(cryptoRepository.findAll()).thenReturn(List.of(a));

        assertEquals(0.0, portfolioService.getTotalPortfolioValue(), 0.01);
    }

    @Test
    void testGetTotalPortfolioValue_WithNullPricesOrQuantities() {
        CryptoAsset nullQuantity = asset("SOL", 100.0, AssetType.CRYPTO, null);
        when(cryptoRepository.findAll()).thenReturn(Arrays.asList(
                asset("BTC", 50000.0, AssetType.CRYPTO, 1.0),
                asset("ETH", null, AssetType.CRYPTO, 3.0),
                nullQuantity));

        assertEquals(50000.0, portfolioService.getTotalPortfolioValue(), 0.01);
    }

    @Test
    void testGetTotalPortfolioValue_EmptyPortfolio() {
        when(cryptoRepository.findAll()).thenReturn(List.of());

        assertEquals(0.0, portfolioService.getTotalPortfolioValue(), 0.01);
    }

    @Test
    void testGetPortfolioSummary_WithMultipleAssetTypes() {
        when(cryptoRepository.findAll()).thenReturn(Arrays.asList(
                asset("BTC", 50000.0, AssetType.CRYPTO, 1.0),
                asset("ETH", 3000.0, AssetType.CRYPTO, 2.0),
                asset("SPY", 400.0, AssetType.ETF, 10.0),
                asset("AAPL", 150.0, AssetType.STOCK, 4.0)));

        PortfolioService.PortfolioSummary summary = portfolioService.getPortfolioSummary();

        assertEquals(56000.0, summary.getCryptoValue(), 0.01);
        assertEquals(4000.0, summary.getEtfValue(), 0.01);
        assertEquals(600.0, summary.getStockValue(), 0.01);
        assertEquals(60600.0, summary.getTotalValue(), 0.01);
        // Sin precios de compra no hay P&L
        assertEquals(0.0, summary.getTotalInvested(), 0.01);
        assertEquals(0.0, summary.getProfitLoss(), 0.01);
        assertEquals(0.0, summary.getProfitLossPercentage(), 0.01);
    }

    @Test
    void testGetPortfolioSummary_WithNullPrices() {
        when(cryptoRepository.findAll()).thenReturn(Arrays.asList(
                asset("BTC", null, AssetType.CRYPTO, 1.0),
                asset("SPY", 400.0, AssetType.ETF, 1.0)));

        PortfolioService.PortfolioSummary summary = portfolioService.getPortfolioSummary();

        assertEquals(0.0, summary.getCryptoValue(), 0.01);
        assertEquals(400.0, summary.getEtfValue(), 0.01);
        assertEquals(400.0, summary.getTotalValue(), 0.01);
    }

    @Test
    void testGetPortfolioSummary_EmptyPortfolio() {
        when(cryptoRepository.findAll()).thenReturn(List.of());

        PortfolioService.PortfolioSummary summary = portfolioService.getPortfolioSummary();

        assertEquals(0.0, summary.getTotalValue(), 0.01);
        assertEquals(0.0, summary.getProfitLossPercentage(), 0.01);
    }

    // ---------------------------------------------------------------- P&L

    @Test
    void testGetPortfolioSummary_ProfitLossOnlyCountsAssetsWithBuyPrice() {
        CryptoAsset btc = asset("BTC", 50000.0, AssetType.CRYPTO, 0.5);
        btc.setAverageBuyPrice(40000.0);          // invertido 20000, valor 25000
        CryptoAsset spy = asset("SPY", 400.0, AssetType.ETF, 10.0);
        spy.setAverageBuyPrice(500.0);            // invertido 5000, valor 4000
        CryptoAsset eth = asset("ETH", 3000.0, AssetType.CRYPTO, 1.0); // sin precio de compra
        when(cryptoRepository.findAll()).thenReturn(Arrays.asList(btc, spy, eth));

        PortfolioService.PortfolioSummary summary = portfolioService.getPortfolioSummary();

        assertEquals(32000.0, summary.getTotalValue(), 0.01);
        assertEquals(25000.0, summary.getTotalInvested(), 0.01);
        assertEquals(4000.0, summary.getProfitLoss(), 0.01);       // 29000 - 25000
        assertEquals(16.0, summary.getProfitLossPercentage(), 0.01);
    }

    @Test
    void testGetPortfolioSummary_ZeroInvestedGivesZeroPercentage() {
        CryptoAsset airdrop = asset("ARB", 2.0, AssetType.CRYPTO, 100.0);
        airdrop.setAverageBuyPrice(0.0);
        when(cryptoRepository.findAll()).thenReturn(List.of(airdrop));

        PortfolioService.PortfolioSummary summary = portfolioService.getPortfolioSummary();

        assertEquals(0.0, summary.getTotalInvested(), 0.0);
        assertEquals(200.0, summary.getProfitLoss(), 0.01);
        assertEquals(0.0, summary.getProfitLossPercentage(), 0.0);
        assertFalse(Double.isNaN(summary.getProfitLossPercentage()));
    }

    // ---------------------------------------------------------------- holdings

    @Test
    void testGetHoldings() {
        CryptoAsset btc = asset("BTC", 50000.0, AssetType.CRYPTO, 0.5);
        btc.setName("Bitcoin");
        btc.setAverageBuyPrice(40000.0);
        CryptoAsset eth = asset("ETH", 3000.0, AssetType.CRYPTO, 2.0);
        when(cryptoRepository.findAll()).thenReturn(Arrays.asList(btc, eth));

        List<HoldingView> holdings = portfolioService.getHoldings();

        assertEquals(2, holdings.size());
        HoldingView h = holdings.get(0);
        assertEquals("BTC", h.getSymbol());
        assertEquals("Bitcoin", h.getName());
        assertEquals(AssetType.CRYPTO, h.getType());
        assertEquals(0.5, h.getQuantity(), 0.0);
        assertEquals(40000.0, h.getAverageBuyPrice(), 0.0);
        assertEquals(50000.0, h.getCurrentPrice(), 0.0);
        assertEquals(25000.0, h.getMarketValue(), 0.01);
        assertEquals(20000.0, h.getInvested(), 0.01);
        assertEquals(5000.0, h.getProfitLoss(), 0.01);
        assertEquals(25.0, h.getProfitLossPercentage(), 0.01);

        HoldingView noCost = holdings.get(1);
        assertEquals(6000.0, noCost.getMarketValue(), 0.01);
        assertNull(noCost.getAverageBuyPrice());
        assertNull(noCost.getInvested());
        assertEquals(0.0, noCost.getProfitLoss(), 0.0);
        assertEquals(0.0, noCost.getProfitLossPercentage(), 0.0);
    }

    // ---------------------------------------------------------------- updateHolding

    @Test
    void testUpdateHolding_CaseInsensitive() {
        CryptoAsset btc = asset("BTC", 50000.0, AssetType.CRYPTO, 0.0);
        when(cryptoRepository.findAll()).thenReturn(List.of(asset("ETH", 3000.0, AssetType.CRYPTO, 0.0), btc));

        CryptoAsset updated = portfolioService.updateHolding("btc", 1.5, 30000.0);

        assertSame(btc, updated);
        assertEquals(1.5, updated.getQuantity(), 0.0);
        assertEquals(30000.0, updated.getAverageBuyPrice(), 0.0);
        verify(cryptoRepository).save(btc);
    }

    @Test
    void testUpdateHolding_NullAverageBuyPriceClearsIt() {
        CryptoAsset btc = asset("BTC", 50000.0, AssetType.CRYPTO, 1.0);
        btc.setAverageBuyPrice(1000.0);
        when(cryptoRepository.findAll()).thenReturn(List.of(btc));

        CryptoAsset updated = portfolioService.updateHolding("BTC", 2.0, null);

        assertEquals(2.0, updated.getQuantity(), 0.0);
        assertNull(updated.getAverageBuyPrice());
    }

    @Test
    void testUpdateHolding_UnknownSymbol() {
        when(cryptoRepository.findAll()).thenReturn(List.of(asset("BTC", 50000.0, AssetType.CRYPTO, 1.0)));

        assertThrows(PortfolioService.HoldingNotFoundException.class,
                () -> portfolioService.updateHolding("DOGE", 1.0, null));
        verify(cryptoRepository, never()).save(any());
    }

    @Test
    void testUpdateHolding_InvalidValues() {
        when(cryptoRepository.findAll()).thenReturn(List.of(asset("BTC", 50000.0, AssetType.CRYPTO, 1.0)));

        IllegalArgumentException negQty = assertThrows(IllegalArgumentException.class,
                () -> portfolioService.updateHolding("BTC", -1.0, null));
        assertFalse(negQty instanceof PortfolioService.HoldingNotFoundException);
        assertThrows(IllegalArgumentException.class, () -> portfolioService.updateHolding("BTC", 1.0, -5.0));
        assertThrows(IllegalArgumentException.class, () -> portfolioService.updateHolding("BTC", null, 5.0));
        assertThrows(IllegalArgumentException.class, () -> portfolioService.updateHolding("BTC", Double.NaN, null));
        assertThrows(IllegalArgumentException.class, () -> portfolioService.updateHolding(" ", 1.0, null));
        verify(cryptoRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- snapshots

    @Test
    void testSavePortfolioSnapshot_PreviousTotalZeroDoesNotProduceNaN() {
        when(cryptoRepository.findAll()).thenReturn(List.of(asset("BTC", 100.0, AssetType.CRYPTO, 2.0)));
        when(portfolioPerformanceRepository.findByDate(any())).thenReturn(List.of());
        PortfolioPerformance previous = new PortfolioPerformance(LocalDate.now().minusDays(1), 0, 0, 0, 0);
        when(portfolioPerformanceRepository.findFirstByDateBeforeOrderByDateDesc(any())).thenReturn(previous);

        portfolioService.savePortfolioSnapshot();

        ArgumentCaptor<PortfolioPerformance> captor = ArgumentCaptor.forClass(PortfolioPerformance.class);
        verify(portfolioPerformanceRepository).save(captor.capture());
        PortfolioPerformance saved = captor.getValue();
        assertEquals(LocalDate.now(), saved.getDate());
        assertEquals(200.0, saved.getTotalValue(), 0.01);
        assertEquals(200.0, saved.getCryptoValue(), 0.01);
        assertEquals(200.0, saved.getDailyChange(), 0.01);
        assertEquals(0.0, saved.getDailyChangePercentage(), 0.0);
    }

    @Test
    void testSavePortfolioSnapshot_ComputesDailyChange() {
        when(cryptoRepository.findAll()).thenReturn(List.of(asset("BTC", 110.0, AssetType.CRYPTO, 1.0)));
        when(portfolioPerformanceRepository.findByDate(any())).thenReturn(List.of());
        when(portfolioPerformanceRepository.findFirstByDateBeforeOrderByDateDesc(any()))
                .thenReturn(new PortfolioPerformance(LocalDate.now().minusDays(1), 100, 100, 0, 0));

        portfolioService.savePortfolioSnapshot();

        ArgumentCaptor<PortfolioPerformance> captor = ArgumentCaptor.forClass(PortfolioPerformance.class);
        verify(portfolioPerformanceRepository).save(captor.capture());
        assertEquals(10.0, captor.getValue().getDailyChange(), 0.01);
        assertEquals(10.0, captor.getValue().getDailyChangePercentage(), 0.01);
    }

    @Test
    void testSavePortfolioSnapshot_ReplacesExistingSnapshotForToday() {
        when(cryptoRepository.findAll()).thenReturn(List.of(asset("BTC", 100.0, AssetType.CRYPTO, 3.0)));
        PortfolioPerformance existing = new PortfolioPerformance(LocalDate.now(), 1, 1, 0, 0);
        existing.setId(7L);
        PortfolioPerformance duplicate = new PortfolioPerformance(LocalDate.now(), 2, 2, 0, 0);
        duplicate.setId(8L);
        when(portfolioPerformanceRepository.findByDate(LocalDate.now()))
                .thenReturn(new ArrayList<>(List.of(existing, duplicate)));

        portfolioService.savePortfolioSnapshot();

        verify(portfolioPerformanceRepository).save(existing);
        verify(portfolioPerformanceRepository).deleteAll(List.of(duplicate));
        assertEquals(7L, existing.getId());
        assertEquals(300.0, existing.getTotalValue(), 0.01);
        assertEquals(0.0, existing.getDailyChangePercentage(), 0.0);
    }

    private static CryptoAsset asset(String symbol, Double price, AssetType type, Double quantity) {
        CryptoAsset a = new CryptoAsset();
        a.setSymbol(symbol);
        a.setPrice(price);
        a.setType(type);
        a.setQuantity(quantity);
        return a;
    }
}
