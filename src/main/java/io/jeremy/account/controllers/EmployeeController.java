package io.jeremy.account.controllers;

import io.jeremy.account.exceptions.BadRequestException;
import io.jeremy.account.service.PaymentService;
import io.jeremy.account.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.security.Principal;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/empl")
public class EmployeeController {

    private final PaymentService paymentService;

    public EmployeeController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payment")
    public ResponseEntity<?> getEmployeePayment(Principal principal,
                                                @RequestParam(name = "period", required = false) String period) {
        String regexp = "^(0?[1-9]|1[0-2])-(19|2[0-1])?\\d{2}$";
        if (period != null && !Pattern.matches(regexp, period)) {
            throw new BadRequestException("Wrong date!");
        }

        String name = principal.getName();
        if (period != null) {
            return ResponseEntity.ok(paymentService.getUserPaymentForPeriod(name, period));
        }
        return ResponseEntity.ok(paymentService.getUserPayments(name));
    }

    @ExceptionHandler({ConstraintViolationException.class, org.hibernate.exception.ConstraintViolationException.class})
    public void handle(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }
}
