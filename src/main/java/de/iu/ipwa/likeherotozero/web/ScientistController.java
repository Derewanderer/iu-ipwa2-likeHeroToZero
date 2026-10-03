package de.iu.ipwa.likeherotozero.web;

import de.iu.ipwa.likeherotozero.model.EmissionRecord;
import de.iu.ipwa.likeherotozero.repository.EmissionRecordRepository;
import de.iu.ipwa.likeherotozero.service.EmissionService;
import de.iu.ipwa.likeherotozero.service.WorldBankService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Locale;

@Controller
@RequestMapping("/scientist")
@PreAuthorize("hasRole('SCIENTIST')")
public class ScientistController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScientistController.class);

    private final EmissionRecordRepository repository;
    private final EmissionService emissionService;
    private final WorldBankService worldBankService;

    public ScientistController(
            EmissionRecordRepository repository,
            EmissionService emissionService,
            WorldBankService worldBankService) {
        this.repository = repository;
        this.emissionService = emissionService;
        this.worldBankService = worldBankService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("records", repository.findAllByOrderByCountryNameAscYearDesc());
        model.addAttribute("form", new EmissionForm());
        return "scientist";
    }

    @PostMapping("/emissions")
    public String save(
            @Valid @ModelAttribute("form") EmissionForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("records", repository.findAllByOrderByCountryNameAscYearDesc());
            return "scientist";
        }

        EmissionRecord saved = emissionService.saveManual(
                form.getIso3(),
                form.getCountryName(),
                form.getYear(),
                form.getCo2Kt());
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Der Datensatz fuer " + saved.getCountryName() + " (" + saved.getYear() + ") wurde gespeichert.");
        return "redirect:/scientist";
    }

    @PostMapping("/import")
    public String importCountry(
            @ModelAttribute("iso3") String iso3,
            RedirectAttributes redirectAttributes) {
        String normalizedIso3 = iso3.trim().toUpperCase(Locale.ROOT);
        if (!normalizedIso3.matches("[A-Z]{3}")) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Bitte einen dreistelligen ISO-Laendercode eingeben, zum Beispiel DEU.");
            return "redirect:/scientist";
        }

        try {
            int count = worldBankService.importCountry(normalizedIso3);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    count + " World-Bank-Datensaetze fuer " + normalizedIso3 + " wurden importiert.");
        } catch (RestClientException | IllegalStateException exception) {
            LOGGER.warn("World-Bank-Import fuer {} fehlgeschlagen", normalizedIso3, exception);
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Der World-Bank-Import ist fehlgeschlagen: " + exception.getMessage());
        }
        return "redirect:/scientist";
    }

    public static class EmissionForm {

        @NotBlank(message = "ISO-3-Laendercode ist erforderlich.")
        @Pattern(regexp = "[A-Za-z]{3}", message = "Der Laendercode muss aus drei Buchstaben bestehen.")
        private String iso3;

        @NotBlank(message = "Laendername ist erforderlich.")
        private String countryName;

        @Min(value = 1960, message = "Das Jahr muss ab 1960 liegen.")
        @Max(value = 2100, message = "Das Jahr darf nicht nach 2100 liegen.")
        private int year = 2024;

        @DecimalMin(value = "0.0", inclusive = false, message = "Der Wert muss groesser als null sein.")
        @Digits(integer = 12, fraction = 4, message = "Maximal vier Nachkommastellen sind erlaubt.")
        private BigDecimal co2Kt;

        public String getIso3() {
            return iso3;
        }

        public void setIso3(String iso3) {
            this.iso3 = iso3;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public BigDecimal getCo2Kt() {
            return co2Kt;
        }

        public void setCo2Kt(BigDecimal co2Kt) {
            this.co2Kt = co2Kt;
        }
    }
}
