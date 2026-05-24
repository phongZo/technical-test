package org.example.service;

import org.example.dto.TradeRequest;
import org.example.entity.BestPrice;
import org.example.entity.Transaction;
import org.example.entity.Wallet;
import org.example.repository.BestPriceRepository;
import org.example.repository.TransactionRepository;
import org.example.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class TradingService {

    private static final Set<String> SUPPORTED_SYMBOLS = Set.of("ETHUSDT", "BTCUSDT");

    private final BestPriceRepository bestPriceRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TradingService(BestPriceRepository bestPriceRepository,
                          WalletRepository walletRepository,
                          TransactionRepository transactionRepository) {
        this.bestPriceRepository = bestPriceRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction executeTrade(TradeRequest request) {
        String symbol = request.getSymbol().toUpperCase();
        String type = request.getType().toUpperCase();

        if (!SUPPORTED_SYMBOLS.contains(symbol)) {
            throw new IllegalArgumentException("Unsupported symbol: " + symbol);
        }
        if (!type.equals("BUY") && !type.equals("SELL")) {
            throw new IllegalArgumentException("Invalid trade type. Must be BUY or SELL.");
        }
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        BestPrice bestPrice = bestPriceRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalStateException("No price available for " + symbol + ". Please try again later."));

        // Base currency: ETH or BTC (strip USDT suffix)
        String baseCurrency = symbol.replace("USDT", "");

        BigDecimal price;
        BigDecimal total;
        Wallet debitWallet;
        Wallet creditWallet;

        if (type.equals("BUY")) {
            // User pays USDT, receives base currency
            price = bestPrice.getAskPrice();
            total = price.multiply(request.getQuantity());

            debitWallet = getWallet(request.getUserId(), "USDT");
            creditWallet = getWallet(request.getUserId(), baseCurrency);

            if (debitWallet.getBalance().compareTo(total) < 0) {
                throw new IllegalArgumentException("Insufficient USDT balance.");
            }

            debitWallet.setBalance(debitWallet.getBalance().subtract(total));
            creditWallet.setBalance(creditWallet.getBalance().add(request.getQuantity()));

        } else {
            // User pays base currency, receives USDT
            price = bestPrice.getBidPrice();
            total = price.multiply(request.getQuantity());

            debitWallet = getWallet(request.getUserId(), baseCurrency);
            creditWallet = getWallet(request.getUserId(), "USDT");

            if (debitWallet.getBalance().compareTo(request.getQuantity()) < 0) {
                throw new IllegalArgumentException("Insufficient " + baseCurrency + " balance.");
            }

            debitWallet.setBalance(debitWallet.getBalance().subtract(request.getQuantity()));
            creditWallet.setBalance(creditWallet.getBalance().add(total));
        }

        walletRepository.save(debitWallet);
        walletRepository.save(creditWallet);

        Transaction transaction = new Transaction(
                request.getUserId(), symbol, type, price, request.getQuantity(), total, LocalDateTime.now()
        );
        return transactionRepository.save(transaction);
    }

    private Wallet getWallet(Long userId, String currency) {
        return walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new IllegalStateException("Wallet not found for currency: " + currency));
    }
}
