package io.jeremy.account.service;

import io.jeremy.account.dto.requests.UserRequest;
import io.jeremy.account.dto.responses.UserPaymentsResponse;
import io.jeremy.account.model.Payment;
import io.jeremy.account.model.User;
import io.jeremy.account.respository.PaymentRepository;
import io.jeremy.account.respository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void getUserPayments() {
        UserRequest userRequest = new UserRequest("Jeremy", "Grecu",
                "jeremy@example.com", "pass");
        User user = new User(userRequest, "pass");
        when(userRepository.findByUsernameIgnoreCase("jeremy")).thenReturn(Optional.of(user));
        when(paymentRepository.findByEmployeeOrderByPeriodDesc(any())).thenReturn(List.of(new Payment()));

        List<UserPaymentsResponse> userPayments = paymentService.getUserPayments("jeremy");
        assertThat(userPayments.get(1).getName()).isEqualTo("Jeremy");

    }
}