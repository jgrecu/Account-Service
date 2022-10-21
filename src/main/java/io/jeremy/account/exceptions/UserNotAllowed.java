package io.jeremy.account.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "User is not allowed")
public class UserNotAllowed extends RuntimeException {
    public UserNotAllowed(String message) {
        super(message);
    }
}
