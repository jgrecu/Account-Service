package account.controllers;

import account.model.LogEntry;
import account.service.LoggingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/security")
public class AuditorController {

    private final LoggingService loggingService;

    public AuditorController(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @GetMapping("/events")
    public ResponseEntity<List<LogEntry>> getLogs() {
        List<LogEntry> logEntries = loggingService.getAllEntries();
        if (logEntries.isEmpty()) return ResponseEntity.ok(Collections.EMPTY_LIST);
        else return ResponseEntity.ok(logEntries);
    }
}
