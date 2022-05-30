package account.service;

import account.exceptions.UserNotAllowed;
import account.model.Employee;
import account.responses.EmployeeResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final List<Employee> employeeList;

    public EmployeeService(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public EmployeeResponse addEnployee(Employee employee) {
        if (!employee.getEmail().endsWith("@acme.com")) {
            throw new UserNotAllowed("Email not allowed");
        }
        employeeList.add(employee);

        return new EmployeeResponse(employee);
    }
}
