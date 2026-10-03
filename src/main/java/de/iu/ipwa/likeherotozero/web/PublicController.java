package de.iu.ipwa.likeherotozero.web;

import de.iu.ipwa.likeherotozero.model.EmissionRecord;
import de.iu.ipwa.likeherotozero.service.EmissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PublicController {

    private final EmissionService emissionService;

    public PublicController(EmissionService emissionService) {
        this.emissionService = emissionService;
    }

    @GetMapping("/")
    public String index(@RequestParam(defaultValue = "DEU") String country, Model model) {
        List<EmissionRecord> records = emissionService.allRecords();
        Map<String, String> countries = new LinkedHashMap<>();
        for (EmissionRecord record : records) {
            countries.putIfAbsent(record.getIso3(), record.getCountryName());
        }

        String selectedIso3 = countries.containsKey(country) ? country : countries.keySet().stream()
                .findFirst()
                .orElse("");
        List<EmissionRecord> history = selectedIso3.isBlank()
                ? List.of()
                : emissionService.latestYears(selectedIso3);

        model.addAttribute("countries", countries);
        model.addAttribute("selectedIso3", selectedIso3);
        model.addAttribute("history", history);
        model.addAttribute("latest", history.isEmpty() ? null : history.get(0));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
