package io.jeremy.account.service;

import io.jeremy.account.model.Payment;
import io.jeremy.account.model.User;
import io.jeremy.account.respository.PaymentRepository;
import io.jeremy.account.respository.UserRepository;
import io.jeremy.account.dto.requests.PaymentRequest;
import io.jeremy.account.dto.responses.UserPaymentsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public boolean addPaymentsBatch(List<PaymentRequest> paymentList) {

        for (PaymentRequest paymentRequest : paymentList) {
            Payment payment = new Payment();
            payment.setSalary(paymentRequest.getSalary());
            payment.setEmployee(paymentRequest.getEmployee().toLowerCase());
            LocalDate date = convertPeriodStringToLocalDate(paymentRequest.getPeriod());
            payment.setPeriod(date);

            userRepository.findByUsernameIgnoreCase(paymentRequest.getEmployee())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "User \"" + paymentRequest.getEmployee() + "\" not found!"));

            Optional<Payment> optionalPayment = paymentRepository.
                    findByEmployeeAndPeriod(paymentRequest.getEmployee().toLowerCase(), date);

            if (optionalPayment.isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "User \"" + paymentRequest.getEmployee() + "\" and period \"" + date + "\" already exists!");
            }

            paymentRepository.saveAndFlush(payment);
        }
        return true;
    }

    @Transactional
    public boolean updatePayment(PaymentRequest paymentRequest) {
        Payment payment = new Payment();
        payment.setSalary(paymentRequest.getSalary());

        userRepository.findByUsernameIgnoreCase(paymentRequest.getEmployee())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "User \"" + paymentRequest.getEmployee() + "\" not found!"));

        payment.setEmployee(paymentRequest.getEmployee());
        LocalDate date = convertPeriodStringToLocalDate(paymentRequest.getPeriod());
        payment.setPeriod(date);
        payment.setSalary(paymentRequest.getSalary());

        Optional<Payment> optionalPayment = paymentRepository.
                findByEmployeeAndPeriod(paymentRequest.getEmployee().toLowerCase(), date);

        optionalPayment.ifPresent(value -> payment.setId(value.getId()));

        paymentRepository.save(payment);

        return true;
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment not found for the period"));
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
        if (text == null || text.isEmpty() || text.isBlank()) {
            return text;
        }

        String firstLetter = String.valueOf(text.strip().charAt(0));

        return text.strip().toLowerCase().replaceFirst(firstLetter.toLowerCase(), firstLetter.toUpperCase());
    }

    private String convertSalaryInDollarsAndCents(Long salary) {

        long dollars = (salary > 0 & salary < 100) ? 0 : salary / 100;
        long cents = dollars > 0 ? salary % 100 : salary;

        return dollars + " dollar(s) " + cents + " cent(s)";
    }
}
