package com.example.demo.controller;

import com.example.demo.model.CryptoAsset;
import com.example.demo.repository.AlertLogRepository;
import com.example.demo.repository.CryptoRepository;
import com.example.demo.service.CoinGeckoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class DashboardController {

    @Autowired private CryptoRepository repository;
    @Autowired private AlertLogRepository alertLogRepository;
    @Autowired private CoinGeckoService cryptoService; // Lo necesitamos para el Fear & Greed

    // SOLO UN MÉTODO INDEX (El que manda en la Home)
    @GetMapping("/")
    public String index(Model model) {
        // 1. Cargamos las criptos
        model.addAttribute("cryptos", repository.findAll());

        // 2. Cargamos el historial de alertas
        model.addAttribute("alerts", alertLogRepository.findTop10ByOrderByTimestampDesc());

        // 3. Cargamos el sentimiento del mercado
        model.addAttribute("fng", cryptoService.getFearAndGreedIndex());

        return "index";
    }

    @PostMapping("/add")
    public String add(@RequestParam String symbol) {
        CryptoAsset asset = new CryptoAsset();
        asset.setSymbol(symbol.toLowerCase().trim());
        asset.setName(symbol.toUpperCase());
        asset.setPrice(0.0);
        asset.setLastUpdated(LocalDateTime.now());
        repository.save(asset);
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/";
    }
}