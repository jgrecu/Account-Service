package io.jeremy.account.service;

import io.jeremy.account.model.LogEntry;
import io.jeremy.account.respository.LogsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoggingService {
    private final LogsRepository logsRepository;

    public LoggingService(LogsRepository logsRepository) {
        this.logsRepository = logsRepository;
    }

    public void saveEntry(LogEntry entry) {
        logsRepository.save(entry);
    }

    public List<LogEntry> getAllEntries() {
        return logsRepository.findAll();
    }
}
