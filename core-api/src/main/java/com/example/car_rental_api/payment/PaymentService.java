package com.example.car_rental_api.payment;

import com.example.car_rental_api.exception.InsufficientFundsException;
import com.example.car_rental_api.transaction.TransactionService;
import com.example.car_rental_api.transaction.TransactionType;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public PaymentService(UserRepository userRepository, TransactionService transactionService) {
        this.userRepository = userRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public void transferFundsForRental(User from, User to, BigDecimal amount){
        if(amount.compareTo(from.getAccountBalance()) > 0){
            throw new InsufficientFundsException("Insufficient funds");
        }

        from.setAccountBalance(from.getAccountBalance().subtract(amount));
        userRepository.save(from);

        to.setAccountBalance(to.getAccountBalance().add(amount));
        userRepository.save(to);

        transactionService.createTransaction(from, to, amount, TransactionType.RENTAL_PAYMENT);
    }

    @Transactional
    public User addFundsToUserAccount(User user, BigDecimal amount){
        user.setAccountBalance(user.getAccountBalance().add(amount));
        User savedUser = userRepository.save(user);

        transactionService.createTransaction(null, user, amount, TransactionType.DEPOSIT);

        return savedUser;
    }

    @Transactional
    public User withdrawFundsFromUserAccount(User user, BigDecimal amount){
        if(user.getAccountBalance().compareTo(amount)<0){
            throw new InsufficientFundsException("Insufficient funds");
        }

        user.setAccountBalance(user.getAccountBalance().subtract(amount));
        User savedUser = userRepository.save(user);

        transactionService.createTransaction(user, null, amount, TransactionType.PAYOUT);

        return savedUser;
    }
}
