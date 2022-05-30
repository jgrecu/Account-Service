package account.controllers;

import account.model.Employee;
import account.responses.EmployeeResponse;
import account.service.EmployeeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final EmployeeService employeeService;

    public AuthenticationController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/signup")
    public EmployeeResponse registerUser(@Valid @RequestBody Employee employee) {
        return employeeService.addEnployee(employee);
    }
}
