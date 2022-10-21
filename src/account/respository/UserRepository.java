package account.respository;

import account.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String email);

    void deleteByUsernameIgnoreCase(String email);

    @Override
    List<User> findAll(Sort sort);
}
