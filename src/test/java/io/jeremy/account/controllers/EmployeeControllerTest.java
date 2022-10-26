package io.jeremy.account.controllers;

import io.jeremy.account.service.PaymentService;
import io.jeremy.account.dto.responses.UserPaymentsResponse;
import org.hamcrest.Matchers;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private PaymentService paymentService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(this.webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "john@acme.com")
    void shouldReturnAListOfPaymentsIfRoleIsUSER() throws Exception {
        when(paymentService.getUserPayments("john@acme.com"))
                .thenReturn(List.of(new UserPaymentsResponse("John", "Doe",
                        "January-2022", "1000 dollar(s) and 0 cent(s)")));

        mockMvc.perform(get("/api/empl/payment").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", Matchers.is(1)))
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[0].lastname").value("Doe"))
                .andExpect(jsonPath("$[0].salary").value("1000 dollar(s) and 0 cent(s)"))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void shouldReturnForbiddenIfRoleIsNotUSER() throws Exception {
        mockMvc.perform(get("/api/empl/payment").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    void shouldReturnUnauthorizedIfNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/empl/payment").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}