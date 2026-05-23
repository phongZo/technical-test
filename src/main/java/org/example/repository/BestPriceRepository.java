package org.example.repository;

import org.example.entity.BestPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BestPriceRepository extends JpaRepository<BestPrice, Long> {
    Optional<BestPrice> findBySymbol(String symbol);
}
