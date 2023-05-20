package io.jeremy.account.service;

import io.jeremy.account.model.Group;
import io.jeremy.account.model.LogEntry;
import io.jeremy.account.respository.GroupRepository;
import io.jeremy.account.respository.PaymentRepository;
import io.jeremy.account.dto.requests.UserRequest;
import io.jeremy.account.model.User;
import io.jeremy.account.respository.UserRepository;
import io.jeremy.account.dto.responses.ChangePassResponse;
import io.jeremy.account.dto.responses.DeleteUserResponse;
import io.jeremy.account.dto.responses.LockUnlockResponse;
import io.jeremy.account.dto.responses.UserResponse;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final PaymentRepository paymentRepository;

    private final GroupRepository groupRepository;

    private final LoggingService loggingService;

    private final Set<String> breachedPasswords = Set.of("PasswordForJanuary", "PasswordForFebruary",
            "PasswordForMarch", "PasswordForApril", "PasswordForMay", "PasswordForJune", "PasswordForJuly",
            "PasswordForAugust", "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember",
            "PasswordForDecember");

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       PaymentRepository paymentRepository, GroupRepository groupRepository,
                       LoggingService loggingService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.paymentRepository = paymentRepository;
        this.groupRepository = groupRepository;
        this.loggingService = loggingService;
    }

    public UserResponse addUser(UserRequest userRequest) {
        if (!userRequest.getEmail().endsWith("@acme.com")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email not allowed");
        }

        if (userRequest.getPassword().length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        }

        if (breachedPasswords.contains(userRequest.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        }

        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(userRequest.getEmail());

        if (optionalUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");
        }

        Group group;
        if (userRepository.count() == 0) {
            group = groupRepository.findByName("ROLE_ADMINISTRATOR")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found!"));

        } else {
            group = groupRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found!"));

        }

        User user = new User(userRequest, passwordEncoder.encode(userRequest.getPassword()));
        user.addGroup(group);

        User savedUser = userRepository.save(user);
        loggingService.saveEntry(new LogEntry(
                "CREATE_USER",
                "Anonymous",
                savedUser.getUsername().toLowerCase(),
                "/api/auth/signup"));
        return new UserResponse(savedUser);
    }

    public UserResponse getUserResponse(String user) {
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(user);

        return optionalUser.map(UserResponse::new).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "User not found!"));
    }

    public User getUser(String user) {
        return userRepository.findByUsernameIgnoreCase(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
    }

    public List<UserResponse> getAllUsers() {
        List<User> userList = userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        return userList.stream().map(UserResponse::new).collect(Collectors.toList());
    }

    public ChangePassResponse updatePassword(String userName, String newPassword) {
        if (newPassword.length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        }

        if (breachedPasswords.contains(newPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        }

        User user = userRepository.findByUsernameIgnoreCase(userName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));


        String oldPassword = user.getPassword();

        if (passwordEncoder.matches(newPassword, oldPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        loggingService.saveEntry(new LogEntry(
                "CHANGE_PASSWORD",
                user.getUsername().toLowerCase(),
                user.getUsername().toLowerCase(),
                "/api/auth/changepass"));

        return new ChangePassResponse(userName.toLowerCase(), "The password has been updated successfully");
    }

    public DeleteUserResponse deleteUser(String email, String admin) {
        User userToDelete = userRepository.findByUsernameIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        boolean roleAdministrator = userToDelete
                .getUserGroups()
                .stream()
                .anyMatch(group -> userToDelete.hasGroup("ROLE_ADMINISTRATOR"));

        if (roleAdministrator) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        }
        DeleteUserResponse userResponse =
                new DeleteUserResponse(userToDelete.getUsername(), "Deleted successfully!");

        userRepository.delete(userToDelete);

        loggingService.saveEntry(new LogEntry(
                "DELETE_USER",
                admin.toLowerCase(),
                email.toLowerCase(),
                "api/admin/user"
        ));

        return userResponse;
    }

    public UserResponse grantRoles(String email, String role, String admin) {
        User user = userRepository.findByUsernameIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        Group newGroup = groupRepository.findByName("ROLE_" + role.toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!"));

        Set<Group> userGroups = user.getUserGroups();

        Optional<Group> adm = userGroups.stream().filter(Group::isAdministrative).findFirst();
        Optional<Group> biz = userGroups.stream().filter(Group::isBusiness).findFirst();

        if (newGroup.isAdministrative() && biz.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The user cannot combine administrative and business roles!");
        } else if (newGroup.isBusiness() && adm.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The user cannot combine administrative and business roles!");
        }

        user.addGroup(newGroup);

        User savedUser = userRepository.save(user);

        loggingService.saveEntry(new LogEntry(
                "GRANT_ROLE",
                admin.toLowerCase(),
                "Grant role %s to %s".formatted(role, email.toLowerCase()),
                "api/admin/user/role"

        ));
        return new UserResponse(savedUser);
    }

    public UserResponse removeRole(String email, String role, String admin) {
        User user = userRepository.findByUsernameIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        Group groupToDelete = groupRepository.findByName("ROLE_" + role)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!"));

        Set<Group> userGroups = user.getUserGroups();

        if (!userGroups.contains(groupToDelete)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
        }

        if ("ROLE_ADMINISTRATOR".equals(groupToDelete.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        }

        if (userGroups.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
        }

        userGroups.remove(groupToDelete);
        User savedUser = userRepository.save(user);

        loggingService.saveEntry(new LogEntry(
                "REMOVE_ROLE",
                admin.toLowerCase(),
                "Remove role %s from %s".formatted(role, email.toLowerCase()),
                "api/admin/user/role"

        ));

        return new UserResponse(savedUser);
    }

    public LockUnlockResponse lockUser(String email, String admin) {
        User user = userRepository.findByUsernameIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
        if (user.hasGroup("ROLE_ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
        }
        user.setLocked(true);
        userRepository.save(user);

        loggingService.saveEntry(new LogEntry(
                "LOCK_USER",
                admin.toLowerCase(),
                "Lock user %s".formatted(email.toLowerCase()),
                "api/admin/user/access"

        ));

        return new LockUnlockResponse(user.getUsername().toLowerCase(), "locked");
    }

    public void lockUser(String email) {
        User user = userRepository.findByUsernameIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
        if (user.hasGroup("ROLE_ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
        }
        user.setLocked(true);
        userRepository.save(user);
    }
    public LockUnlockResponse unlockUser(String email, String admin) {
        User user = userRepository.findByUsernameIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
        user.setLocked(false);
        user.setFailedAttempt(0);

        loggingService.saveEntry(new LogEntry(
                "UNLOCK_USER",
                admin.toLowerCase(),
                "Unlock user %s".formatted(email.toLowerCase()),
                "api/admin/user/access"
        ));

        return new LockUnlockResponse(user.getUsername().toLowerCase(), "unlocked");
    }

    public void increaseFailedAttempts(User user) {
        int newFailedAttempts = user.getFailedAttempt() + 1;
        user.setFailedAttempt(newFailedAttempts);
        userRepository.save(user);
    }

    public void resetFailedAttempts(User user) {
        user.setFailedAttempt(0);
        userRepository.save(user);
    }
}
