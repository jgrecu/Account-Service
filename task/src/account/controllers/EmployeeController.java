package account.controllers;

import account.responses.UserResponse;
import account.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/empl")
public class EmployeeController {

    private final UserService userService;

    public EmployeeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/payment")
    public UserResponse getEmployee(Principal principal) {
        String name = principal.getName();

        return userService.getUser(name);
    }
}
