package account.web.responses;

public class LockUnlockResponse {
    private String status;

    public LockUnlockResponse() {
    }

    public LockUnlockResponse(String user, String status) {
        this.status = String.format("User %s %s!", user, status);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
