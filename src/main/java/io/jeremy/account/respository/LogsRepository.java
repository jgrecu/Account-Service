package io.jeremy.account.respository;

import io.jeremy.account.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogsRepository extends JpaRepository<LogEntry, Long> {
}
