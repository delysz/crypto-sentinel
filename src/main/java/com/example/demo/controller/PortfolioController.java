package com.example.demo.controller;

import com.example.demo.dto.HoldingView;
import com.example.demo.model.CryptoAsset;
import com.example.demo.model.PortfolioPerformance;
import com.example.demo.model.RiskAssessment;
import com.example.demo.service.PortfolioService;
import com.example.demo.service.RiskAssessmentService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    
    @Autowired
    private PortfolioService portfolioService;
    
    @Autowired
    private RiskAssessmentService riskAssessmentService;
    
    /**
     * Obtiene el valor total del portafolio
     * @return Valor total del portafolio
     */
    @GetMapping("/total")
    public double getTotalPortfolioValue() {
        return portfolioService.getTotalPortfolioValue();
    }
    
    /**
     * Obtiene el resumen del portafolio por tipo de activo
     * @return Resumen del portafolio
     */
    @GetMapping("/summary")
    public PortfolioService.PortfolioSummary getPortfolioSummary() {
        return portfolioService.getPortfolioSummary();
    }
    
    /**
     * Gets portfolio performance history for charts
     * @param days Number of days to retrieve (default 30)
     * @return List of performance snapshots
     */
    @GetMapping("/performance/history")
    public List<PortfolioPerformance> getPerformanceHistory(
            @RequestParam(defaultValue = "30") int days) {
        return portfolioService.getPerformanceHistory(days);
    }
    
    /**
     * Gets current portfolio performance metrics
     * @return Current performance data
     */
    @GetMapping("/performance/current")
    public PortfolioPerformance getCurrentPerformance() {
        return portfolioService.getCurrentPerformance();
    }
    
    /**
     * Saves a snapshot of the current portfolio performance
     * @return Success status
     */
    @PostMapping("/performance/snapshot")
    public boolean savePortfolioSnapshot() {
        try {
            portfolioService.savePortfolioSnapshot();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Ejecuta y guarda una nueva evaluación de riesgo
     */
    @PostMapping("/risk")
    public ResponseEntity<RiskAssessment> performRiskAssessment(
            @RequestParam(defaultValue = "default") String portfolio) {
        RiskAssessment assessment = riskAssessmentService.performRiskAssessment(portfolio);
        return assessment != null ? ResponseEntity.ok(assessment) : ResponseEntity.noContent().build();
    }
    
    /**
     * Última evaluación de riesgo guardada
     */
    @GetMapping("/risk")
    public ResponseEntity<RiskAssessment> getLatestRiskAssessment(
            @RequestParam(defaultValue = "default") String portfolio) {
        RiskAssessment assessment = riskAssessmentService.getLatestRiskAssessment(portfolio);
        return assessment != null ? ResponseEntity.ok(assessment) : ResponseEntity.notFound().build();
    }
    
    /**
     * Histórico de evaluaciones de riesgo
     */
    @GetMapping("/risk/history")
    public List<RiskAssessment> getRiskHistory(
            @RequestParam(defaultValue = "default") String portfolio) {
        return riskAssessmentService.getPortfolioRiskHistory(portfolio);
    }

    // ------------------------------------------------------------------
    // Holdings (posiciones)
    // ------------------------------------------------------------------

    static final String CSV_HEADER =
            "symbol,name,type,quantity,averageBuyPrice,currentPrice,marketValue,invested,profitLoss,profitLossPercentage";

    /**
     * Posiciones de la cartera con valor de mercado y P&L.
     */
    @GetMapping("/holdings")
    public List<HoldingView> getHoldings() {
        return portfolioService.getHoldings();
    }

    /**
     * Actualiza cantidad y precio medio de compra de un activo.
     * Body: {"quantity": number, "averageBuyPrice": number|null}
     */
    @PutMapping("/holdings/{symbol}")
    public ResponseEntity<?> updateHolding(@PathVariable String symbol,
                                           @RequestBody(required = false) Map<String, Object> body) {
        if (body == null) {
            return badRequest("Request body is required");
        }
        Object rawQuantity = body.get("quantity");
        if (!(rawQuantity instanceof Number)) {
            return badRequest("quantity must be a number");
        }
        Object rawAvg = body.get("averageBuyPrice");
        if (rawAvg != null && !(rawAvg instanceof Number)) {
            return badRequest("averageBuyPrice must be a number or null");
        }
        Double quantity = ((Number) rawQuantity).doubleValue();
        Double averageBuyPrice = rawAvg == null ? null : ((Number) rawAvg).doubleValue();
        try {
            CryptoAsset updated = portfolioService.updateHolding(symbol, quantity, averageBuyPrice);
            return ResponseEntity.ok(PortfolioService.toHoldingView(updated));
        } catch (PortfolioService.HoldingNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    /**
     * Exporta las posiciones como CSV descargable.
     */
    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] csv = toCsv(portfolioService.getHoldings()).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("portfolio.csv").build().toString())
                .body(csv);
    }

    /** JSON mal formado en este controlador → 400 {"error": ...}. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException e) {
        return badRequest("Malformed JSON request body");
    }

    private static ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message == null ? "Invalid request" : message));
    }

    /** Genera el CSV (RFC 4180, separador coma, números con Locale.ROOT, fin de línea CRLF). */
    static String toCsv(List<HoldingView> holdings) {
        StringBuilder sb = new StringBuilder(CSV_HEADER).append("\r\n");
        for (HoldingView h : holdings) {
            sb.append(csvText(h.getSymbol())).append(',')
              .append(csvText(h.getName())).append(',')
              .append(csvText(h.getType() == null ? null : h.getType().name())).append(',')
              .append(csvNumber(h.getQuantity())).append(',')
              .append(csvNumber(h.getAverageBuyPrice())).append(',')
              .append(csvNumber(h.getCurrentPrice())).append(',')
              .append(csvNumber(h.getMarketValue())).append(',')
              .append(csvNumber(h.getInvested())).append(',')
              .append(csvNumber(h.getProfitLoss())).append(',')
              .append(csvNumber(h.getProfitLossPercentage()))
              .append("\r\n");
        }
        return sb.toString();
    }

    private static String csvText(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String csvNumber(Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return "";
        }
        // DecimalFormat no es thread-safe: se crea por llamada
        DecimalFormat format = new DecimalFormat("0.##########", DecimalFormatSymbols.getInstance(Locale.ROOT));
        format.setGroupingUsed(false);
        return format.format(value);
    }
}
