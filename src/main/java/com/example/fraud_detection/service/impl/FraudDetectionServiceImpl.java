package com.example.fraud_detection.service.impl;

import com.example.fraud_detection.dto.TransactionRequest;
import com.example.fraud_detection.model.*;
import com.example.fraud_detection.repository.FraudRuleRepository;
import com.example.fraud_detection.repository.TransactionRepository;
import com.example.fraud_detection.repository.UserRepository;
import com.example.fraud_detection.service.FraudDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionServiceImpl.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FraudRuleRepository fraudRuleRepository;

    @Value("${fraud.detection.max-amount-threshold:10000.00}")
    private BigDecimal maxAmountThreshold;

    @Value("${fraud.detection.velocity-check-window-minutes:60}")
    private int velocityCheckWindowMinutes;

    @Value("${fraud.detection.max-transactions-per-window:5}")
    private int maxTransactionsPerWindow;

    @Override
    public Transaction analyzeTransaction(Transaction transaction) {
        logger.info("Analyzing transaction: {}", transaction.getId());
        
        double fraudScore = calculateFraudScore(transaction);
        transaction.setFraudScore(fraudScore);
        
        List<String> reasons = new ArrayList<>();
        
        // Determine fraud status based on score and rules
        if (fraudScore >= 80.0) {
            transaction.setFraudStatus(FraudStatus.FRAUDULENT);
            reasons.add("High fraud score: " + fraudScore);
        } else if (fraudScore >= 50.0) {
            transaction.setFraudStatus(FraudStatus.SUSPICIOUS);
            reasons.add("Moderate fraud score: " + fraudScore);
        } else {
            transaction.setFraudStatus(FraudStatus.LEGITIMATE);
        }
        
        // Set fraud reason
        if (!reasons.isEmpty()) {
            transaction.setFraudReason(String.join("; ", reasons));
        }
        
        logger.info("Transaction {} analyzed - Status: {}, Score: {}", 
                   transaction.getId(), transaction.getFraudStatus(), fraudScore);
        
        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction createAndAnalyzeTransaction(TransactionRequest request) {
        logger.info("Creating transaction for user: {}", request.getUserId());
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));
        
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setMerchantName(request.getMerchantName());
        transaction.setMerchantCategory(request.getMerchantCategory());
        transaction.setLocation(request.getLocation());
        transaction.setCardNumberMasked(request.getCardNumberMasked());
        transaction.setIpAddress(request.getIpAddress());
        transaction.setTransactionTime(LocalDateTime.now());
        
        // Save transaction first
        transaction = transactionRepository.save(transaction);
        
        // Then analyze for fraud
        return analyzeTransaction(transaction);
    }

    @Override
    public double calculateFraudScore(Transaction transaction) {
        double totalScore = 0.0;
        int ruleCount = 0;
        
        // Check amount threshold
        if (checkAmountThreshold(transaction)) {
            totalScore += 30.0;
            ruleCount++;
        }
        
        // Check velocity fraud
        if (checkVelocityFraud(transaction)) {
            totalScore += 40.0;
            ruleCount++;
        }
        
        // Check for unusual merchant category
        if (checkUnusualMerchant(transaction)) {
            totalScore += 25.0;
            ruleCount++;
        }
        
        // Check for unusual time patterns
        if (checkUnusualTime(transaction)) {
            totalScore += 20.0;
            ruleCount++;
        }
        
        // Check active fraud rules from database
        List<FraudRule> activeRules = fraudRuleRepository.findByIsActive(true);
        for (FraudRule rule : activeRules) {
            if (applyFraudRule(transaction, rule)) {
                totalScore += rule.getRiskScore();
                ruleCount++;
            }
        }
        
        // Normalize score to 0-100 range
        if (ruleCount == 0) {
            return 0.0;
        }
        
        return Math.min(100.0, totalScore);
    }

    @Override
    public boolean checkAmountThreshold(Transaction transaction) {
        boolean isHighAmount = transaction.getAmount().compareTo(maxAmountThreshold) > 0;
        
        if (isHighAmount) {
            logger.warn("High amount detected for transaction {}: {}", 
                       transaction.getId(), transaction.getAmount());
        }
        
        return isHighAmount;
    }

    @Override
    public boolean checkVelocityFraud(Transaction transaction) {
        LocalDateTime windowStart = transaction.getTransactionTime().minusMinutes(velocityCheckWindowMinutes);
        
        long recentTransactionCount = transactionRepository.countTransactionsByUserInTimeWindow(
                transaction.getUser(), windowStart, transaction.getTransactionTime());
        
        boolean isVelocityFraud = recentTransactionCount >= maxTransactionsPerWindow;
        
        if (isVelocityFraud) {
            logger.warn("Velocity fraud detected for user {}: {} transactions in {} minutes", 
                       transaction.getUser().getId(), recentTransactionCount, velocityCheckWindowMinutes);
        }
        
        return isVelocityFraud;
    }

    private boolean checkUnusualMerchant(Transaction transaction) {
        if (transaction.getMerchantCategory() == null) {
            return false;
        }
        
        // Check if merchant category is high-risk
        List<String> highRiskCategories = List.of("GAMBLING", "ADULT", "CRYPTOCURRENCY", "CASH_ADVANCE");
        return highRiskCategories.contains(transaction.getMerchantCategory().toUpperCase());
    }

    private boolean checkUnusualTime(Transaction transaction) {
        int hour = transaction.getTransactionTime().getHour();
        
        // Transactions between 2 AM and 5 AM are considered unusual
        return hour >= 2 && hour <= 5;
    }

    private boolean applyFraudRule(Transaction transaction, FraudRule rule) {
        switch (rule.getRuleType()) {
            case AMOUNT_THRESHOLD:
                if (rule.getThresholdAmount() != null) {
                    return transaction.getAmount().compareTo(rule.getThresholdAmount()) > 0;
                }
                break;
                
            case VELOCITY_CHECK:
                if (rule.getTimeWindowMinutes() != null && rule.getMaxOccurrences() != null) {
                    LocalDateTime windowStart = transaction.getTransactionTime()
                            .minusMinutes(rule.getTimeWindowMinutes());
                    long count = transactionRepository.countTransactionsByUserInTimeWindow(
                            transaction.getUser(), windowStart, transaction.getTransactionTime());
                    return count >= rule.getMaxOccurrences();
                }
                break;
                
            case MERCHANT_CATEGORY:
                if (rule.getMerchantCategory() != null && transaction.getMerchantCategory() != null) {
                    return transaction.getMerchantCategory().equalsIgnoreCase(rule.getMerchantCategory());
                }
                break;
                
            case LOCATION_BASED:
                if (rule.getLocationRestriction() != null && transaction.getLocation() != null) {
                    return transaction.getLocation().toLowerCase()
                            .contains(rule.getLocationRestriction().toLowerCase());
                }
                break;
                
            case TIME_BASED:
                int hour = transaction.getTransactionTime().getHour();
                return hour >= 2 && hour <= 5; // Unusual hours
                
            case IP_BASED:
                // Could implement IP-based checks here
                return false;
                
            default:
                return false;
        }
        
        return false;
    }
}