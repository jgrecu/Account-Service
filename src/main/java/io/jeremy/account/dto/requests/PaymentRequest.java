package io.jeremy.account.dto.requests;

import javax.validation.constraints.*;

public class PaymentRequest {
    @NotBlank(message = "Employee cannot be empty!")
    @Email(regexp = ".*@acme\\.com$", message = "Employee must be a valid email address @acme.com")
    private final String employee;
    @NotBlank
    @Pattern(regexp = "^(0?[1-9]|1[0-2])-(19|2[0-1])?\\d{2}$", message = "Wrong date!")
    private final String period;

    @Min(value = 0, message = "Salary must be non negative!")
    private final Long salary;


    public PaymentRequest(String employee, String period, Long salary) {
        this.employee = employee;
        this.period = period;
        this.salary = salary;
    }

    public String getEmployee() {
        return employee;
    }

    public String getPeriod() {
        return period;
    }

    public Long getSalary() {
        return salary;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "employee='" + employee + '\'' +
                ", period='" + period + '\'' +
                ", salary=" + salary +
                '}';
    }
}
