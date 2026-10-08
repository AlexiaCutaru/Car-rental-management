package ro.rentacar.rental;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import ro.rentacar.car.CarRepository;
import ro.rentacar.customer.CustomerRepository;

@Controller
public class RentalController {

    private final RentalService rentalService;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;

    public RentalController(
            RentalService rentalService,
            CarRepository carRepository,
            CustomerRepository customerRepository) {

        this.rentalService = rentalService;
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
    }

    @InitBinder("rentalForm")
    public void configureBinding(WebDataBinder binder) {
        binder.setAllowedFields(
                "carId", "customerId", "startDate", "endDate"
        );
    }

    @GetMapping("/rentals/new")
    public String showForm(Model model) {
        model.addAttribute("rentalForm", new RentalForm());
        loadOptions(model);
        return "rental-form";
    }

    @PostMapping("/rentals")
    public String createRental(
            @Valid @ModelAttribute("rentalForm") RentalForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            loadOptions(model);
            return "rental-form";
        }

        try {
            rentalService.create(form);
        } catch (IllegalArgumentException exception) {
            errors.reject("rentalInvalid", exception.getMessage());
            loadOptions(model);
            return "rental-form";
        }

        redirectAttributes.addFlashAttribute(
                "success", "Închirierea a fost înregistrată."
        );

        return "redirect:/rentals";
    }

    private void loadOptions(Model model) {
        model.addAttribute(
                "cars",
                carRepository.findByInServiceFalseOrderByBrandAscModelAsc()
        );

        model.addAttribute(
                "customers",
                customerRepository.findAll(Sort.by("name").ascending())
        );
    }

    @PostMapping("/rentals/{id}/status")
public String changeStatus(
        @PathVariable("id") Long id,
        @RequestParam("action") String action,
        RedirectAttributes redirectAttributes) {

    try {
        rentalService.changeStatus(id, action);

        String message = switch (action) {
            case "start" -> "Preluarea mașinii a fost înregistrată.";
            case "complete" -> "Închirierea a fost finalizată.";
            case "cancel" -> "Închirierea a fost anulată.";
            default -> "Starea închirierii a fost actualizată.";
        };

        redirectAttributes.addFlashAttribute("success", message);

    } catch (IllegalArgumentException exception) {
        redirectAttributes.addFlashAttribute(
                "error", exception.getMessage()
        );
    }

    return "redirect:/rentals";
}
}