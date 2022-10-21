package io.jeremy.account.bootstrap;

import io.jeremy.account.model.Group;
import io.jeremy.account.respository.GroupRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GroupDataLoader {

    private final GroupRepository repository;

    public GroupDataLoader(GroupRepository repository) {
        this.repository = repository;
    }


    @Bean
    public CommandLineRunner commandLineRunner(GroupRepository repository) {
        return args -> {
            if (repository.findByName("ROLE_ADMINISTRATOR").isEmpty()) {
                repository.save(new Group("ROLE_ADMINISTRATOR"));
            }
            if (repository.findByName("ROLE_USER").isEmpty()) {
                repository.save(new Group("ROLE_USER"));
            }
            if (repository.findByName("ROLE_ACCOUNTANT").isEmpty()) {
                repository.save(new Group("ROLE_ACCOUNTANT"));
            }
            if (repository.findByName("ROLE_AUDITOR").isEmpty()) {
                repository.save(new Group("ROLE_AUDITOR"));
            }
        };
    }
}
