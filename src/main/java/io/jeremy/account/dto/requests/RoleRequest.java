package io.jeremy.account.dto.requests;

import javax.validation.constraints.NotBlank;

public class RoleRequest {

    @NotBlank
    private final String user;
    @NotBlank
    private final String role;

    @NotBlank
    private final Operation operation;

    public RoleRequest(String user, String role, Operation operation) {
        this.user = user;
        this.role = role;
        this.operation = operation;
    }

    public enum Operation {
        GRANT, REMOVE
    }

    public String getUser() {
        return user;
    }

    public String getRole() {
        return role;
    }

    public Operation getOperation() {
        return operation;
    }

}
