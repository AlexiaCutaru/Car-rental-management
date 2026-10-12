package ro.rentacar.auth;

import java.security.Principal;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import ro.rentacar.customer.Customer;

@Controller
public class ClientAuthController {

    private final ClientAccountService service;
    private final ClientAccountRepository accounts;

    public ClientAuthController(
            ClientAccountService service,
            ClientAccountRepository accounts) {

        this.service = service;
        this.accounts = accounts;
    }

    @InitBinder("customer")
    public void configureBinding(WebDataBinder binder) {
        binder.setAllowedFields("name", "email", "phone");

        binder.registerCustomEditor(
                String.class,
                new StringTrimmerEditor(true)
        );
    }

    @GetMapping("/client/login")
    public String showLoginForm(
            Principal principal,
            Model model) {

        model.addAttribute("loggedIn", principal != null);

        return "client-login";
    }

    @GetMapping("/client/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("customer", new Customer());

        return "client-register";
    }

    @PostMapping("/client/register")
    public String register(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult errors,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            errors.reject(
                    "passwordMismatch",
                    "Cele două parole nu coincid."
            );
        }

        if (errors.hasErrors()) {
            return "client-register";
        }

        try {
            service.register(customer, password);

        } catch (IllegalArgumentException exception) {
            errors.reject(
                    "registrationFailed",
                    exception.getMessage()
            );

            return "client-register";

        } catch (DataIntegrityViolationException exception) {
            errors.reject(
                    "registrationFailed",
                    "Contul nu a putut fi creat. "
                            + "Verifică datele și emailul."
            );

            return "client-register";
        }

        redirectAttributes.addFlashAttribute(
                "success",
                "Contul a fost creat. Autentifică-te cu "
                        + "emailul și parola aleasă."
        );

        return "redirect:/client/login";
    }

    @GetMapping("/client/account")
    public String showAccount(
            Principal principal,
            Model model) {

        ClientAccount account = accounts.findByEmailIgnoreCase(
                principal.getName()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Contul nu există."
        ));

        model.addAttribute("customer", account.getCustomer());
        model.addAttribute("loginEmail", account.getEmail());

        return "client-account";
    }
}