package io.jeremy.account.dto.responses;

public class LockUnlockResponse {
    private String status;

    public LockUnlockResponse() {
    }

    public LockUnlockResponse(String user, String status) {
        this.status = "User %s %s!".formatted(user, status);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
