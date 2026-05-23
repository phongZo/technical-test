package org.example.repository;

import org.example.entity.BestPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BestPriceRepository extends JpaRepository<BestPrice, Long> {
}
