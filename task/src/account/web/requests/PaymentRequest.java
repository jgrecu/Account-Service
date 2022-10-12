package account.web.requests;

import javax.validation.constraints.*;

public class PaymentRequest {
    @NotBlank(message = "Employee cannot be empty!")
    @Email(regexp = ".*@acme\\.com$", message = "Employee must be a valid email address @acme.com")
    private String employee;
    @NotBlank
    @Pattern(regexp = "^(0?[1-9]|1[0-2])-(19|2[0-1])?\\d{2}$", message = "Wrong date!")
    private String period;

    @Min(value = 0, message = "Salary must be non negative!")
    private Long salary;


    public PaymentRequest() {
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Long getSalary() {
        return salary;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
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
