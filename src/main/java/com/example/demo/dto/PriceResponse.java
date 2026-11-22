package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
// Lombok removed – explicit getters/setters added

public class PriceResponse {
     // Le decimos a Jackson: "Busca la clave 'usd' en el JSON y métela aquí"
    @JsonProperty("usd")
    private Double price;

    public Double getPrice() { return price; }
     public void setPrice(Double price) { this.price = price; }
 }