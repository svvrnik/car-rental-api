package com.example.car_rental_api;

import com.example.car_rental_api.exception.InsufficientFundsException;
import com.example.car_rental_api.payment.PaymentService;
import com.example.car_rental_api.transaction.TransactionService;
import com.example.car_rental_api.transaction.TransactionType;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionService transactionService;
    @InjectMocks
    private PaymentService paymentService;

    @Test
    void transferFundsForRentalShouldThrowInsufficientFundsException(){
        User from = new User();
        User to = new User();
        from.setAccountBalance(BigDecimal.valueOf(50));
        to.setAccountBalance(BigDecimal.valueOf(100));

        Assertions.assertThrows(InsufficientFundsException.class, () -> {
            paymentService.transferFundsForRental(from, to, BigDecimal.valueOf(100));
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(User.class), Mockito.any(User.class), Mockito.any(BigDecimal.class), Mockito.any(TransactionType.class));
    }

    @Test
    void transferFundsForRentalShouldSuccesfullyTransferAndSaveTransaction(){
        User from = new User();
        User to = new User();
        from.setAccountBalance(BigDecimal.valueOf(500));
        to.setAccountBalance(BigDecimal.valueOf(100));

        paymentService.transferFundsForRental(from, to, BigDecimal.valueOf(300));

        Assertions.assertEquals(BigDecimal.valueOf(200), from.getAccountBalance());
        Assertions.assertEquals(BigDecimal.valueOf(400), to.getAccountBalance());

        Mockito.verify(userRepository, Mockito.times(1)).save(from);
        Mockito.verify(userRepository, Mockito.times(1)).save(to);
        Mockito.verify(transactionService, Mockito.times(1)).createTransaction(from, to, BigDecimal.valueOf(300), TransactionType.RENTAL_PAYMENT);
    }

    @Test
    void addFundsToUserAccountShouldSuccesfullyAddFunds(){
        User user = new User();
        user.setAccountBalance(BigDecimal.valueOf(50));

        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = paymentService.addFundsToUserAccount(user, BigDecimal.valueOf(50));

        Assertions.assertNotNull(savedUser);
        Assertions.assertEquals(BigDecimal.valueOf(100), savedUser.getAccountBalance());
        Assertions.assertEquals(BigDecimal.valueOf(100), user.getAccountBalance());

        Mockito.verify(userRepository, Mockito.times(1)).save(user);
        Mockito.verify(transactionService, Mockito.times(1)).createTransaction(null, user, BigDecimal.valueOf(50), TransactionType.DEPOSIT);
    }

    @Test
    void withdrawFundsFromUserAccountShouldThrowInsufficientFundsException() {
        User user = new User();
        user.setAccountBalance(BigDecimal.valueOf(50));

        Assertions.assertThrows(InsufficientFundsException.class, () -> {
            paymentService.withdrawFundsFromUserAccount(user, BigDecimal.valueOf(100));
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(transactionService, Mockito.never()).createTransaction(Mockito.any(User.class), Mockito.any(), Mockito.any(BigDecimal.class), Mockito.any(TransactionType.class));
    }

    @Test
    void withdrawFundsFromUserAccountShouldSuccessfullyWithdrawAndSaveTransaction() {
        User user = new User();
        user.setAccountBalance(BigDecimal.valueOf(200));

        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = paymentService.withdrawFundsFromUserAccount(user, BigDecimal.valueOf(50));

        Assertions.assertNotNull(savedUser);
        Assertions.assertEquals(BigDecimal.valueOf(150), savedUser.getAccountBalance());
        Assertions.assertEquals(BigDecimal.valueOf(150), user.getAccountBalance());

        Mockito.verify(userRepository, Mockito.times(1)).save(user);
        Mockito.verify(transactionService, Mockito.times(1)).createTransaction(user, null, BigDecimal.valueOf(50), TransactionType.PAYOUT);
    }
}
