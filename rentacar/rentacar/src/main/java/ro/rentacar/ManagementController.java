package ro.rentacar;


import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import ro.rentacar.customer.Customer;
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

    @InitBinder("customer")
    public void configureCustomerBinding(WebDataBinder binder) {
        binder.setAllowedFields("name", "email", "phone");

        binder.registerCustomEditor(
                String.class,
                new StringTrimmerEditor(true)
        );
    }

    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute(
                "customers",
                customerRepository.findAll(
                        Sort.by("name").ascending()
                )
        );

        return "customers";
    }

    @GetMapping("/customers/new")
    public String showCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer-form";
    }

    @PostMapping("/customers")
    public String createCustomer(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult errors,
            RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            return "customer-form";
        }

        customerRepository.save(customer);

        redirectAttributes.addFlashAttribute(
                "success",
                "Clientul a fost adăugat cu succes."
        );

        return "redirect:/customers";
    }

    @GetMapping("/rentals")
    public String rentals(Model model) {
        model.addAttribute(
                "rentals",
                rentalRepository.findAll(
                        Sort.by("startDate").descending()
                )
        );

        return "rentals";
    }

    @GetMapping("/customers/{id}/edit")
        public String showEditCustomerForm(
                @PathVariable("id") Long id,
                Model model) {

            model.addAttribute("customer", findCustomer(id));
            model.addAttribute("customerId", id);

            return "customer-form";
        }

        @PostMapping("/customers/{id}/edit")
        public String updateCustomer(
                @PathVariable("id") Long id,
                @Valid @ModelAttribute("customer") Customer form,
                BindingResult errors,
                Model model,
                RedirectAttributes redirectAttributes) {

            Customer existing = findCustomer(id);
            model.addAttribute("customerId", id);

            if (errors.hasErrors()) {
                return "customer-form";
            }

            existing.setName(form.getName());
            existing.setEmail(form.getEmail());
            existing.setPhone(form.getPhone());

            customerRepository.save(existing);

            redirectAttributes.addFlashAttribute(
                    "success", "Datele clientului au fost actualizate."
            );

            return "redirect:/customers";
        }

        @PostMapping("/customers/{id}/delete")
        public String deleteCustomer(
                @PathVariable("id") Long id,
                RedirectAttributes redirectAttributes) {

            Customer customer = findCustomer(id);

            try {
                customerRepository.deleteById(customer.getId());

                redirectAttributes.addFlashAttribute(
                        "success", "Clientul a fost șters."
                );
            } catch (DataIntegrityViolationException exception) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Clientul nu poate fi șters deoarece are "
                                + "înregistrări asociate, precum închirieri."
                );
            }

            return "redirect:/customers";
        }

        private Customer findCustomer(Long id) {
            return customerRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Clientul nu există."
                    ));
        }
}