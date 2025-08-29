package com.example.fraud_detection.controller;

import com.example.fraud_detection.model.Transaction;
import com.example.fraud_detection.model.User;
import com.example.fraud_detection.model.FraudRule;
import com.example.fraud_detection.model.FraudStatus;
import com.example.fraud_detection.repository.TransactionRepository;
import com.example.fraud_detection.repository.UserRepository;
import com.example.fraud_detection.repository.FraudRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class WebController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FraudRuleRepository fraudRuleRepository;

    @GetMapping("/")
    public String home(Model model) {
        long userCount = userRepository.count();
        long transactionCount = transactionRepository.count();
        long fraudulentCount = transactionRepository.findByFraudStatus(FraudStatus.FRAUDULENT).size();
        long suspiciousCount = transactionRepository.findByFraudStatus(FraudStatus.SUSPICIOUS).size();
        long ruleCount = fraudRuleRepository.count();

        model.addAttribute("userCount", userCount);
        model.addAttribute("transactionCount", transactionCount);
        model.addAttribute("fraudulentCount", fraudulentCount);
        model.addAttribute("suspiciousCount", suspiciousCount);
        model.addAttribute("ruleCount", ruleCount);

        return "index";
    }

    @GetMapping("/users")
    public String users(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            List<Transaction> transactions = transactionRepository.findByUser(user.get());
            model.addAttribute("user", user.get());
            model.addAttribute("transactions", transactions);
            return "user-detail";
        }
        return "redirect:/users";
    }

    @GetMapping("/transactions")
    public String transactions(Model model) {
        List<Transaction> transactions = transactionRepository.findAll();
        model.addAttribute("transactions", transactions);
        return "transactions";
    }

    @GetMapping("/transactions/{id}")
    public String transactionDetail(@PathVariable Long id, Model model) {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        if (transaction.isPresent()) {
            model.addAttribute("transaction", transaction.get());
            return "transaction-detail";
        }
        return "redirect:/transactions";
    }

    @GetMapping("/fraud-rules")
    public String fraudRules(Model model) {
        List<FraudRule> fraudRules = fraudRuleRepository.findAll();
        model.addAttribute("fraudRules", fraudRules);
        return "fraud-rules";
    }

    @GetMapping("/create-user")
    public String createUser(Model model) {
        model.addAttribute("user", new User());
        return "create-user";
    }

    @GetMapping("/create-transaction")
    public String createTransaction(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "create-transaction";
    }

    @GetMapping("/fraud-dashboard")
    public String fraudDashboard(Model model) {
        List<Transaction> fraudulentTransactions = transactionRepository.findByFraudStatus(FraudStatus.FRAUDULENT);
        List<Transaction> suspiciousTransactions = transactionRepository.findByFraudStatus(FraudStatus.SUSPICIOUS);
        
        model.addAttribute("fraudulentTransactions", fraudulentTransactions);
        model.addAttribute("suspiciousTransactions", suspiciousTransactions);
        
        return "fraud-dashboard";
    }
}