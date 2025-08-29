package com.example.fraud_detection.repository;

import com.example.fraud_detection.model.FraudRule;
import com.example.fraud_detection.model.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, Long> {

    List<FraudRule> findByIsActive(Boolean isActive);
    
    List<FraudRule> findByRuleType(RuleType ruleType);
    
    List<FraudRule> findByRuleTypeAndIsActive(RuleType ruleType, Boolean isActive);
    
    Optional<FraudRule> findByRuleName(String ruleName);
    
    @Query("SELECT fr FROM FraudRule fr WHERE fr.isActive = true ORDER BY fr.riskScore DESC")
    List<FraudRule> findActiveRulesOrderByRiskScore();
    
    @Query("SELECT fr FROM FraudRule fr WHERE fr.merchantCategory = :category AND fr.isActive = true")
    List<FraudRule> findByMerchantCategoryAndActive(@Param("category") String merchantCategory);
}