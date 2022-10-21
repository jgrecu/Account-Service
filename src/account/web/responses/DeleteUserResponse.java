package account.web.responses;

public class DeleteUserResponse {

    private String user;
    private String status;

    public DeleteUserResponse() {
    }

    public DeleteUserResponse(String user, String status) {
        this.user = user.toLowerCase();
        this.status = status;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
