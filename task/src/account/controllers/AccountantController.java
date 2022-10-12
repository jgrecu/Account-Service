package account.controllers;

import account.service.PaymentService;
import account.web.requests.PaymentRequest;
import account.web.responses.PaymentsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/acct")
public class AccountantController {

    private final PaymentService paymentService;

    public AccountantController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public PaymentsResponse uploadPayrolls(@RequestBody List<@Valid PaymentRequest> paymentList) {
        boolean successful = paymentService.addPaymentsBatch(paymentList);
        return new PaymentsResponse("Updated successfully!");
    }

    @PutMapping("/payments")
    public PaymentsResponse updateSalary(@Valid @RequestBody PaymentRequest payment) {
        System.out.println(payment);
        return new PaymentsResponse("Updated successfully!");
    }

    @ExceptionHandler({ConstraintViolationException.class, org.hibernate.exception.ConstraintViolationException.class})
    public void handle(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }
}
