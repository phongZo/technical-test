package org.example.client;

import org.example.dto.HuobiResponseDto;
import org.example.dto.HuobiTickerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class HuobiClient {

    private static final Logger log = LoggerFactory.getLogger(HuobiClient.class);
    // Huobi symbols are lowercase
    private static final Set<String> SUPPORTED_SYMBOLS = Set.of("ethusdt", "btcusdt");

    private final RestTemplate restTemplate;

    @Value("${exchange.huobi.url}")
    private String url;

    public HuobiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<HuobiTickerDto> fetchSupportedTickers() {
        try {
            HuobiResponseDto response = restTemplate.getForObject(url, HuobiResponseDto.class);
            if (response == null || response.getData() == null) return Collections.emptyList();
            return response.getData().stream()
                    .filter(t -> SUPPORTED_SYMBOLS.contains(t.getSymbol()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch Huobi tickers: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
