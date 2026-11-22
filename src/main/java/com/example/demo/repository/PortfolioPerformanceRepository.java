package com.example.demo.repository;

import com.example.demo.model.PortfolioPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PortfolioPerformanceRepository extends JpaRepository<PortfolioPerformance, Long> {
    List<PortfolioPerformance> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);
    List<PortfolioPerformance> findByDateAfterOrderByDateAsc(LocalDate date);
    PortfolioPerformance findFirstByOrderByDateDesc();

    /** Snapshots de un día concreto (debería haber como mucho uno; puede haber duplicados antiguos). */
    List<PortfolioPerformance> findByDate(LocalDate date);

    /** Último snapshot anterior a la fecha dada (para calcular la variación diaria). */
    PortfolioPerformance findFirstByDateBeforeOrderByDateDesc(LocalDate date);
    
    @Query("SELECT p FROM PortfolioPerformance p WHERE p.date >= :startDate AND p.date <= :endDate ORDER BY p.date ASC")
    List<PortfolioPerformance> findByDateRange(@Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
}
