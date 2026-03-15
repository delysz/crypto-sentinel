package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class CryptoAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private String name;
    private Double price;
    private Double lastPrice;
    private LocalDateTime lastUpdated;

    // ESTO ES LO QUE HACE QUE LA GRÁFICA EXISTA
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Double> priceHistory = new ArrayList<>();
}