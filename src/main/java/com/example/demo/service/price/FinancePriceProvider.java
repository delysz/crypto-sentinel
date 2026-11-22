package com.example.demo.service.price;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

/**
 * Precios de ETFs y acciones desde Yahoo Finance.
 *
 * Usa el endpoint v8/finance/chart: el antiguo v7/finance/quote exige cookie + "crumb"
 * y responde 401. Yahoo devuelve 429 a clientes sin User-Agent de navegador.
 */
@Component
public class FinancePriceProvider implements PriceProvider {
    private final WebClient webClient;

    public FinancePriceProvider(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://query1.finance.yahoo.com/v8/finance/chart")
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    public Double getPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }
        try {
            ChartResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{symbol}")
                            .queryParam("interval", "1d")
                            .queryParam("range", "1d")
                            .build(symbol.trim().toUpperCase()))
                    .retrieve()
                    .bodyToMono(ChartResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return extractPrice(response);
        } catch (Exception e) {
            System.err.println("❌ YahooFinanceProvider error for " + symbol + ": " + e.getMessage());
            return null;
        }
    }

    static Double extractPrice(ChartResponse response) {
        if (response == null || response.chart == null || response.chart.result == null
                || response.chart.result.isEmpty() || response.chart.result.get(0).meta == null) {
            return null;
        }
        Double price = response.chart.result.get(0).meta.regularMarketPrice;
        return (price != null && price > 0) ? price : null;
    }

    // DTOs mínimos de la respuesta de Yahoo (se ignora el resto de campos)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChartResponse {
        public Chart chart;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Chart {
        public List<Result> result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Result {
        public Meta meta;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Meta {
        public Double regularMarketPrice;
    }
}
