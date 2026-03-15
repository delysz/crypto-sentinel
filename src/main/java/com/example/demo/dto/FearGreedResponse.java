package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class FearGreedResponse {
    private List<FearGreedData> data;

    @Data
    public static class FearGreedData {
        private String value;
        private String value_classification;
    }
}