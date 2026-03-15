package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PriceResponse {
    // Le decimos a Jackson: "Busca la clave 'usd' en el JSON y métela aquí"
    @JsonProperty("usd")
    private Double price;
}