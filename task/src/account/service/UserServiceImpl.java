package account.service;

import account.exceptions.UserExistsException;
import account.exceptions.UserNotAllowed;
import account.model.User;
import account.dto.UserDTO;
import account.responses.ChangePassResponse;
import account.responses.UserResponse;
import account.respository.UserRepository;
import account.security.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final Set<String> breachedPasswords = Set.of("PasswordForJanuary", "PasswordForFebruary",
            "PasswordForMarch", "PasswordForApril", "PasswordForMay", "PasswordForJune", "PasswordForJuly",
            "PasswordForAugust", "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember",
            "PasswordForDecember");

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> user = userRepository.findByUsernameIgnoreCase(username);
        return user.orElseThrow(() -> new UsernameNotFoundException("Username \"" + username + "\" not found"));
    }

    @Override
    public UserResponse addEmployee(UserDTO userDTO) {
        if (!userDTO.getEmail().endsWith("@acme.com")) {
            throw new UserNotAllowed("Email not allowed");
        }

        if (userDTO.getPassword().length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        }

        if (breachedPasswords.contains(userDTO.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        }

        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(userDTO.getEmail());

        if (optionalUser.isPresent()) {
            throw new UserExistsException("User exist!");
        }

        User user = new User(userDTO, passwordEncoder.encode(userDTO.getPassword()), Role.USER);

        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser);
    }

    @Override
    public UserResponse getUser(String user) {
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(user);

        return optionalUser.map(UserResponse::new).orElseThrow(() -> new UserNotAllowed("test"));
    }

    @Override
    public ChangePassResponse updatePassword(String userName, String newPassword) {
        if (newPassword.length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        }

        if (breachedPasswords.contains(newPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        }

        User user = userRepository.findByUsernameIgnoreCase(userName).get();


        String oldPassword = user.getPassword();

        if (passwordEncoder.matches(newPassword, oldPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return new ChangePassResponse(userName.toLowerCase(), "The password has been updated successfully");
    }
}
