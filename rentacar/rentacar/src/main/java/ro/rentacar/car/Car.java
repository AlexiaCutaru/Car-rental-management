package ro.rentacar.car;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Completează marca.")
    @Size(max = 100, message = "Marca poate avea maximum 100 de caractere.")
    @Column(nullable = false)
    private String brand;

    @NotBlank(message = "Completează modelul.")
    @Size(max = 100, message = "Modelul poate avea maximum 100 de caractere.")
    @Column(nullable = false)
    private String model;

    @NotBlank(message = "Completează numărul de înmatriculare.")
    @Size(max = 20, message = "Numărul poate avea maximum 20 de caractere.")
    @Column(nullable = false, unique = true)
    private String licensePlate;

    @NotNull(message = "Completează anul fabricației.")
    @Min(value = 1900, message = "Anul trebuie să fie cel puțin 1900.")
    @Column(nullable = false)
    private Integer manufactureYear;

    @NotNull(message = "Completează tariful zilnic.")
    @DecimalMin(value = "0.01", message = "Tariful trebuie să fie pozitiv.")
    @Digits(integer = 8, fraction = 2,
            message = "Introdu maximum 8 cifre înainte de virgulă și 2 după.")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Column(nullable = false)
    private boolean inService;

    public Car() {
    }

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Integer getManufactureYear() {
        return manufactureYear;
    }

    public void setManufactureYear(Integer manufactureYear) {
        this.manufactureYear = manufactureYear;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public boolean isInService() {
        return inService;
    }

    public void setInService(boolean inService) {
        this.inService = inService;
    }
}