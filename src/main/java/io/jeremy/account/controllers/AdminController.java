package io.jeremy.account.controllers;

import io.jeremy.account.service.UserService;
import io.jeremy.account.dto.requests.LockUnlockRequest;
import io.jeremy.account.dto.requests.RoleRequest;
import io.jeremy.account.dto.responses.DeleteUserResponse;
import io.jeremy.account.dto.responses.LockUnlockResponse;
import io.jeremy.account.dto.responses.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
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
    public DeleteUserResponse deleteUser(@PathVariable String userEmail, Authentication authentication) {
        var loggedUser = authentication.getName();
        return userService.deleteUser(userEmail, loggedUser);
    }

    @PutMapping("/user/role")
    public UserResponse grantRole(@RequestBody @Valid RoleRequest roleRequest, Authentication authentication) {
        var loggedUser = authentication.getName();
        var operation = roleRequest.getOperation().name();

        if (operation.equals("GRANT")) {
            return userService.grantRoles(roleRequest.getUser(), roleRequest.getRole(), loggedUser);
        }

        if (operation.equals("REMOVE")) {
            return userService.removeRole(roleRequest.getUser(), roleRequest.getRole(), loggedUser);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operation must be either GRANT or REMOVE!");
    }

    @PutMapping("/user/access")
    public LockUnlockResponse lockUnlockUser(@RequestBody @Valid LockUnlockRequest request, Authentication authentication) {
        var loggedUser = authentication.getName();
        var operation = request.getOperation().name();

        if (operation.equals("LOCK")) {
            return userService.lockUser(request.getUser(), loggedUser);
        }

        if (operation.equals("UNLOCK")) {
            return userService.unlockUser(request.getUser(), loggedUser);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operation must be either LOCK or UNLOCK!");
    }
}
