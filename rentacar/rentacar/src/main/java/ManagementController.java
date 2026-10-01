package ro.rentacar;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ro.rentacar.customer.CustomerRepository;
import ro.rentacar.rental.RentalRepository;

@Controller
public class ManagementController {

    private final CustomerRepository customerRepository;
    private final RentalRepository rentalRepository;

    public ManagementController(
            CustomerRepository customerRepository,
            RentalRepository rentalRepository) {
        this.customerRepository = customerRepository;
        this.rentalRepository = rentalRepository;
    }

    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute(
                "customers",
                customerRepository.findAll(Sort.by("name").ascending())
        );
        return "customers";
    }

    @GetMapping("/rentals")
    public String rentals(Model model) {
        model.addAttribute(
                "rentals",
                rentalRepository.findAll(Sort.by("startDate").descending())
        );
        return "rentals";
    }
}