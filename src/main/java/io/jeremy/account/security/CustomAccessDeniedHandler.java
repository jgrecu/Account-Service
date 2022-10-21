package io.jeremy.account.security;

import io.jeremy.account.model.LogEntry;
import io.jeremy.account.service.LoggingService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final LoggingService loggingService;

    public CustomAccessDeniedHandler(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String user = request.getRemoteUser();
        if (user == null) user = "Anonymous";

        loggingService.saveEntry(new LogEntry(
                "ACCESS_DENIED",
                user.toLowerCase(),
                request.getRequestURI(),
                request.getRequestURI()));

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied!");
    }
}
