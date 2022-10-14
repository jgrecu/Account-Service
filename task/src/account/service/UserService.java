package account.service;

import account.exceptions.EmployeeNotFoundException;
import account.model.Group;
import account.model.Payment;
import account.respository.GroupRepository;
import account.respository.PaymentRepository;
import account.web.requests.UserRequest;
import account.model.User;
import account.web.responses.ChangePassResponse;
import account.web.responses.DeleteUserResponse;
import account.web.responses.UserPaymentsResponse;
import account.web.responses.UserResponse;
import account.respository.UserRepository;
import org.springframework.data.domain.Sort;
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
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final PaymentRepository paymentRepository;

    private final GroupRepository groupRepository;

    private final Set<String> breachedPasswords = Set.of("PasswordForJanuary", "PasswordForFebruary",
            "PasswordForMarch", "PasswordForApril", "PasswordForMay", "PasswordForJune", "PasswordForJuly",
            "PasswordForAugust", "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember",
            "PasswordForDecember");

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       PaymentRepository paymentRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.paymentRepository = paymentRepository;
        this.groupRepository = groupRepository;
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

        return new UserResponse(savedUser);
    }

    public UserResponse getUser(String user) {
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(user);

        return optionalUser.map(UserResponse::new).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "User not found!"));
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

        return new ChangePassResponse(userName.toLowerCase(), "The password has been updated successfully");
    }

    public DeleteUserResponse deleteUser(String email) {
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

        return userResponse;
    }

    public UserResponse grantRoles(String email, String role) {
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

        return new UserResponse(savedUser);
    }

    public UserResponse removeRole(String email, String role) {
        User user = userRepository.findByUsernameIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        Group groupToDelete = groupRepository.findByName("ROLE_" + role)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!"));

        Set<Group> userGroups = user.getUserGroups();

        if (!userGroups.contains(groupToDelete)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
        }

        if (groupToDelete.getName().equals("ROLE_ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        }

        if (userGroups.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
        }

        userGroups.remove(groupToDelete);
        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser);
    }

    public List<UserPaymentsResponse> getUserPayments(String user) {
        User retrievedUser = userRepository.findByUsernameIgnoreCase(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
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
        User retrievedUser = userRepository.findByUsernameIgnoreCase(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
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
