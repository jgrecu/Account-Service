package io.jeremy.account.controllers;

import io.jeremy.account.service.JpaUserDetailsService;
import io.jeremy.account.service.LoggingService;
import io.jeremy.account.service.UserService;
import io.jeremy.account.dto.requests.UserRequest;
import io.jeremy.account.dto.responses.ChangePassResponse;
import io.jeremy.account.dto.responses.UserResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JpaUserDetailsService jpaUserDetailsService;

    @MockBean
    private LoggingService loggingService;

//    @BeforeEach
//    public void setup() {
//        mockMvc = MockMvcBuilders
//                .webAppContextSetup(this.webApplicationContext)
//                .apply(springSecurity())
//                .build();
//    }

    @Test
    void shouldCreateANewUSer() throws Exception {
        String userRequest = """
                {
                    "name": "John",
                    "lastname": "Doe",
                    "email": "johndoe@acme.com",
                    "password": "bZPGqH7fTJWW"
                }""";

        UserResponse userResponse = new UserResponse(1L, "John", "Doe",
                "johndoe@acme.com", List.of("ROLE_USER"));
        when(userService.addUser(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/signup")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(userRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"))
                .andExpect(jsonPath("$.email").value("johndoe@acme.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "johndoe@acme.com")
    void shouldChangePasswordOfExistingUser() throws Exception {
        ChangePassResponse passResponse = new ChangePassResponse("johndoe@acme.com",
                "The password has been updated successfully");
        when(userService.updatePassword(anyString(), anyString())).thenReturn(passResponse);

        mockMvc.perform(post("/api/auth/changepass")
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"new_password\": \"bZPGqH7fTJW12\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("johndoe@acme.com"))
                .andExpect(jsonPath("$.status").isNotEmpty())
                .andDo(print());
    }
}