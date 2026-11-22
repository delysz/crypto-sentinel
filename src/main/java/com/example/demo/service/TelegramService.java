package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TelegramService {

    private final WebClient webClient;

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String token;

    @Value("${TELEGRAM_CHAT_ID}")
    private String chatId;

    public TelegramService(WebClient.Builder builder,
                           @Value("${TELEGRAM_BOT_TOKEN}") String token) {
        this.webClient = builder
                .baseUrl("https://api.telegram.org/bot" + token)
                .build();
    }

    // NO static
    public void sendMessage(String text) {
        if (chatId == null || chatId.isBlank()) {
            System.err.println("⚠️ chat_id no está configurado. No se enviará el mensaje.");
            return;
        }
        // Build the request URL with required query parameters
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/sendMessage")
                        .queryParam("chat_id", chatId)
                        .queryParam("text", text)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> System.err.println("❌ Error al enviar mensaje a Telegram: " + e.getMessage()))
                .subscribe();
    }
}