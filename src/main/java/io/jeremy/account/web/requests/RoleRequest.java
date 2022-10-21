package io.jeremy.account.web.requests;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class RoleRequest {

    @NotBlank
    private String user;
    private String role;

    @Pattern(regexp = "GRANT|REMOVE")
    private Operation operation;

    public enum Operation {
        GRANT, REMOVE
    }

    public RoleRequest() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}
