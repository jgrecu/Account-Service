package account.controllers;

import account.web.requests.ChangePasswordRequest;
import account.web.requests.UserRequest;
import account.web.responses.ChangePassResponse;
import account.web.responses.UserResponse;
import account.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public UserResponse registerUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.addUser(userRequest);
        System.out.println("REPLY: " + userResponse);
        return userResponse;
    }

    @PostMapping("/changepass")
    public ChangePassResponse updatePassword(Principal principal, @Valid @RequestBody ChangePasswordRequest passRequest) {
        return userService.updatePassword(principal.getName(), passRequest.getNewPassword());
    }
}
