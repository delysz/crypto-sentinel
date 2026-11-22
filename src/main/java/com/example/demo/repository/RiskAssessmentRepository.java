package com.example.demo.repository;

import com.example.demo.model.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    List<RiskAssessment> findByPortfolioNameOrderByAssessmentDateDesc(String portfolioName);
    List<RiskAssessment> findAllByOrderByAssessmentDateDesc();
}