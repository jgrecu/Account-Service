package io.jeremy.account.dto.requests;

import javax.validation.constraints.NotBlank;

public class LockUnlockRequest {

    @NotBlank
    private final String user;

    @NotBlank
    private final LockOperation operation;


    public enum LockOperation {
        LOCK, UNLOCK
    }

    public LockUnlockRequest(String user, LockOperation operation) {
        this.user = user;
        this.operation = operation;
    }

    public String getUser() {
        return user;
    }

    public LockOperation getOperation() {
        return operation;
    }

}