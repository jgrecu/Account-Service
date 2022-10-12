package account.web.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;

public class ChangePassRequest {
    @JsonProperty("new_password")
    @NotBlank
    private String newPassword;

    public ChangePassRequest() {
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
