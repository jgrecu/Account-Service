package io.jeremy.account.respository;

import io.jeremy.account.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String email);

    void deleteByUsernameIgnoreCase(String email);

    @Override
    List<User> findAll(Sort sort);
}
