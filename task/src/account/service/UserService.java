package account.service;

import account.exceptions.EmployeeNotFoundException;
import account.model.Payment;
import account.respository.PaymentRepository;
import account.web.requests.UserRequest;
import account.exceptions.UserNotAllowed;
import account.model.User;
import account.web.responses.ChangePassResponse;
import account.web.responses.UserPaymentsResponse;
import account.web.responses.UserResponse;
import account.respository.UserRepository;
import account.model.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final PaymentRepository paymentRepository;

    private final Set<String> breachedPasswords = Set.of("PasswordForJanuary", "PasswordForFebruary",
            "PasswordForMarch", "PasswordForApril", "PasswordForMay", "PasswordForJune", "PasswordForJuly",
            "PasswordForAugust", "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember",
            "PasswordForDecember");

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.paymentRepository = paymentRepository;
    }

    public UserResponse addEmployee(UserRequest userRequest) {
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

        User user = new User(userRequest, passwordEncoder.encode(userRequest.getPassword()), Role.USER);

        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser);
    }

    public UserResponse getUser(String user) {
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(user);

        return optionalUser.map(UserResponse::new).orElseThrow(() -> new UserNotAllowed("test"));
    }

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

    public List<UserPaymentsResponse> getUserPayments(String user) {
        User retrievedUser = userRepository.findByUsernameIgnoreCase(user).get();
        String name = retrievedUser.getName();
        String lastName = retrievedUser.getLastname();

        List<Payment> paymentList = paymentRepository.findByEmployeeOrderByPeriodDesc(user.toLowerCase());

        List<UserPaymentsResponse> userPaymentsResponseList = new ArrayList<>();

        for (Payment payment : paymentList) {
            String periodString = convertLocalDateToNicePeriodString(payment.getPeriod());
            String salary = convertSalaryInDollarsAndCents(payment.getSalary());
            userPaymentsResponseList.add(new UserPaymentsResponse(name, lastName, periodString, salary));
        }

        return userPaymentsResponseList;
    }

    public UserPaymentsResponse getUserPaymentForPeriod(String user, String period) {
        User retrievedUser = userRepository.findByUsernameIgnoreCase(user).get();
        String name = retrievedUser.getName();
        String lastName = retrievedUser.getLastname();
        LocalDate date = convertPeriodStringToLocalDate(period);

        Optional<Payment> paymentOptional = paymentRepository
                .findByEmployeeAndPeriod(user.toLowerCase(), date);

        return paymentOptional.map(payment -> new UserPaymentsResponse(name, lastName,
                convertLocalDateToNicePeriodString(payment.getPeriod()),
                convertSalaryInDollarsAndCents(payment.getSalary())))
                .orElseThrow(() -> new EmployeeNotFoundException("Payment not found for the period"));
    }

    private String convertLocalDateToNicePeriodString(LocalDate date) {
        String periodMonth = convertToTitleCaseIteratingChars(date.getMonth().toString());
        String periodYear = String.valueOf(date.getYear());
        return periodMonth + "-" + periodYear;
    }
    private LocalDate convertPeriodStringToLocalDate(String period) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        YearMonth ym = YearMonth.parse(period, formatter);
        return ym.atEndOfMonth();
    }
    private String convertToTitleCaseIteratingChars(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }

        return converted.toString();
    }

    private String convertSalaryInDollarsAndCents(Long salary) {

        long dollars = (salary > 0 & salary < 100) ? 0 : salary / 100;
        long cents = dollars > 0 ? salary % dollars : salary;

        return dollars + " dollar(s) " + cents + " cent(s)";
    }
}
