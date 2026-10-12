package ro.rentacar;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class RecordDeletionController {

    private final RecordDeletionService service;

    public RecordDeletionController(
            RecordDeletionService service) {

        this.service = service;
    }

    @PostMapping("/customers/{id}/remove")
    public String deleteCustomer(
            @PathVariable("id") Long id,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        try {
            String loginEmail = service.deleteCustomer(id);

            if (authentication != null
                    && loginEmail != null
                    && loginEmail.equalsIgnoreCase(
                            authentication.getName()
                    )) {

                new SecurityContextLogoutHandler().logout(
                        request,
                        response,
                        authentication
                );
            }

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Clientul, contul său și închirierile asociate "
                            + "au fost șterse."
            );

        } catch (IllegalStateException exception) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

        } catch (DataIntegrityViolationException
                 | PersistenceException exception) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ștergerea nu a putut fi finalizată. "
                            + "Datele au fost păstrate."
            );
        }

        return "redirect:/customers";
    }

    @PostMapping("/rentals/{id}/remove")
    public String deleteRental(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {
            service.deleteRental(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Închirierea a fost ștearsă."
            );

        } catch (IllegalStateException exception) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

        } catch (DataIntegrityViolationException
                 | PersistenceException exception) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ștergerea nu a putut fi finalizată. "
                            + "Datele au fost păstrate."
            );
        }

        return "redirect:/rentals";
    }
}