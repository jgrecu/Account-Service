package account.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class UserNotAllowed extends RuntimeException {
    public UserNotAllowed(String message) {
        super(message);
    }
}
