package org.example.scheduler;

import org.example.service.PriceAggregationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceScheduler {

    private final PriceAggregationService priceAggregationService;

    public PriceScheduler(PriceAggregationService priceAggregationService) {
        this.priceAggregationService = priceAggregationService;
    }

    @Scheduled(fixedRate = 10000)
    public void schedulePriceAggregation() {
        priceAggregationService.aggregateAndStore();
    }
}
