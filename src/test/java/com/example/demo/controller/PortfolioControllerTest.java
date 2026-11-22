package com.example.demo.controller;

import com.example.demo.dto.HoldingView;
import com.example.demo.model.AssetType;
import com.example.demo.model.CryptoAsset;
import com.example.demo.service.PortfolioService;
import com.example.demo.service.RiskAssessmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PortfolioControllerTest {

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private RiskAssessmentService riskAssessmentService;

    private PortfolioController portfolioController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        portfolioController = new PortfolioController();
        inject("portfolioService", portfolioService);
        inject("riskAssessmentService", riskAssessmentService);
        mockMvc = MockMvcBuilders.standaloneSetup(portfolioController).build();
    }

    private void inject(String fieldName, Object value) throws Exception {
        var field = PortfolioController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(portfolioController, value);
    }

    @Test
    void testGetTotalPortfolioValue() {
        when(portfolioService.getTotalPortfolioValue()).thenReturn(53550.0);

        assertEquals(53550.0, portfolioController.getTotalPortfolioValue(), 0.01);
        verify(portfolioService, times(1)).getTotalPortfolioValue();
    }

    @Test
    void testGetPortfolioSummary() {
        PortfolioService.PortfolioSummary expectedSummary = summary();
        when(portfolioService.getPortfolioSummary()).thenReturn(expectedSummary);

        PortfolioService.PortfolioSummary result = portfolioController.getPortfolioSummary();

        assertSame(expectedSummary, result);
        verify(portfolioService, times(1)).getPortfolioSummary();
    }

    @Test
    void testGetPortfolioSummary_JsonShape() throws Exception {
        when(portfolioService.getPortfolioSummary()).thenReturn(summary());

        mockMvc.perform(get("/api/portfolio/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cryptoValue").value(53000.0))
                .andExpect(jsonPath("$.etfValue").value(400.0))
                .andExpect(jsonPath("$.stockValue").value(150.0))
                .andExpect(jsonPath("$.totalValue").value(53550.0))
                .andExpect(jsonPath("$.totalInvested").value(50000.0))
                .andExpect(jsonPath("$.profitLoss").value(3550.0))
                .andExpect(jsonPath("$.profitLossPercentage").value(7.1))
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    void testGetHoldings() throws Exception {
        when(portfolioService.getHoldings()).thenReturn(List.of(btcHolding()));

        mockMvc.perform(get("/api/portfolio/holdings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("BTC"))
                .andExpect(jsonPath("$[0].name").value("Bitcoin"))
                .andExpect(jsonPath("$[0].type").value("CRYPTO"))
                .andExpect(jsonPath("$[0].quantity").value(0.5))
                .andExpect(jsonPath("$[0].averageBuyPrice").value(40000.0))
                .andExpect(jsonPath("$[0].currentPrice").value(50000.0))
                .andExpect(jsonPath("$[0].marketValue").value(25000.0))
                .andExpect(jsonPath("$[0].invested").value(20000.0))
                .andExpect(jsonPath("$[0].profitLoss").value(5000.0))
                .andExpect(jsonPath("$[0].profitLossPercentage").value(25.0));
    }

    @Test
    void testUpdateHolding_Ok() throws Exception {
        CryptoAsset btc = new CryptoAsset();
        btc.setSymbol("BTC");
        btc.setName("Bitcoin");
        btc.setType(AssetType.CRYPTO);
        btc.setPrice(50000.0);
        btc.setQuantity(0.5);
        btc.setAverageBuyPrice(40000.0);
        when(portfolioService.updateHolding("btc", 0.5, 40000.0)).thenReturn(btc);

        mockMvc.perform(put("/api/portfolio/holdings/btc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 0.5, \"averageBuyPrice\": 40000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("BTC"))
                .andExpect(jsonPath("$.marketValue").value(25000.0))
                .andExpect(jsonPath("$.profitLoss").value(5000.0));
    }

    @Test
    void testUpdateHolding_NullAverageBuyPrice() throws Exception {
        CryptoAsset eth = new CryptoAsset();
        eth.setSymbol("ETH");
        eth.setPrice(3000.0);
        eth.setQuantity(2.0);
        when(portfolioService.updateHolding(eq("ETH"), eq(2.0), isNull())).thenReturn(eth);

        mockMvc.perform(put("/api/portfolio/holdings/ETH")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 2, \"averageBuyPrice\": null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marketValue").value(6000.0))
                .andExpect(jsonPath("$.averageBuyPrice", nullValue()));
    }

    @Test
    void testUpdateHolding_InvalidBodies() throws Exception {
        String[] invalid = {
                "{}",
                "{\"quantity\": \"abc\"}",
                "{\"quantity\": 1, \"averageBuyPrice\": \"x\"}",
                "not json"
        };
        for (String body : invalid) {
            mockMvc.perform(put("/api/portfolio/holdings/BTC")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }
        verify(portfolioService, never()).updateHolding(anyString(), any(), any());
    }

    @Test
    void testUpdateHolding_NegativeValueIs400() throws Exception {
        when(portfolioService.updateHolding(anyString(), anyDouble(), any()))
                .thenThrow(new IllegalArgumentException("quantity must be a non-negative number"));

        mockMvc.perform(put("/api/portfolio/holdings/BTC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": -1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("quantity must be a non-negative number"));
    }

    @Test
    void testUpdateHolding_UnknownSymbolIs404() throws Exception {
        when(portfolioService.updateHolding(eq("DOGE"), anyDouble(), any()))
                .thenThrow(new PortfolioService.HoldingNotFoundException("Unknown symbol: DOGE"));

        mockMvc.perform(put("/api/portfolio/holdings/DOGE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Unknown symbol: DOGE"));
    }

    @Test
    void testExportCsv() throws Exception {
        HoldingView quoted = new HoldingView();
        quoted.setSymbol("XYZ");
        quoted.setName("Foo, \"Bar\" Inc");
        quoted.setType(AssetType.STOCK);
        quoted.setQuantity(1234567.0);
        quoted.setCurrentPrice(null);
        quoted.setMarketValue(0.0);
        when(portfolioService.getHoldings()).thenReturn(List.of(btcHolding(), quoted));

        Locale previous = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY); // decimal comma: must not leak into the CSV
        try {
            MvcResult result = mockMvc.perform(get("/api/portfolio/export.csv"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("text/csv"))
                    .andExpect(header().string("Content-Disposition",
                            containsString("attachment; filename=\"portfolio.csv\"")))
                    .andReturn();

            String csv = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
            String[] lines = csv.split("\r\n");
            assertEquals(3, lines.length);
            assertEquals("symbol,name,type,quantity,averageBuyPrice,currentPrice,marketValue,invested,"
                    + "profitLoss,profitLossPercentage", lines[0]);
            assertEquals("BTC,Bitcoin,CRYPTO,0.5,40000,50000,25000,20000,5000,25", lines[1]);
            assertEquals("XYZ,\"Foo, \"\"Bar\"\" Inc\",STOCK,1234567,,,0,,0,0", lines[2]);
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    void testToCsv_EmptyHasOnlyHeader() {
        assertEquals(PortfolioController.CSV_HEADER + "\r\n", PortfolioController.toCsv(List.of()));
    }

    private static PortfolioService.PortfolioSummary summary() {
        PortfolioService.PortfolioSummary s = new PortfolioService.PortfolioSummary();
        s.setCryptoValue(53000.0);
        s.setEtfValue(400.0);
        s.setStockValue(150.0);
        s.setTotalValue(53550.0);
        s.setTotalInvested(50000.0);
        s.setProfitLoss(3550.0);
        s.setProfitLossPercentage(7.1);
        return s;
    }

    private static HoldingView btcHolding() {
        HoldingView h = new HoldingView();
        h.setSymbol("BTC");
        h.setName("Bitcoin");
        h.setType(AssetType.CRYPTO);
        h.setQuantity(0.5);
        h.setAverageBuyPrice(40000.0);
        h.setCurrentPrice(50000.0);
        h.setMarketValue(25000.0);
        h.setInvested(20000.0);
        h.setProfitLoss(5000.0);
        h.setProfitLossPercentage(25.0);
        return h;
    }
}
