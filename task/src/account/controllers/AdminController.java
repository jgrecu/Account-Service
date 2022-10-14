package account.controllers;

import account.service.UserService;
import account.web.responses.DeleteUserResponse;
import account.web.responses.UserResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/user/{userEmail}")
    public DeleteUserResponse deleteUser(@PathVariable String userEmail) {
        return userService.deleteUser(userEmail);
    }
}
