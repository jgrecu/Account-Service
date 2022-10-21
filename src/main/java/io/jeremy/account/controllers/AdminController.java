package io.jeremy.account.controllers;

import io.jeremy.account.model.SecurityUser;
import io.jeremy.account.service.UserService;
import io.jeremy.account.web.requests.LockUnlockRequest;
import io.jeremy.account.web.requests.RoleRequest;
import io.jeremy.account.web.responses.DeleteUserResponse;
import io.jeremy.account.web.responses.LockUnlockResponse;
import io.jeremy.account.web.responses.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public DeleteUserResponse deleteUser(@PathVariable String userEmail, @AuthenticationPrincipal SecurityUser admin) {
        return userService.deleteUser(userEmail, admin);
    }

    @PutMapping("/user/role")
    public UserResponse grantRole(@RequestBody RoleRequest roleRequest, @AuthenticationPrincipal SecurityUser admin) {
        var operation = roleRequest.getOperation().name();

        if (operation.equals("GRANT")) {
            return userService.grantRoles(roleRequest.getUser(), roleRequest.getRole(), admin);
        }

        if (operation.equals("REMOVE")) {
            return userService.removeRole(roleRequest.getUser(), roleRequest.getRole(), admin);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operation must be only GRANT or REMOVE!");
    }

    @PutMapping("/user/access")
    public LockUnlockResponse lockUnlockUser(@RequestBody @Valid LockUnlockRequest request,
                                             @AuthenticationPrincipal SecurityUser admin) {
        var operation = request.getOperation().name();

        if (operation.equals("LOCK")) {
            return userService.lockUser(request.getUser(), admin);
        }

        if (operation.equals("UNLOCK")) {
            return userService.unlockUser(request.getUser(),admin);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operation must be only LOCK or UNLOCK!");
    }
}
