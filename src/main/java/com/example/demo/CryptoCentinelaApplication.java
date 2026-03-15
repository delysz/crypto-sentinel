package com.example.demo;

import com.example.demo.model.CryptoAsset;
import com.example.demo.repository.CryptoRepository;
import com.example.demo.service.CoinGeckoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class CryptoCentinelaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoCentinelaApplication.class, args);
	}

	@Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}
}