package account.service;

import account.exceptions.EmployeeNotFoundException;
import account.model.Payment;
import account.respository.PaymentRepository;
import account.respository.UserRepository;
import account.web.requests.PaymentRequest;
import org.springframework.stereotype.Service;

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
            payment.setEmployee(paymentRequest.getEmployee());
            LocalDate date = convertPeriodStringToLocalDate(paymentRequest.getPeriod());
            payment.setPeriod(date);

            userRepository.findByUsernameIgnoreCase(paymentRequest.getEmployee())
                    .orElseThrow(() ->
                            new EmployeeNotFoundException("User \"" + paymentRequest.getEmployee() + "\" not found!"));

            Optional<Payment> optionalPayment = paymentRepository.
                    findByEmployeeAndPeriod(paymentRequest.getEmployee().toLowerCase(), date);

            if (optionalPayment.isPresent()) {
                throw new EmployeeNotFoundException("User \"" + paymentRequest.getEmployee() + "\" and period \""
                        + date + "\" already exists!");
            }

            paymentRepository.saveAndFlush(payment);
        }
        return true;
    }

    private LocalDate convertPeriodStringToLocalDate(String period) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        YearMonth ym = YearMonth.parse(period, formatter);
        return ym.atEndOfMonth();
    }
}
