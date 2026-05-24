package org.example.service;

import org.example.dto.TradeRequest;
import org.example.entity.BestPrice;
import org.example.entity.Transaction;
import org.example.entity.Wallet;
import org.example.repository.BestPriceRepository;
import org.example.repository.TransactionRepository;
import org.example.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingServiceTest {

    @Mock
    private BestPriceRepository bestPriceRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TradingService tradingService;

    private BestPrice ethBestPrice;
    private Wallet usdtWallet;
    private Wallet ethWallet;

    @BeforeEach
    void setUp() {
        ethBestPrice = new BestPrice("ETHUSDT", new BigDecimal("2990"), new BigDecimal("3000"), LocalDateTime.now());
        usdtWallet = new Wallet(1L, "USDT", new BigDecimal("50000"));
        ethWallet = new Wallet(1L, "ETH", new BigDecimal("5"));
    }

    @Test
    void executeTrade_buy_deductsUsdtAndCreditsEth() {
        TradeRequest request = buildRequest("ETHUSDT", "BUY", "2");

        when(bestPriceRepository.findBySymbol("ETHUSDT")).thenReturn(Optional.of(ethBestPrice));
        when(walletRepository.findByUserIdAndCurrency(1L, "USDT")).thenReturn(Optional.of(usdtWallet));
        when(walletRepository.findByUserIdAndCurrency(1L, "ETH")).thenReturn(Optional.of(ethWallet));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Transaction result = tradingService.executeTrade(request);

        // USDT deducted: 50000 - (3000 * 2) = 44000
        assertEquals(new BigDecimal("44000"), usdtWallet.getBalance());
        // ETH credited: 5 + 2 = 7
        assertEquals(new BigDecimal("7"), ethWallet.getBalance());
        assertEquals("BUY", result.getType());
        assertEquals(new BigDecimal("3000"), result.getPrice());
        assertEquals(new BigDecimal("6000"), result.getTotal());
    }

    @Test
    void executeTrade_sell_deductsEthAndCreditsUsdt() {
        TradeRequest request = buildRequest("ETHUSDT", "SELL", "2");

        when(bestPriceRepository.findBySymbol("ETHUSDT")).thenReturn(Optional.of(ethBestPrice));
        when(walletRepository.findByUserIdAndCurrency(1L, "ETH")).thenReturn(Optional.of(ethWallet));
        when(walletRepository.findByUserIdAndCurrency(1L, "USDT")).thenReturn(Optional.of(usdtWallet));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Transaction result = tradingService.executeTrade(request);

        // ETH deducted: 5 - 2 = 3
        assertEquals(new BigDecimal("3"), ethWallet.getBalance());
        // USDT credited: 50000 + (2990 * 2) = 55980
        assertEquals(new BigDecimal("55980"), usdtWallet.getBalance());
        assertEquals("SELL", result.getType());
        assertEquals(new BigDecimal("2990"), result.getPrice());
        assertEquals(new BigDecimal("5980"), result.getTotal());
    }

    @Test
    void executeTrade_buy_insufficientUsdt_throwsException() {
        usdtWallet = new Wallet(1L, "USDT", new BigDecimal("100")); // only 100 USDT
        TradeRequest request = buildRequest("ETHUSDT", "BUY", "1"); // needs 3000 USDT

        when(bestPriceRepository.findBySymbol("ETHUSDT")).thenReturn(Optional.of(ethBestPrice));
        when(walletRepository.findByUserIdAndCurrency(1L, "USDT")).thenReturn(Optional.of(usdtWallet));
        when(walletRepository.findByUserIdAndCurrency(1L, "ETH")).thenReturn(Optional.of(ethWallet));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.executeTrade(request));
        assertTrue(ex.getMessage().contains("Insufficient USDT balance"));
    }

    @Test
    void executeTrade_sell_insufficientCrypto_throwsException() {
        ethWallet = new Wallet(1L, "ETH", new BigDecimal("0.5")); // only 0.5 ETH
        TradeRequest request = buildRequest("ETHUSDT", "SELL", "2"); // selling 2 ETH

        when(bestPriceRepository.findBySymbol("ETHUSDT")).thenReturn(Optional.of(ethBestPrice));
        when(walletRepository.findByUserIdAndCurrency(1L, "ETH")).thenReturn(Optional.of(ethWallet));
        when(walletRepository.findByUserIdAndCurrency(1L, "USDT")).thenReturn(Optional.of(usdtWallet));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.executeTrade(request));
        assertTrue(ex.getMessage().contains("Insufficient ETH balance"));
    }

    @Test
    void executeTrade_unsupportedSymbol_throwsException() {
        TradeRequest request = buildRequest("DOGEUSDT", "BUY", "1");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.executeTrade(request));
        assertTrue(ex.getMessage().contains("Unsupported symbol"));
    }

    @Test
    void executeTrade_invalidType_throwsException() {
        TradeRequest request = buildRequest("ETHUSDT", "HOLD", "1");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.executeTrade(request));
        assertTrue(ex.getMessage().contains("Invalid trade type"));
    }

    @Test
    void executeTrade_zeroQuantity_throwsException() {
        TradeRequest request = buildRequest("ETHUSDT", "BUY", "0");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.executeTrade(request));
        assertTrue(ex.getMessage().contains("Quantity must be greater than zero"));
    }

    @Test
    void executeTrade_noPriceAvailable_throwsException() {
        TradeRequest request = buildRequest("ETHUSDT", "BUY", "1");
        when(bestPriceRepository.findBySymbol("ETHUSDT")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> tradingService.executeTrade(request));
    }

    private TradeRequest buildRequest(String symbol, String type, String quantity) {
        TradeRequest request = new TradeRequest();
        request.setUserId(1L);
        request.setSymbol(symbol);
        request.setType(type);
        request.setQuantity(new BigDecimal(quantity));
        return request;
    }
}
