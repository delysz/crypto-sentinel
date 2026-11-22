package com.example.demo.service;

import com.example.demo.dto.HoldingView;
import com.example.demo.model.CryptoAsset;
import com.example.demo.model.PortfolioPerformance;
import com.example.demo.repository.CryptoRepository;
import com.example.demo.repository.PortfolioPerformanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PortfolioService {

    @Autowired
    private CryptoRepository cryptoRepository;

    @Autowired
    private PortfolioPerformanceRepository portfolioPerformanceRepository;

    /**
     * Calcula el valor de mercado total del portafolio (Σ cantidad × precio).
     */
    public double getTotalPortfolioValue() {
        double totalValue = 0.0;
        for (CryptoAsset asset : cryptoRepository.findAll()) {
            totalValue += asset.getMarketValue();
        }
        return totalValue;
    }

    /**
     * Resumen del portafolio: valor de mercado por tipo de activo y P&L total.
     * totalInvested y profitLoss solo consideran los activos con precio medio de compra.
     */
    public PortfolioSummary getPortfolioSummary() {
        PortfolioSummary summary = new PortfolioSummary();
        double marketValueWithCost = 0.0;

        for (CryptoAsset asset : cryptoRepository.findAll()) {
            double marketValue = asset.getMarketValue();
            if (asset.getType() != null) {
                switch (asset.getType()) {
                    case CRYPTO -> summary.setCryptoValue(summary.getCryptoValue() + marketValue);
                    case ETF -> summary.setEtfValue(summary.getEtfValue() + marketValue);
                    case STOCK -> summary.setStockValue(summary.getStockValue() + marketValue);
                }
            }
            if (asset.getAverageBuyPrice() != null) {
                summary.setTotalInvested(summary.getTotalInvested() + asset.getQuantity() * asset.getAverageBuyPrice());
                marketValueWithCost += marketValue;
            }
        }

        summary.setTotalValue(summary.getCryptoValue() + summary.getEtfValue() + summary.getStockValue());
        summary.setProfitLoss(marketValueWithCost - summary.getTotalInvested());
        summary.setProfitLossPercentage(percentage(summary.getProfitLoss(), summary.getTotalInvested()));
        return summary;
    }

    /**
     * Posiciones de la cartera con valor de mercado y P&L por activo.
     */
    public List<HoldingView> getHoldings() {
        List<HoldingView> holdings = new ArrayList<>();
        for (CryptoAsset asset : cryptoRepository.findAll()) {
            holdings.add(toHoldingView(asset));
        }
        return holdings;
    }

    /**
     * Construye la vista de una posición a partir del activo.
     */
    public static HoldingView toHoldingView(CryptoAsset asset) {
        HoldingView view = new HoldingView();
        view.setSymbol(asset.getSymbol());
        view.setName(asset.getName());
        view.setType(asset.getType());
        view.setQuantity(asset.getQuantity());
        view.setAverageBuyPrice(asset.getAverageBuyPrice());
        view.setCurrentPrice(asset.getPrice());
        double marketValue = asset.getMarketValue();
        view.setMarketValue(marketValue);
        if (asset.getAverageBuyPrice() != null) {
            double invested = asset.getQuantity() * asset.getAverageBuyPrice();
            double profitLoss = marketValue - invested;
            view.setInvested(invested);
            view.setProfitLoss(profitLoss);
            view.setProfitLossPercentage(percentage(profitLoss, invested));
        }
        return view;
    }

    /**
     * Actualiza la posición (cantidad y precio medio de compra) de un activo.
     *
     * @throws HoldingNotFoundException si no existe el símbolo (búsqueda sin distinguir mayúsculas)
     * @throws IllegalArgumentException si los valores son nulos (cantidad), negativos o no finitos
     */
    @Transactional
    public CryptoAsset updateHolding(String symbol, Double quantity, Double averageBuyPrice) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol is required");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("quantity is required");
        }
        if (quantity.isNaN() || quantity.isInfinite() || quantity < 0) {
            throw new IllegalArgumentException("quantity must be a non-negative number");
        }
        if (averageBuyPrice != null
                && (averageBuyPrice.isNaN() || averageBuyPrice.isInfinite() || averageBuyPrice < 0)) {
            throw new IllegalArgumentException("averageBuyPrice must be a non-negative number or null");
        }

        String wanted = symbol.trim();
        CryptoAsset asset = cryptoRepository.findAll().stream()
                .filter(a -> a.getSymbol() != null && a.getSymbol().equalsIgnoreCase(wanted))
                .findFirst()
                .orElseThrow(() -> new HoldingNotFoundException("Unknown symbol: " + wanted));

        asset.setQuantity(quantity);
        asset.setAverageBuyPrice(averageBuyPrice);
        return cryptoRepository.save(asset);
    }

    /**
     * Guarda el snapshot diario de la cartera. Si ya existe uno para hoy se sobrescribe
     * (y se eliminan posibles duplicados antiguos) en lugar de crear otro.
     */
    @Transactional
    public void savePortfolioSnapshot() {
        LocalDate today = LocalDate.now();
        PortfolioSummary summary = getPortfolioSummary();

        List<PortfolioPerformance> existing = portfolioPerformanceRepository.findByDate(today);
        PortfolioPerformance snapshot;
        if (existing == null || existing.isEmpty()) {
            snapshot = new PortfolioPerformance();
            snapshot.setDate(today);
        } else {
            snapshot = existing.get(0);
            if (existing.size() > 1) {
                portfolioPerformanceRepository.deleteAll(existing.subList(1, existing.size()));
            }
        }

        snapshot.setTotalValue(summary.getTotalValue());
        snapshot.setCryptoValue(summary.getCryptoValue());
        snapshot.setEtfValue(summary.getEtfValue());
        snapshot.setStockValue(summary.getStockValue());

        // Variación respecto al último snapshot de un día anterior
        PortfolioPerformance previous = portfolioPerformanceRepository.findFirstByDateBeforeOrderByDateDesc(today);
        if (previous != null) {
            double change = snapshot.getTotalValue() - previous.getTotalValue();
            snapshot.setDailyChange(change);
            snapshot.setDailyChangePercentage(percentage(change, previous.getTotalValue()));
        } else {
            snapshot.setDailyChange(0);
            snapshot.setDailyChangePercentage(0);
        }

        portfolioPerformanceRepository.save(snapshot);
    }

    /**
     * Gets performance data for charts
     */
    public List<PortfolioPerformance> getPerformanceHistory(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return portfolioPerformanceRepository.findByDateAfterOrderByDateAsc(startDate);
    }

    /**
     * Gets current portfolio performance metrics
     */
    public PortfolioPerformance getCurrentPerformance() {
        return portfolioPerformanceRepository.findFirstByOrderByDateDesc();
    }

    /** value / base × 100, o 0 si la base es 0. */
    private static double percentage(double value, double base) {
        return base == 0 ? 0.0 : (value / base) * 100.0;
    }

    /**
     * Se lanza cuando no existe ningún activo con el símbolo indicado.
     */
    public static class HoldingNotFoundException extends IllegalArgumentException {
        public HoldingNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Resumen del portafolio. JSON: {cryptoValue, etfValue, stockValue, totalValue,
     * totalInvested, profitLoss, profitLossPercentage}.
     */
    public static class PortfolioSummary {
        private double cryptoValue = 0.0;
        private double etfValue = 0.0;
        private double stockValue = 0.0;
        private double totalValue = 0.0;
        private double totalInvested = 0.0;
        private double profitLoss = 0.0;
        private double profitLossPercentage = 0.0;

        public double getCryptoValue() { return cryptoValue; }
        public void setCryptoValue(double cryptoValue) { this.cryptoValue = cryptoValue; }
        public double getEtfValue() { return etfValue; }
        public void setEtfValue(double etfValue) { this.etfValue = etfValue; }
        public double getStockValue() { return stockValue; }
        public void setStockValue(double stockValue) { this.stockValue = stockValue; }
        public double getTotalValue() { return totalValue; }
        public void setTotalValue(double totalValue) { this.totalValue = totalValue; }
        public double getTotalInvested() { return totalInvested; }
        public void setTotalInvested(double totalInvested) { this.totalInvested = totalInvested; }
        public double getProfitLoss() { return profitLoss; }
        public void setProfitLoss(double profitLoss) { this.profitLoss = profitLoss; }
        public double getProfitLossPercentage() { return profitLossPercentage; }
        public void setProfitLossPercentage(double profitLossPercentage) { this.profitLossPercentage = profitLossPercentage; }
    }
}
