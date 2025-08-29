package com.example.fraud_detection.repository;

import com.example.fraud_detection.model.Transaction;
import com.example.fraud_detection.model.User;
import com.example.fraud_detection.model.FraudStatus;
import com.example.fraud_detection.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser(User user);
    
    List<Transaction> findByUserAndTransactionTimeBetween(User user, LocalDateTime start, LocalDateTime end);
    
    List<Transaction> findByFraudStatus(FraudStatus fraudStatus);
    
    List<Transaction> findByUserAndFraudStatus(User user, FraudStatus fraudStatus);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.transactionTime >= :time ORDER BY t.transactionTime DESC")
    List<Transaction> findRecentTransactionsByUser(@Param("user") User user, @Param("time") LocalDateTime time);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user = :user AND t.transactionTime BETWEEN :start AND :end")
    long countTransactionsByUserInTimeWindow(@Param("user") User user, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.transactionTime BETWEEN :start AND :end")
    BigDecimal sumAmountByUserInTimeWindow(@Param("user") User user, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    List<Transaction> findByAmountGreaterThan(BigDecimal amount);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.transactionTime >= :time AND t.fraudStatus = 'FRAUDULENT'")
    List<Transaction> findFraudulentTransactionsByUserSince(@Param("user") User user, @Param("time") LocalDateTime time);
    
    @Query("SELECT t FROM Transaction t WHERE t.transactionType = :type AND t.transactionTime BETWEEN :start AND :end")
    List<Transaction> findByTransactionTypeAndTimeBetween(@Param("type") TransactionType type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM Transaction t WHERE t.ipAddress = :ipAddress AND t.transactionTime >= :time")
    List<Transaction> findByIpAddressSince(@Param("ipAddress") String ipAddress, @Param("time") LocalDateTime time);
}