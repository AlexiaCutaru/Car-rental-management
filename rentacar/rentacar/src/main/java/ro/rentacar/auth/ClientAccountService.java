package ro.rentacar.auth;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ro.rentacar.customer.Customer;
import ro.rentacar.customer.CustomerRepository;

@Service
public class ClientAccountService {

    private final ClientAccountRepository accounts;
    private final CustomerRepository customers;
    private final PasswordEncoder encoder;

    public ClientAccountService(
            ClientAccountRepository accounts,
            CustomerRepository customers,
            PasswordEncoder encoder) {

        this.accounts = accounts;
        this.customers = customers;
        this.encoder = encoder;
    }

    @Transactional
    public void register(Customer form, String password) {

        if (password == null
                || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new IllegalArgumentException(
                    "Parola trebuie să aibă minimum 8 caractere. "
                            + "Dacă este foarte lungă, alege una mai scurtă."
            );
        }

        String email = normalizeEmail(form.getEmail());

        if (accounts.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Există deja un cont cu acest email. Autentifică-te."
            );
        }

        if (customers.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Acest email aparține unui client adăugat deja manual. "
                            + "Asocierea unui cont necesită confirmarea identității."
            );
        }

        Customer customer = new Customer();
        customer.setName(form.getName());
        customer.setEmail(email);
        customer.setPhone(form.getPhone());

        customers.saveAndFlush(customer);

        accounts.saveAndFlush(new ClientAccount(
                email,
                encoder.encode(password),
                customer
        ));
    }

    public static String normalizeEmail(String email) {
        return email == null
                ? ""
                : email.strip().toLowerCase(Locale.ROOT);
    }
}