package io.jeremy.account.service;

import io.jeremy.account.dto.requests.UserRequest;
import io.jeremy.account.dto.responses.UserPaymentsResponse;
import io.jeremy.account.model.Payment;
import io.jeremy.account.model.User;
import io.jeremy.account.respository.PaymentRepository;
import io.jeremy.account.respository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private UserRepository userRepository;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        userRepository = mock(UserRepository.class);
        paymentService = new PaymentService(paymentRepository, userRepository);
    }

    @Test
    void getUserPaymentsForValidUser() {
        UserRequest userRequest = new UserRequest("Jeremy", "Grecu",
                "jeremy@example.com", "pass");
        User user = new User(userRequest, "pass");
        Payment payment = new Payment(1L, "jeremy@example.com",
                LocalDate.of(2023, 1, 1), 20_000L);

        when(userRepository.findByUsernameIgnoreCase("jeremy")).thenReturn(Optional.of(user));
        when(paymentRepository.findByEmployeeOrderByPeriodDesc(any())).thenReturn(List.of(payment));

        List<UserPaymentsResponse> userPayments = paymentService.getUserPayments("jeremy");
        assertThat(userPayments.get(0).getName()).isEqualTo("Jeremy");

        verify(userRepository, times(1)).findByUsernameIgnoreCase(any(String.class));
        verify(paymentRepository, times(1)).findByEmployeeOrderByPeriodDesc(any());
    }
}