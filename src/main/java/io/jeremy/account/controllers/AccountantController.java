package io.jeremy.account.controllers;

import io.jeremy.account.service.PaymentService;
import io.jeremy.account.web.requests.PaymentRequest;
import io.jeremy.account.web.responses.PaymentsResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
        paymentService.addPaymentsBatch(paymentList);
        return new PaymentsResponse("Added successfully!");
    }

    @PutMapping("/payments")
    public PaymentsResponse updateSalary(@Valid @RequestBody PaymentRequest payment) {
        paymentService.updatePayment(payment);
        return new PaymentsResponse("Updated successfully!");
    }

}
