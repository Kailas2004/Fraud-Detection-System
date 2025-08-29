package com.example.fraud_detection.controller;

import com.example.fraud_detection.dto.TransactionRequest;
import com.example.fraud_detection.model.Transaction;
import com.example.fraud_detection.model.FraudStatus;
import com.example.fraud_detection.repository.TransactionRepository;
import com.example.fraud_detection.service.FraudDetectionService;
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
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        logger.info("GET /api/transactions - Fetching all transactions");
        List<Transaction> transactions = transactionRepository.findAll();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        logger.info("GET /api/transactions/{} - Fetching transaction by id", id);
        Optional<Transaction> transaction = transactionRepository.findById(id);
        
        if (transaction.isPresent()) {
            return ResponseEntity.ok(transaction.get());
        } else {
            logger.warn("Transaction not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionRequest transactionRequest) {
        logger.info("POST /api/transactions - Creating and analyzing new transaction for user: {}", 
                   transactionRequest.getUserId());
        
        try {
            Transaction analyzedTransaction = fraudDetectionService.createAndAnalyzeTransaction(transactionRequest);
            logger.info("Successfully created and analyzed transaction with id: {} - Status: {}, Score: {}", 
                       analyzedTransaction.getId(), 
                       analyzedTransaction.getFraudStatus(), 
                       analyzedTransaction.getFraudScore());
            return ResponseEntity.status(HttpStatus.CREATED).body(analyzedTransaction);
        } catch (RuntimeException e) {
            logger.error("Error creating transaction: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<?> analyzeTransaction(@PathVariable Long id) {
        logger.info("POST /api/transactions/{}/analyze - Re-analyzing transaction", id);
        
        try {
            Optional<Transaction> existingTransaction = transactionRepository.findById(id);
            if (existingTransaction.isEmpty()) {
                logger.warn("Transaction not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            Transaction analyzedTransaction = fraudDetectionService.analyzeTransaction(existingTransaction.get());
            logger.info("Successfully re-analyzed transaction: {} - Status: {}, Score: {}", 
                       analyzedTransaction.getId(), 
                       analyzedTransaction.getFraudStatus(), 
                       analyzedTransaction.getFraudScore());
            return ResponseEntity.ok(analyzedTransaction);
        } catch (RuntimeException e) {
            logger.error("Error analyzing transaction: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionsByUser(@PathVariable Long userId) {
        logger.info("GET /api/transactions/user/{} - Fetching transactions by user", userId);
        // Note: This would require a service method to fetch user first, then transactions
        // For now, we'll return all transactions (this should be improved in a real application)
        List<Transaction> transactions = transactionRepository.findAll();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Transaction>> getTransactionsByStatus(@PathVariable FraudStatus status) {
        logger.info("GET /api/transactions/status/{} - Fetching transactions by fraud status", status);
        List<Transaction> transactions = transactionRepository.findByFraudStatus(status);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/fraudulent")
    public ResponseEntity<List<Transaction>> getFraudulentTransactions() {
        logger.info("GET /api/transactions/fraudulent - Fetching fraudulent transactions");
        List<Transaction> fraudulentTransactions = transactionRepository.findByFraudStatus(FraudStatus.FRAUDULENT);
        return ResponseEntity.ok(fraudulentTransactions);
    }

    @GetMapping("/suspicious")
    public ResponseEntity<List<Transaction>> getSuspiciousTransactions() {
        logger.info("GET /api/transactions/suspicious - Fetching suspicious transactions");
        List<Transaction> suspiciousTransactions = transactionRepository.findByFraudStatus(FraudStatus.SUSPICIOUS);
        return ResponseEntity.ok(suspiciousTransactions);
    }

    @GetMapping("/legitimate")
    public ResponseEntity<List<Transaction>> getLegitimateTransactions() {
        logger.info("GET /api/transactions/legitimate - Fetching legitimate transactions");
        List<Transaction> legitimateTransactions = transactionRepository.findByFraudStatus(FraudStatus.LEGITIMATE);
        return ResponseEntity.ok(legitimateTransactions);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTransactionStatus(@PathVariable Long id, @RequestBody FraudStatus status) {
        logger.info("PUT /api/transactions/{}/status - Updating transaction status to: {}", id, status);
        
        try {
            Optional<Transaction> transaction = transactionRepository.findById(id);
            if (transaction.isEmpty()) {
                logger.warn("Transaction not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            Transaction updatedTransaction = transaction.get();
            updatedTransaction.setFraudStatus(status);
            updatedTransaction = transactionRepository.save(updatedTransaction);
            
            logger.info("Successfully updated transaction status: {}", updatedTransaction.getId());
            return ResponseEntity.ok(updatedTransaction);
        } catch (RuntimeException e) {
            logger.error("Error updating transaction status: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}