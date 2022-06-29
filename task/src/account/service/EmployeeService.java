package account.service;

import account.exceptions.UserExistsException;
import account.exceptions.UserNotAllowed;
import account.model.Employee;
import account.security.Role;
import account.model.User;
import account.responses.EmployeeResponse;
import account.respository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public EmployeeResponse addEmployee(Employee employee) {
        if (!employee.getEmail().endsWith("@acme.com")) {
            throw new UserNotAllowed("Email not allowed");
        }

        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(employee.getEmail());

        if (optionalUser.isPresent()) {
            throw new UserExistsException("User exist!");
        }

        User user = new User(employee, passwordEncoder.encode(employee.getPassword()), Role.USER);

        User savedUser = userRepository.save(user);

        return new EmployeeResponse(savedUser);
    }

    public EmployeeResponse getEmployee(String user) {
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(user);

        return optionalUser.map(EmployeeResponse::new).orElseThrow(() -> new UserNotAllowed("test"));
    }
}
