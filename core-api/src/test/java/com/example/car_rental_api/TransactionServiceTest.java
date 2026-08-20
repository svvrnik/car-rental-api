package com.example.car_rental_api;

import com.example.car_rental_api.transaction.Transaction;
import com.example.car_rental_api.transaction.TransactionRepository;
import com.example.car_rental_api.transaction.TransactionService;
import com.example.car_rental_api.transaction.TransactionType;
import com.example.car_rental_api.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransactionShouldSaveTransaction(){
        User mockUser = new User();

        transactionService.createTransaction(mockUser, null, BigDecimal.ONE, TransactionType.DEPOSIT, "Transaction reference");

        Mockito.verify(transactionRepository, Mockito.times(1)).save(Mockito.any(Transaction.class));
    }
}
