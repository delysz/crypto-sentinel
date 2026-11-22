package com.example.demo.service.price;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FinancePriceProviderTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private Double parse(String json) throws Exception {
        return FinancePriceProvider.extractPrice(mapper.readValue(json, FinancePriceProvider.ChartResponse.class));
    }

    @Test
    void extractsRegularMarketPriceFromChartResponse() throws Exception {
        String json = "{\"chart\":{\"result\":[{\"meta\":{\"currency\":\"USD\",\"symbol\":\"SPY\","
                + "\"regularMarketPrice\":689.81,\"fulldayChange\":-26.1},\"timestamp\":[1]}],\"error\":null}}";
        assertEquals(689.81, parse(json), 0.0001);
    }

    @Test
    void returnsNullForUnknownSymbol() throws Exception {
        String json = "{\"chart\":{\"result\":null,\"error\":{\"code\":\"Not Found\",\"description\":\"No data found\"}}}";
        assertNull(parse(json));
    }

    @Test
    void returnsNullForMissingOrZeroPrice() throws Exception {
        assertNull(parse("{\"chart\":{\"result\":[{\"meta\":{}}]}}"));
        assertNull(parse("{\"chart\":{\"result\":[{\"meta\":{\"regularMarketPrice\":0}}]}}"));
        assertNull(FinancePriceProvider.extractPrice(null));
    }
}
