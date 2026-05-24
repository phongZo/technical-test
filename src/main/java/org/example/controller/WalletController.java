package org.example.controller;

import org.example.entity.Wallet;
import org.example.repository.WalletRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletRepository walletRepository;

    public WalletController(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Wallet>> getWalletBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(walletRepository.findByUserId(userId));
    }
}
