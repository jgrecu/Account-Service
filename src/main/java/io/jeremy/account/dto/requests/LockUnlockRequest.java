package io.jeremy.account.dto.requests;

import javax.validation.constraints.NotBlank;

public class LockUnlockRequest {

    @NotBlank
    private String user;

    @NotBlank
    private LockOperation operation;


    public enum LockOperation {
        LOCK, UNLOCK
    }

    public LockUnlockRequest() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public LockOperation getOperation() {
        return operation;
    }

    public void setOperation(LockOperation operation) {
        this.operation = operation;
    }
}
