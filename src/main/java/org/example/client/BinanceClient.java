package org.example.client;

import org.example.dto.BinanceBookTickerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class BinanceClient {

    private static final Logger log = LoggerFactory.getLogger(BinanceClient.class);
    private static final Set<String> SUPPORTED_SYMBOLS = Set.of("ETHUSDT", "BTCUSDT");

    private final RestTemplate restTemplate;

    @Value("${exchange.binance.url}")
    private String url;

    public BinanceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<BinanceBookTickerDto> fetchSupportedTickers() {
        try {
            BinanceBookTickerDto[] tickers = restTemplate.getForObject(url, BinanceBookTickerDto[].class);
            if (tickers == null) return Collections.emptyList();
            return Arrays.stream(tickers)
                    .filter(t -> SUPPORTED_SYMBOLS.contains(t.getSymbol()))
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch Binance tickers: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
