package com.example.demo.service;

import com.example.demo.model.CryptoAsset;
import com.example.demo.model.PriceAlert;
import com.example.demo.repository.CryptoRepository;
import com.example.demo.repository.PriceAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PriceAlertServiceTest {

    @Mock
    private PriceAlertRepository priceAlertRepository;

    @Mock
    private CryptoRepository cryptoRepository;

    @InjectMocks
    private PriceAlertService priceAlertService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(priceAlertRepository.save(any(PriceAlert.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cryptoRepository.findBySymbol(anyString())).thenReturn(Optional.empty());
    }

    private CryptoAsset asset(String symbol, String name) {
        CryptoAsset a = new CryptoAsset();
        a.setSymbol(symbol);
        a.setName(name);
        a.setPrice(100.0);
        return a;
    }

    @Test
    void validCreationIsNormalized() {
        when(cryptoRepository.findBySymbol("bitcoin")).thenReturn(Optional.of(asset("bitcoin", "Bitcoin")));

        PriceAlert input = new PriceAlert(" ", "BITCOIN", 50000.0, "ABOVE");
        input.setId(99L);
        input.setActive(false);
        input.setTriggeredDate(LocalDateTime.now().minusDays(1));
        input.setCreatedDate(LocalDateTime.now().minusYears(1));

        PriceAlert result = priceAlertService.createPriceAlert(input);

        assertNull(result.getId());
        assertEquals("above", result.getAlertType());
        assertEquals("bitcoin", result.getAssetSymbol());
        assertEquals("Bitcoin", result.getAssetName());
        assertTrue(result.isActive());
        assertNull(result.getTriggeredDate());
        assertTrue(result.getCreatedDate().isAfter(LocalDateTime.now().minusMinutes(1)));
        verify(priceAlertRepository).save(input);
    }

    @Test
    void lowercaseEtfSymbolResolvesToUppercase() {
        when(cryptoRepository.findBySymbol("SPY")).thenReturn(Optional.of(asset("SPY", "S&P 500 ETF")));

        PriceAlert result = priceAlertService.createPriceAlert(new PriceAlert("Mi SPY", "spy", 400.0, "below"));

        assertEquals("SPY", result.getAssetSymbol());
        assertEquals("Mi SPY", result.getAssetName());
        assertEquals("below", result.getAlertType());
    }

    @Test
    void invalidTypeIsRejected() {
        when(cryptoRepository.findBySymbol("bitcoin")).thenReturn(Optional.of(asset("bitcoin", "Bitcoin")));

        assertThrows(IllegalArgumentException.class,
                () -> priceAlertService.createPriceAlert(new PriceAlert("Bitcoin", "bitcoin", 50000.0, "sideways")));
        assertThrows(IllegalArgumentException.class,
                () -> priceAlertService.createPriceAlert(new PriceAlert("Bitcoin", "bitcoin", 50000.0, null)));
        verify(priceAlertRepository, never()).save(any());
    }

    @Test
    void nonPositivePriceIsRejected() {
        when(cryptoRepository.findBySymbol("bitcoin")).thenReturn(Optional.of(asset("bitcoin", "Bitcoin")));

        assertThrows(IllegalArgumentException.class,
                () -> priceAlertService.createPriceAlert(new PriceAlert("Bitcoin", "bitcoin", 0.0, "above")));
        assertThrows(IllegalArgumentException.class,
                () -> priceAlertService.createPriceAlert(new PriceAlert("Bitcoin", "bitcoin", -5.0, "above")));
        verify(priceAlertRepository, never()).save(any());
    }

    @Test
    void blankSymbolIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> priceAlertService.createPriceAlert(new PriceAlert("X", "  ", 10.0, "above")));
        verify(priceAlertRepository, never()).save(any());
    }

    @Test
    void unknownAssetIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> priceAlertService.createPriceAlert(new PriceAlert("Foo", "foocoin", 10.0, "above")));
        assertTrue(ex.getMessage().contains("foocoin"));
        verify(priceAlertRepository, never()).save(any());
    }

    @Test
    void updateStatusChangesActiveFlag() {
        PriceAlert existing = new PriceAlert("Bitcoin", "bitcoin", 50000.0, "above");
        existing.setId(1L);
        when(priceAlertRepository.findById(1L)).thenReturn(Optional.of(existing));

        PriceAlert result = priceAlertService.updateAlertStatus(1L, false);

        assertNotNull(result);
        assertFalse(result.isActive());
        verify(priceAlertRepository).save(existing);
    }

    @Test
    void updateStatusOfMissingAlertReturnsNull() {
        when(priceAlertRepository.findById(42L)).thenReturn(Optional.empty());

        assertNull(priceAlertService.updateAlertStatus(42L, false));
        verify(priceAlertRepository, never()).save(any());
    }

    @Test
    void deleteMissingAlertReturnsFalse() {
        when(priceAlertRepository.existsById(7L)).thenReturn(false);

        assertFalse(priceAlertService.deleteAlert(7L));
        verify(priceAlertRepository, never()).deleteById(any());
    }
}
