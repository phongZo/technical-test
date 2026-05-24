package org.example.service;

import org.example.client.BinanceClient;
import org.example.client.HuobiClient;
import org.example.dto.BinanceBookTickerDto;
import org.example.dto.HuobiTickerDto;
import org.example.entity.BestPrice;
import org.example.repository.BestPriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceAggregationServiceTest {

    @Mock
    private BinanceClient binanceClient;
    @Mock
    private HuobiClient huobiClient;
    @Mock
    private BestPriceRepository bestPriceRepository;

    @InjectMocks
    private PriceAggregationService priceAggregationService;

    @Test
    void aggregateAndStore_picksBestBidAndAsk() {
        // Binance: bid=3000, ask=3010 | Huobi: bid=3005, ask=3008
        // Expected best: bid=3005 (max), ask=3008 (min)
        when(binanceClient.fetchSupportedTickers()).thenReturn(List.of(
                buildBinanceTicker("ETHUSDT", "3000", "3010")
        ));
        when(huobiClient.fetchSupportedTickers()).thenReturn(List.of(
                buildHuobiTicker("ethusdt", "3005", "3008")
        ));
        when(bestPriceRepository.findBySymbol("ETHUSDT")).thenReturn(Optional.empty());
        when(bestPriceRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        priceAggregationService.aggregateAndStore();

        ArgumentCaptor<BestPrice> captor = ArgumentCaptor.forClass(BestPrice.class);
        verify(bestPriceRepository).save(captor.capture());
        BestPrice saved = captor.getValue();

        assertEquals(new BigDecimal("3005"), saved.getBidPrice());
        assertEquals(new BigDecimal("3008"), saved.getAskPrice());
    }

    @Test
    void aggregateAndStore_whenBinanceFails_usesHuobiOnly() {
        when(binanceClient.fetchSupportedTickers()).thenReturn(Collections.emptyList());
        when(huobiClient.fetchSupportedTickers()).thenReturn(List.of(
                buildHuobiTicker("ethusdt", "3005", "3008")
        ));
        when(bestPriceRepository.findBySymbol("ETHUSDT")).thenReturn(Optional.empty());
        when(bestPriceRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        priceAggregationService.aggregateAndStore();

        ArgumentCaptor<BestPrice> captor = ArgumentCaptor.forClass(BestPrice.class);
        verify(bestPriceRepository).save(captor.capture());
        assertEquals(new BigDecimal("3005"), captor.getValue().getBidPrice());
        assertEquals(new BigDecimal("3008"), captor.getValue().getAskPrice());
    }

    @Test
    void aggregateAndStore_whenBothExchangesFail_doesNotSave() {
        when(binanceClient.fetchSupportedTickers()).thenReturn(Collections.emptyList());
        when(huobiClient.fetchSupportedTickers()).thenReturn(Collections.emptyList());

        priceAggregationService.aggregateAndStore();

        verify(bestPriceRepository, never()).save(any());
    }

    @Test
    void aggregateAndStore_updatesExistingRecord() {
        BestPrice existing = new BestPrice("ETHUSDT", new BigDecimal("2900"), new BigDecimal("2910"), null);
        when(binanceClient.fetchSupportedTickers()).thenReturn(List.of(
                buildBinanceTicker("ETHUSDT", "3000", "3010")
        ));
        when(huobiClient.fetchSupportedTickers()).thenReturn(Collections.emptyList());
        when(bestPriceRepository.findBySymbol("ETHUSDT")).thenReturn(Optional.of(existing));
        when(bestPriceRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        priceAggregationService.aggregateAndStore();

        ArgumentCaptor<BestPrice> captor = ArgumentCaptor.forClass(BestPrice.class);
        verify(bestPriceRepository).save(captor.capture());
        // same object should be updated, not a new one
        assertEquals(existing, captor.getValue());
        assertEquals(new BigDecimal("3000"), captor.getValue().getBidPrice());
    }

    private BinanceBookTickerDto buildBinanceTicker(String symbol, String bid, String ask) {
        BinanceBookTickerDto dto = new BinanceBookTickerDto();
        dto.setSymbol(symbol);
        dto.setBidPrice(new BigDecimal(bid));
        dto.setAskPrice(new BigDecimal(ask));
        return dto;
    }

    private HuobiTickerDto buildHuobiTicker(String symbol, String bid, String ask) {
        HuobiTickerDto dto = new HuobiTickerDto();
        dto.setSymbol(symbol);
        dto.setBid(new BigDecimal(bid));
        dto.setAsk(new BigDecimal(ask));
        return dto;
    }
}
