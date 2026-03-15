package com.example.demo.repository;

import com.example.demo.model.AlertLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {
    // Esto nos servirá para traer las últimas 10 alertas
    List<AlertLog> findTop10ByOrderByTimestampDesc();
}