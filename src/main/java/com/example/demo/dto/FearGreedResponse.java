package com.example.demo.dto;

// Lombok removed – explicit getters/setters added

import java.util.List;

public class FearGreedResponse {
     private List<FearGreedData> data;

    public List<FearGreedData> getData() { return data; }
    public void setData(List<FearGreedData> data) { this.data = data; }

     public static class FearGreedData {
        private String value;
        private String value_classification;

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getValue_classification() { return value_classification; }
        public void setValue_classification(String value_classification) { this.value_classification = value_classification; }
     }
 }