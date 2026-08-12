package com.example.car_rental_api.transaction;

import com.example.car_rental_api.user.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void createTransaction(User sender, User receiver, BigDecimal amount,TransactionType transactionType){
        Transaction transaction = new Transaction();
        transaction.setReceiver(receiver);
        transaction.setSender(sender);
        transaction.setAmount(amount);
        transaction.setType(transactionType);

        transactionRepository.save(transaction);
    }
}
