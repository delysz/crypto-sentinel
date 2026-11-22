package com.example.demo.repository;

import com.example.demo.model.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findByActiveTrue();
    List<PriceAlert> findByAssetSymbolAndActiveTrue(String assetSymbol);
    List<PriceAlert> findByAssetSymbolIgnoreCaseAndActiveTrue(String assetSymbol);
    List<PriceAlert> findAllByOrderByCreatedDateDesc();
}
