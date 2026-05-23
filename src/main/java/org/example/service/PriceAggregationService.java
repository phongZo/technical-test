package org.example.service;

import org.example.client.BinanceClient;
import org.example.client.HuobiClient;
import org.example.dto.BinanceBookTickerDto;
import org.example.dto.HuobiTickerDto;
import org.example.entity.BestPrice;
import org.example.repository.BestPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PriceAggregationService {

    private static final Logger log = LoggerFactory.getLogger(PriceAggregationService.class);

    private final BinanceClient binanceClient;
    private final HuobiClient huobiClient;
    private final BestPriceRepository bestPriceRepository;

    public PriceAggregationService(BinanceClient binanceClient, HuobiClient huobiClient,
                                   BestPriceRepository bestPriceRepository) {
        this.binanceClient = binanceClient;
        this.huobiClient = huobiClient;
        this.bestPriceRepository = bestPriceRepository;
    }

    public List<BestPrice> getLatestPrices() {
        return bestPriceRepository.findAll();
    }

    public void aggregateAndStore() {
        // symbol -> {bestBid, bestAsk}
        Map<String, BigDecimal[]> aggregated = new HashMap<>();

        List<BinanceBookTickerDto> binanceTickers = binanceClient.fetchSupportedTickers();
        for (BinanceBookTickerDto t : binanceTickers) {
            aggregated.merge(t.getSymbol(),
                    new BigDecimal[]{t.getBidPrice(), t.getAskPrice()},
                    (existing, incoming) -> new BigDecimal[]{
                            existing[0].max(incoming[0]),
                            existing[1].min(incoming[1])
                    });
        }

        // Huobi uses lowercase symbols —> we normalise to uppercase before merging
        List<HuobiTickerDto> huobiTickers = huobiClient.fetchSupportedTickers();
        for (HuobiTickerDto t : huobiTickers) {
            String symbol = t.getSymbol().toUpperCase();
            aggregated.merge(symbol,
                    new BigDecimal[]{t.getBid(), t.getAsk()},
                    (existing, incoming) -> new BigDecimal[]{
                            existing[0].max(incoming[0]),
                            existing[1].min(incoming[1])
                    });
        }

        if (aggregated.isEmpty()) {
            log.warn("No price data retrieved from any exchange.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        aggregated.forEach((symbol, prices) -> {
            BestPrice bestPrice = bestPriceRepository.findBySymbol(symbol)
                    .orElse(new BestPrice());
            bestPrice.setSymbol(symbol);
            bestPrice.setBidPrice(prices[0]);
            bestPrice.setAskPrice(prices[1]);
            bestPrice.setUpdatedAt(now);
            bestPriceRepository.save(bestPrice);
            log.info("Updated best price for {}: bid={}, ask={}", symbol, prices[0], prices[1]);
        });
    }
}
