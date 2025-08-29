package com.example.fraud_detection.service;

import com.example.fraud_detection.model.*;
import com.example.fraud_detection.repository.FraudRuleRepository;
import com.example.fraud_detection.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DataInitializationService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FraudRuleRepository fraudRuleRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Initializing sample data...");
        
        initializeUsers();
        initializeFraudRules();
        
        logger.info("Sample data initialization completed");
    }

    private void initializeUsers() {
        if (userRepository.count() == 0) {
            logger.info("Creating sample users...");
            
            User user1 = new User("john_doe", "john.doe@email.com", "John Doe");
            user1.setPhoneNumber("555-0101");
            userRepository.save(user1);
            
            User user2 = new User("jane_smith", "jane.smith@email.com", "Jane Smith");
            user2.setPhoneNumber("555-0102");
            userRepository.save(user2);
            
            User user3 = new User("bob_wilson", "bob.wilson@email.com", "Bob Wilson");
            user3.setPhoneNumber("555-0103");
            userRepository.save(user3);
            
            User user4 = new User("alice_brown", "alice.brown@email.com", "Alice Brown");
            user4.setPhoneNumber("555-0104");
            userRepository.save(user4);
            
            logger.info("Created {} sample users", userRepository.count());
        }
    }

    private void initializeFraudRules() {
        if (fraudRuleRepository.count() == 0) {
            logger.info("Creating default fraud rules...");
            
            // High Amount Threshold Rule
            FraudRule highAmountRule = new FraudRule("High Amount Transaction", RuleType.AMOUNT_THRESHOLD, 40.0);
            highAmountRule.setDescription("Flags transactions over $10,000 as high risk");
            highAmountRule.setThresholdAmount(new BigDecimal("10000.00"));
            fraudRuleRepository.save(highAmountRule);
            
            // Velocity Rule
            FraudRule velocityRule = new FraudRule("Transaction Velocity", RuleType.VELOCITY_CHECK, 50.0);
            velocityRule.setDescription("Flags users with more than 5 transactions in 60 minutes");
            velocityRule.setTimeWindowMinutes(60);
            velocityRule.setMaxOccurrences(5);
            fraudRuleRepository.save(velocityRule);
            
            // High Risk Merchant Category - Gambling
            FraudRule gamblingRule = new FraudRule("Gambling Transaction", RuleType.MERCHANT_CATEGORY, 30.0);
            gamblingRule.setDescription("Flags gambling-related transactions as moderate risk");
            gamblingRule.setMerchantCategory("GAMBLING");
            fraudRuleRepository.save(gamblingRule);
            
            // High Risk Merchant Category - Cryptocurrency
            FraudRule cryptoRule = new FraudRule("Cryptocurrency Transaction", RuleType.MERCHANT_CATEGORY, 35.0);
            cryptoRule.setDescription("Flags cryptocurrency transactions as moderate risk");
            cryptoRule.setMerchantCategory("CRYPTOCURRENCY");
            fraudRuleRepository.save(cryptoRule);
            
            // Cash Advance Rule
            FraudRule cashAdvanceRule = new FraudRule("Cash Advance", RuleType.MERCHANT_CATEGORY, 25.0);
            cashAdvanceRule.setDescription("Flags cash advance transactions as low-moderate risk");
            cashAdvanceRule.setMerchantCategory("CASH_ADVANCE");
            fraudRuleRepository.save(cashAdvanceRule);
            
            // Unusual Time Rule
            FraudRule unusualTimeRule = new FraudRule("Unusual Time Transaction", RuleType.TIME_BASED, 20.0);
            unusualTimeRule.setDescription("Flags transactions between 2AM and 5AM as unusual");
            fraudRuleRepository.save(unusualTimeRule);
            
            // Very High Amount Rule
            FraudRule veryHighAmountRule = new FraudRule("Very High Amount", RuleType.AMOUNT_THRESHOLD, 60.0);
            veryHighAmountRule.setDescription("Flags transactions over $50,000 as very high risk");
            veryHighAmountRule.setThresholdAmount(new BigDecimal("50000.00"));
            fraudRuleRepository.save(veryHighAmountRule);
            
            // Rapid Velocity Rule
            FraudRule rapidVelocityRule = new FraudRule("Rapid Transaction Velocity", RuleType.VELOCITY_CHECK, 70.0);
            rapidVelocityRule.setDescription("Flags users with more than 3 transactions in 10 minutes");
            rapidVelocityRule.setTimeWindowMinutes(10);
            rapidVelocityRule.setMaxOccurrences(3);
            fraudRuleRepository.save(rapidVelocityRule);
            
            logger.info("Created {} default fraud rules", fraudRuleRepository.count());
        }
    }
}