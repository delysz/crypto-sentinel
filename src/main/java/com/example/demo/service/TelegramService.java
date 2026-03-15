package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TelegramService {
    private static WebClient webClient = null;

    // REEMPLAZA ESTO CON TUS DATOS REALES
    private final String TOKEN = "8722639958:AAGBS50hg9Hkeg9bi0jPUtkeMIZolIiXcMI";
    private static final String CHAT_ID = "17762448";

    public TelegramService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.telegram.org/bot" + TOKEN).build();
    }

    public static void sendMessage(String text) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/sendMessage")
                        .queryParam("chat_id", CHAT_ID)
                        .queryParam("text", text)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(); // Esto lo envía "al aire" sin bloquear tu programa
    }
}