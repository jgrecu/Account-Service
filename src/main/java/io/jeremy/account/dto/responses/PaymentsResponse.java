package io.jeremy.account.dto.responses;

public class PaymentsResponse {
    private String status;

    public PaymentsResponse() {
    }

    public PaymentsResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
