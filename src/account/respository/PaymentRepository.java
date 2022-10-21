package account.respository;

import account.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p WHERE p.employee = :employee AND p.period = :date")
    Optional<Payment> findByEmployeeAndPeriod(@Param("employee") String employee, @Param("date") LocalDate date);

    List<Payment> findByEmployeeOrderByPeriodDesc(String employee);

    List<Payment> findAllByEmployeeIgnoreCaseOrderByPeriodDesc(String employee);
}
