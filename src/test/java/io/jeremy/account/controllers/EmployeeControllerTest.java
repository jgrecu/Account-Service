package io.jeremy.account.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jeremy.account.dto.responses.UserPaymentsResponse;
import io.jeremy.account.service.JpaUserDetailsService;
import io.jeremy.account.service.LoggingService;
import io.jeremy.account.service.PaymentService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JpaUserDetailsService jpaUserDetailsService;

    @MockBean
    private LoggingService loggingService;

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

        verify(paymentService, times(1)).getUserPayments(anyString());
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