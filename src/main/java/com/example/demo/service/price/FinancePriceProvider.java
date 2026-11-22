package com.example.demo.service.price;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class FinancePriceProvider implements PriceProvider {
    private final WebClient webClient;

    public FinancePriceProvider(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://query1.finance.yahoo.com/v7/finance/quote")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    public Double getPrice(String symbol) {
        try {
            // Yahoo Finance API returns JSON with nested structure. We'll fetch minimal fields.
            YahooQuoteResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("symbols", symbol)
                            .build())
                    .retrieve()
                    .bodyToMono(YahooQuoteResponse.class)
                    .block();
            if (response == null || response.getQuoteResponse() == null || response.getQuoteResponse().getResult() == null || response.getQuoteResponse().getResult().isEmpty()) {
                return null;
            }
            return response.getQuoteResponse().getResult().get(0).getRegularMarketPrice();
        } catch (Exception e) {
            System.err.println("❌ YahooFinanceProvider error for " + symbol + ": " + e.getMessage());
            return null;
        }
    }

    // DTO classes for minimal Yahoo response
    private static class YahooQuoteResponse {
        private YahooQuoteResponseData quoteResponse;

        public YahooQuoteResponseData getQuoteResponse() {
            return quoteResponse;
        }

        public void setQuoteResponse(YahooQuoteResponseData quoteResponse) {
            this.quoteResponse = quoteResponse;
        }
    }

    private static class YahooQuoteResponseData {
        private java.util.List<YahooQuote> result;
        public java.util.List<YahooQuote> getResult() { return result; }
        public void setResult(java.util.List<YahooQuote> result) { this.result = result; }
    }

    private static class YahooQuote {
        private Double regularMarketPrice;
        public Double getRegularMarketPrice() { return regularMarketPrice; }
        public void setRegularMarketPrice(Double regularMarketPrice) { this.regularMarketPrice = regularMarketPrice; }
    }
}
