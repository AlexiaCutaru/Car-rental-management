package ro.rentacar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ro.rentacar.car.CarRepository;
import ro.rentacar.customer.CustomerRepository;
import ro.rentacar.rental.RentalRepository;
import ro.rentacar.rental.RentalStatus;

@Controller
public class DashboardController {
    private final CarRepository cars;
    private final CustomerRepository customers;
    private final RentalRepository rentals;

    public DashboardController(CarRepository cars, CustomerRepository customers,
                               RentalRepository rentals) {
        this.cars = cars;
        this.customers = customers;
        this.rentals = rentals;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        var entries = rentals.findAll(Sort.by("id").descending());
        LocalDate today = LocalDate.now();
        model.addAttribute("todayLabel", today.format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.forLanguageTag("ro"))));
        model.addAttribute("carCount", cars.count());
        model.addAttribute("customerCount", customers.count());
        model.addAttribute("activeCount", entries.stream()
                .filter(r -> r.getStatus() == RentalStatus.ACTIVE).count());
        model.addAttribute("reservedCount", entries.stream()
                .filter(r -> r.getStatus() == RentalStatus.RESERVED).count());
        model.addAttribute("pickups", entries.stream()
                .filter(r -> r.getStatus() == RentalStatus.RESERVED
                        && r.getStartDate().equals(today)).toList());
        model.addAttribute("returns", entries.stream()
                .filter(r -> r.getStatus() == RentalStatus.ACTIVE
                        && !r.getEndDate().isAfter(today)).toList());
        model.addAttribute("recentRentals", entries.stream().limit(4).toList());
        return "dashboard";
    }
}
