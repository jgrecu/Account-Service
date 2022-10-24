package io.jeremy.account.controllers;

import io.jeremy.account.service.PaymentService;
import io.jeremy.account.web.responses.UserPaymentsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PaymentService paymentService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser("john@acme.com")
    void whenGivenTheRightUser() throws Exception {
        when(paymentService.getUserPayments("john@acme.com"))
                .thenReturn(List.of(new UserPaymentsResponse("John", "Doe",
                        "January-2022", "1")));

        mockMvc.perform(get("/api/empl/payment").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void name() throws Exception {
        mockMvc.perform(get("/api/empl/payment").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void name_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/empl/payment").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}