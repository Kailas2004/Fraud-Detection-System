package com.example.fraud_detection.controller;

import com.example.fraud_detection.model.FraudRule;
import com.example.fraud_detection.model.RuleType;
import com.example.fraud_detection.repository.FraudRuleRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/fraud-rules")
@CrossOrigin(origins = "*")
public class FraudRuleController {

    private static final Logger logger = LoggerFactory.getLogger(FraudRuleController.class);

    @Autowired
    private FraudRuleRepository fraudRuleRepository;

    @GetMapping
    public ResponseEntity<List<FraudRule>> getAllFraudRules() {
        logger.info("GET /api/fraud-rules - Fetching all fraud rules");
        List<FraudRule> fraudRules = fraudRuleRepository.findAll();
        return ResponseEntity.ok(fraudRules);
    }

    @GetMapping("/active")
    public ResponseEntity<List<FraudRule>> getActiveFraudRules() {
        logger.info("GET /api/fraud-rules/active - Fetching active fraud rules");
        List<FraudRule> activeFraudRules = fraudRuleRepository.findByIsActive(true);
        return ResponseEntity.ok(activeFraudRules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FraudRule> getFraudRuleById(@PathVariable Long id) {
        logger.info("GET /api/fraud-rules/{} - Fetching fraud rule by id", id);
        Optional<FraudRule> fraudRule = fraudRuleRepository.findById(id);
        
        if (fraudRule.isPresent()) {
            return ResponseEntity.ok(fraudRule.get());
        } else {
            logger.warn("Fraud rule not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/type/{ruleType}")
    public ResponseEntity<List<FraudRule>> getFraudRulesByType(@PathVariable RuleType ruleType) {
        logger.info("GET /api/fraud-rules/type/{} - Fetching fraud rules by type", ruleType);
        List<FraudRule> fraudRules = fraudRuleRepository.findByRuleType(ruleType);
        return ResponseEntity.ok(fraudRules);
    }

    @PostMapping
    public ResponseEntity<?> createFraudRule(@Valid @RequestBody FraudRule fraudRule) {
        logger.info("POST /api/fraud-rules - Creating new fraud rule: {}", fraudRule.getRuleName());
        
        try {
            FraudRule savedFraudRule = fraudRuleRepository.save(fraudRule);
            logger.info("Successfully created fraud rule with id: {}", savedFraudRule.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFraudRule);
        } catch (Exception e) {
            logger.error("Error creating fraud rule: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFraudRule(@PathVariable Long id, @Valid @RequestBody FraudRule fraudRule) {
        logger.info("PUT /api/fraud-rules/{} - Updating fraud rule", id);
        
        try {
            Optional<FraudRule> existingRule = fraudRuleRepository.findById(id);
            if (existingRule.isEmpty()) {
                logger.warn("Fraud rule not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            fraudRule.setId(id);
            FraudRule updatedFraudRule = fraudRuleRepository.save(fraudRule);
            logger.info("Successfully updated fraud rule: {}", updatedFraudRule.getId());
            return ResponseEntity.ok(updatedFraudRule);
        } catch (Exception e) {
            logger.error("Error updating fraud rule: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFraudRule(@PathVariable Long id) {
        logger.info("DELETE /api/fraud-rules/{} - Deleting fraud rule", id);
        
        try {
            Optional<FraudRule> fraudRule = fraudRuleRepository.findById(id);
            if (fraudRule.isEmpty()) {
                logger.warn("Fraud rule not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            fraudRuleRepository.deleteById(id);
            logger.info("Successfully deleted fraud rule: {}", id);
            return ResponseEntity.ok().body("Fraud rule deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting fraud rule: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleFraudRuleStatus(@PathVariable Long id) {
        logger.info("PUT /api/fraud-rules/{}/toggle - Toggling fraud rule status", id);
        
        try {
            Optional<FraudRule> fraudRule = fraudRuleRepository.findById(id);
            if (fraudRule.isEmpty()) {
                logger.warn("Fraud rule not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            FraudRule rule = fraudRule.get();
            rule.setIsActive(!rule.getIsActive());
            FraudRule updatedRule = fraudRuleRepository.save(rule);
            
            logger.info("Successfully toggled fraud rule status: {} - Now: {}", 
                       updatedRule.getId(), updatedRule.getIsActive());
            return ResponseEntity.ok(updatedRule);
        } catch (Exception e) {
            logger.error("Error toggling fraud rule status: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}