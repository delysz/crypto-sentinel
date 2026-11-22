package com.example.demo.security;

import com.example.demo.scheduler.PortfolioSnapshotScheduler;
import com.example.demo.scheduler.PriceAlertScheduler;
import com.example.demo.service.CoinGeckoService;
import com.example.demo.service.MonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@WithMockUser
class CsrfProtectionTest {

    @Autowired
    private MockMvc mockMvc;

    // Sin llamadas externas
    @MockitoBean private MonitoringService monitoringService;
    @MockitoBean private PriceAlertScheduler priceAlertScheduler;
    @MockitoBean private PortfolioSnapshotScheduler portfolioSnapshotScheduler;
    @MockitoBean private CoinGeckoService coinGeckoService;

    @Test
    void addWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/add").param("symbol", "bitcoin"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addWithCsrfTokenRedirects() throws Exception {
        mockMvc.perform(post("/add").param("symbol", "bitcoin").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteViaGetIsNoLongerAllowed() throws Exception {
        mockMvc.perform(get("/delete/1"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void deleteWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/delete/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void apiPutWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(put("/api/portfolio/holdings/bitcoin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void dashboardRendersCsrfMetaTag() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
    }
}
