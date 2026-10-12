package ro.rentacar.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientAccountRepository
        extends JpaRepository<ClientAccount, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "customer")
    Optional<ClientAccount> findByEmailIgnoreCase(String email);
}