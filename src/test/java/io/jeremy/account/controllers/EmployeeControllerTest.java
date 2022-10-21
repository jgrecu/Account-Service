package io.jeremy.account.controllers;

import io.jeremy.account.service.PaymentService;
import io.jeremy.account.web.responses.UserPaymentsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {


    @MockBean
    PaymentService paymentService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "jeremy", authorities = "ROLE_USER")
    void name() throws Exception {
        given(paymentService.getUserPayments("jeremy")).willReturn(List.of(
                new UserPaymentsResponse("Jeremy", "Grecu", "January-2022",
                        "50 dolar(s)")));

        mockMvc.perform(get("/payment")).andExpect(status().isOk()).andExpect(content().json("""
                [{"name": "Jeremy", "last_name": "Grecu", "period":  "January-2022", "salary":  "50 dolar(s)"}]
                """));
    }
}