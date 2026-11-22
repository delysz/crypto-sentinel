package com.example.demo.repository;

import com.example.demo.model.CryptoAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.Optional;


    Optional<CryptoAsset> findBySymbol(String symbol);
public interface CryptoRepository extends JpaRepository<CryptoAsset, Long> {
    Optional<CryptoAsset> findBySymbol(String symbol);
}