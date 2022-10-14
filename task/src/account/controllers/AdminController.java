package account.controllers;

import account.service.UserService;
import account.web.requests.RoleRequest;
import account.web.responses.DeleteUserResponse;
import account.web.responses.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @PutMapping("/user/role")
    public UserResponse grantRole(@RequestBody RoleRequest roleRequest) {
        var operation = roleRequest.getOperation().name();
        if (operation.equals("GRANT")) {
            return userService.grantRoles(roleRequest.getUser(), roleRequest.getRole());
        } else if (operation.equals("REMOVE")) {
            return userService.removeRole(roleRequest.getUser(), roleRequest.getRole());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Operation not found!");
    }
}
