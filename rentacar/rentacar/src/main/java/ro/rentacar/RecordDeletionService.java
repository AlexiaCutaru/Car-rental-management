package ro.rentacar;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import ro.rentacar.auth.ClientAccount;
import ro.rentacar.customer.Customer;
import ro.rentacar.rental.Rental;
import ro.rentacar.rental.RentalStatus;

@Service
public class RecordDeletionService {

    @PersistenceContext
    private EntityManager manager;

    @Transactional
    public String deleteCustomer(Long id) {

        Customer customer = manager.find(
                Customer.class,
                id,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (customer == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Clientul nu există."
            );
        }

        List<Rental> rentals = manager.createQuery(
                "select r from Rental r "
                        + "where r.customer.id = :customerId",
                Rental.class
        )
                .setParameter("customerId", id)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();

        LocalDate today = LocalDate.now(
                ZoneId.of("Europe/Bucharest")
        );

        // Verificăm toate închirierile înainte de orice ștergere.
        for (Rental rental : rentals) {

            if (rental.getStatus() == RentalStatus.ACTIVE) {
                throw new IllegalStateException(
                        "Clientul nu poate fi șters: "
                                + "are o închiriere activă. "
                                + "Finalizează închirierea după "
                                + "returnarea mașinii."
                );
            }

            if (rental.getEndDate() == null
                    || rental.getEndDate().isAfter(today)) {

                throw new IllegalStateException(
                        "Clientul nu poate fi șters: "
                                + "perioada unei închirieri nu s-a încheiat. "
                                + "Data returnării trebuie să fie "
                                + "astăzi sau în trecut."
                );
            }
        }

        List<ClientAccount> accounts = manager.createQuery(
                "select a from ClientAccount a "
                        + "where a.customer.id = :customerId",
                ClientAccount.class
        )
                .setParameter("customerId", id)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();

        String loginEmail = accounts.isEmpty()
                ? null
                : accounts.get(0).getEmail();

        // Ștergem mai întâi înregistrările asociate.
        for (Rental rental : rentals) {
            manager.remove(rental);
        }

        manager.flush();

        for (ClientAccount account : accounts) {
            manager.remove(account);
        }

        manager.flush();

        manager.remove(customer);
        manager.flush();

        return loginEmail;
    }

    @Transactional
    public void deleteRental(Long id) {

        Rental rental = manager.find(
                Rental.class,
                id,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (rental == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Închirierea nu există."
            );
        }

        if (rental.getStatus() == RentalStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Închirierea este activă și nu poate fi ștearsă. "
                            + "Finalizeaz-o după returnarea mașinii."
            );
        }

        LocalDate today = LocalDate.now(
                ZoneId.of("Europe/Bucharest")
        );

        if (rental.getEndDate() == null
                || rental.getEndDate().isAfter(today)) {

            throw new IllegalStateException(
                    "Perioada închirierii nu s-a încheiat. "
                            + "Ștergerea este permisă când data returnării "
                            + "este astăzi sau în trecut."
            );
        }

        manager.remove(rental);
        manager.flush();
    }
}