package ro.rentacar.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Completează numele.")
    @Size(
            max = 100,
            message = "Numele poate avea maximum 100 de caractere."
    )
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Completează emailul.")
    @Email(message = "Introdu o adresă de email validă.")
    @Size(
            max = 254,
            message = "Adresa de email este prea lungă."
    )
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "Completează telefonul.")
    @Pattern(
            regexp = "^\\+?[0-9 ()-]{7,25}$",
            message = "Introdu un telefon valid, folosind cifre, "
                    + "spații, +, - sau paranteze."
    )
    @Column(nullable = false)
    private String phone;

    public Customer() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}