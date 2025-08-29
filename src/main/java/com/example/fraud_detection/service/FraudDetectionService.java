package com.example.fraud_detection.service;

import com.example.fraud_detection.model.Transaction;
import com.example.fraud_detection.dto.TransactionRequest;

public interface FraudDetectionService {
    
    /**
     * Analyzes a transaction for potential fraud
     * @param transaction The transaction to analyze
     * @return The transaction with fraud status and score updated
     */
    Transaction analyzeTransaction(Transaction transaction);
    
    /**
     * Creates and analyzes a transaction from a request
     * @param transactionRequest The transaction request
     * @return The analyzed transaction
     */
    Transaction createAndAnalyzeTransaction(TransactionRequest transactionRequest);
    
    /**
     * Calculates the fraud risk score for a transaction
     * @param transaction The transaction to score
     * @return The fraud risk score (0.0 - 100.0)
     */
    double calculateFraudScore(Transaction transaction);
    
    /**
     * Checks if a transaction exceeds the amount threshold
     * @param transaction The transaction to check
     * @return true if amount is suspicious
     */
    boolean checkAmountThreshold(Transaction transaction);
    
    /**
     * Checks for velocity fraud (too many transactions in a short time)
     * @param transaction The transaction to check
     * @return true if velocity is suspicious
     */
    boolean checkVelocityFraud(Transaction transaction);
}