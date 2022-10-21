package io.jeremy.account.security;

import io.jeremy.account.model.User;
import io.jeremy.account.service.UserService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class CustomLoginSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final UserService userService;

    public CustomLoginSuccessListener(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String email = event.getAuthentication().getName();
        User user = userService.getUser(email);
        if (user.getFailedAttempt() > 0) {
            userService.resetFailedAttempts(user);
        }
    }
}
