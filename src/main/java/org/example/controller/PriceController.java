package org.example.controller;

import org.example.entity.BestPrice;
import org.example.service.PriceAggregationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
public class PriceController {

    private final PriceAggregationService priceAggregationService;

    public PriceController(PriceAggregationService priceAggregationService) {
        this.priceAggregationService = priceAggregationService;
    }

    @GetMapping
    public ResponseEntity<List<BestPrice>> getLatestPrices() {
        return ResponseEntity.ok(priceAggregationService.getLatestPrices());
    }
}
