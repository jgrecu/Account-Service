package io.jeremy.account.controllers;

import io.jeremy.account.respository.PaymentRepository;
import io.jeremy.account.respository.UserRepository;
import io.jeremy.account.security.RestAuthenticationEntryPoint;
import io.jeremy.account.service.JpaUserDetailsService;
import io.jeremy.account.service.LoggingService;
import io.jeremy.account.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {



    @Autowired
    private MockMvc mockMvc;

    @MockBean
    PaymentService paymentService;

    @MockBean
    PaymentRepository paymentRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    JpaUserDetailsService jpaUserDetailsService;

    @MockBean
    RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @MockBean
    LoggingService loggingService;

    @Test
    @WithMockUser(username = "jeremy", authorities = "ROLE_USER")
    void name() throws Exception {
        mockMvc.perform(get("/api/empl/payment")).andExpect(status().isOk());
    }
}