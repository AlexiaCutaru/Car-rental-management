package ro.rentacar.car;

import java.time.Year;
import java.util.Locale;

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

@Controller
public class CarController {

    private final CarRepository carRepository;

    public CarController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @InitBinder("car")
    public void configureBinding(WebDataBinder binder) {
        binder.setAllowedFields(
                "brand", "model", "licensePlate",
                "manufactureYear", "pricePerDay", "inService"
        );

        binder.registerCustomEditor(
                String.class, new StringTrimmerEditor(true)
        );
    }

    @GetMapping("/cars")
    public String listCars(Model model) {
        model.addAttribute(
                "cars",
                carRepository.findAll(Sort.by("id").descending())
        );
        return "cars";
    }

    @GetMapping("/cars/new")
    public String showCreateForm(Model model) {
        model.addAttribute("car", new Car());
        return "car-form";
    }

    @PostMapping("/cars")
    public String createCar(
            @Valid @ModelAttribute("car") Car car,
            BindingResult errors,
            RedirectAttributes redirectAttributes) {

        validateCar(car, errors, null);

        if (errors.hasErrors()) {
            return "car-form";
        }

        try {
            carRepository.saveAndFlush(car);
        } catch (DataIntegrityViolationException exception) {
            addSaveError(errors);
            return "car-form";
        }

        redirectAttributes.addFlashAttribute(
                "success", "Mașina a fost adăugată cu succes."
        );

        return "redirect:/cars";
    }

    @GetMapping("/cars/{id}/edit")
    public String showEditForm(
            @PathVariable("id") Long id,
            Model model) {

        model.addAttribute("car", findCar(id));
        model.addAttribute("carId", id);

        return "car-form";
    }

    @PostMapping("/cars/{id}/edit")
    public String updateCar(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("car") Car form,
            BindingResult errors,
            Model model,
            RedirectAttributes redirectAttributes) {

        Car existing = findCar(id);
        model.addAttribute("carId", id);

        validateCar(form, errors, id);

        if (errors.hasErrors()) {
            return "car-form";
        }

        existing.setBrand(form.getBrand());
        existing.setModel(form.getModel());
        existing.setLicensePlate(form.getLicensePlate());
        existing.setManufactureYear(form.getManufactureYear());
        existing.setPricePerDay(form.getPricePerDay());
        existing.setInService(form.isInService());

        try {
            carRepository.saveAndFlush(existing);
        } catch (DataIntegrityViolationException exception) {
            addSaveError(errors);
            return "car-form";
        }

        redirectAttributes.addFlashAttribute(
                "success", "Datele mașinii au fost actualizate."
        );

        return "redirect:/cars";
    }

    @PostMapping("/cars/{id}/delete")
    public String deleteCar(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        Car car = findCar(id);

        try {
            carRepository.deleteById(car.getId());

            redirectAttributes.addFlashAttribute(
                    "success", "Mașina a fost ștearsă."
            );
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Mașina nu poate fi ștearsă deoarece are "
                            + "înregistrări asociate, precum închirieri."
            );
        }

        return "redirect:/cars";
    }

    private Car findCar(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Mașina nu există."
                ));
    }

    private void validateCar(
            Car car,
            BindingResult errors,
            Long excludedId) {

        if (car.getManufactureYear() != null
                && car.getManufactureYear() > Year.now().getValue()) {
            errors.rejectValue(
                    "manufactureYear", "future",
                    "Anul fabricației nu poate fi în viitor."
            );
        }

        if (errors.hasFieldErrors("licensePlate")) {
            return;
        }

        String plate = car.getLicensePlate();

        if (plate == null || plate.isBlank()) {
            errors.rejectValue(
                    "licensePlate", "required",
                    "Completează numărul de înmatriculare."
            );
            return;
        }

        plate = plate.replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);

        car.setLicensePlate(plate);

        boolean duplicate = excludedId == null
                ? carRepository.existsByLicensePlateIgnoreCase(plate)
                : carRepository.existsByLicensePlateIgnoreCaseAndIdNot(
                        plate, excludedId
                );

        if (duplicate) {
            errors.rejectValue(
                    "licensePlate", "duplicate",
                    "Există deja o altă mașină cu acest număr."
            );
        }
    }

    private void addSaveError(BindingResult errors) {
        errors.reject(
                "saveFailed",
                "Mașina nu a putut fi salvată. Verifică datele "
                        + "și dacă numărul de înmatriculare există deja."
        );
    }
}