package com.example.demo.service;

import com.example.demo.dto.FearGreedResponse;
import com.example.demo.dto.PriceResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class CoinGeckoService {
    private final WebClient webClient;

    public CoinGeckoService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.coingecko.com/api/v3").build();
    }

    // Ahora devolvemos un MAPA completo: Clave = Nombre de Cripto, Valor = Nuestro DTO
    public Map<String, PriceResponse> getMultiplePrices(String ids) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/simple/price")
                        .queryParam("ids", ids)
                        .queryParam("vs_currencies", "usd")
                        .build())
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, PriceResponse>>() {})
                .block();
    }

    public FearGreedResponse.FearGreedData getFearAndGreedIndex() {
        FearGreedResponse response = webClient.get()
                .uri("https://api.alternative.me/fng/")
                .retrieve()
                .bodyToMono(FearGreedResponse.class)
                .block();

        return (response != null && !response.getData().isEmpty()) ? response.getData().get(0) : null;
    }
}