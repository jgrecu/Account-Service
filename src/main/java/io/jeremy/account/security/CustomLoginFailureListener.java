package io.jeremy.account.security;

import io.jeremy.account.model.LogEntry;
import io.jeremy.account.model.User;
import io.jeremy.account.service.LoggingService;
import io.jeremy.account.service.UserService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class CustomLoginFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final UserService userService;
    private final LoggingService loggingService;
    private final HttpServletRequest request;

    public CustomLoginFailureListener(UserService userService, LoggingService loggingService,
                                      HttpServletRequest request) {
        this.userService = userService;
        this.loggingService = loggingService;
        this.request = request;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        try {
            String email = event.getAuthentication().getName();

            loggingService.saveEntry(new LogEntry(
                    "LOGIN_FAILED",
                    email.toLowerCase(),
                    request.getRequestURI(),
                    request.getRequestURI()

            ));
            User user = userService.getUser(email);

            if (!user.hasGroup("ROLE_ADMINISTRATOR")) {
                if (!user.isLocked()) {
                    if (user.getFailedAttempt() < UserService.MAX_FAILED_ATTEMPTS - 1) {
                        userService.increaseFailedAttempts(user);
                    } else {
                        loggingService.saveEntry(new LogEntry(
                                "BRUTE_FORCE",
                                email.toLowerCase(),
                                request.getRequestURI(),
                                request.getRequestURI()

                        ));
                        userService.lockUser(email);
                        loggingService.saveEntry(new LogEntry(
                                "LOCK_USER",
                                email.toLowerCase(),
                                "Lock user %s".formatted(email.toLowerCase()),
                                request.getRequestURI()
                        ));
                    }
                }
            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
        }
    }
}
