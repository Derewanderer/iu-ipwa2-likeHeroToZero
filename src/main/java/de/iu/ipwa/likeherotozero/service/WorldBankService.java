package de.iu.ipwa.likeherotozero.service;

import com.fasterxml.jackson.databind.JsonNode;
import de.iu.ipwa.likeherotozero.model.EmissionRecord;
import de.iu.ipwa.likeherotozero.repository.EmissionRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class WorldBankService {

    private static final String INDICATOR = "EN.GHG.CO2.MT.CE.AR5";
    private static final String SOURCE = "World Bank / EN.GHG.CO2.MT.CE.AR5";
    private static final Pattern ISO3_PATTERN = Pattern.compile("[A-Z]{3}");

    private final RestClient restClient;
    private final EmissionRecordRepository repository;

    public WorldBankService(RestClient.Builder restClientBuilder, EmissionRecordRepository repository) {
        this.restClient = restClientBuilder.baseUrl("https://api.worldbank.org").build();
        this.repository = repository;
    }

    @Transactional
    public int importCountry(String countryCode) {
        String iso3 = countryCode.trim().toUpperCase(Locale.ROOT);
        if (!ISO3_PATTERN.matcher(iso3).matches()) {
            throw new IllegalArgumentException("Bitte einen dreistelligen ISO-Laendercode eingeben.");
        }

        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/country/{iso3}/indicator/{indicator}")
                        .queryParam("format", "json")
                        .queryParam("per_page", 20000)
                        .build(iso3, INDICATOR))
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.isArray() || response.size() < 2) {
            throw new IllegalStateException("Die World-Bank-API lieferte keine gueltige Datenantwort.");
        }
        if (!response.get(0).has("total")) {
            throw new IllegalStateException("Die World-Bank-API meldet einen Fehler: " + response);
        }

        JsonNode entries = response.get(1);
        if (!entries.isArray()) {
            throw new IllegalStateException("Fuer diesen Laendercode wurden keine World-Bank-Daten gefunden.");
        }

        LocalDateTime importedAt = LocalDateTime.now();
        List<EmissionRecord> records = new ArrayList<>();
        for (JsonNode entry : entries) {
            JsonNode value = entry.get("value");
            JsonNode date = entry.get("date");
            JsonNode country = entry.path("country").path("value");
            if (value == null || value.isNull() || date == null || country.isMissingNode()) {
                continue;
            }
            records.add(new EmissionRecord(
                    iso3,
                    country.asText(),
                    Integer.parseInt(date.asText()),
                    EmissionService.megatonnesToKilotonnes(new BigDecimal(value.asText())),
                    SOURCE,
                    importedAt));
        }

        if (records.isEmpty()) {
            throw new IllegalStateException("Die World-Bank-API enthaelt fuer dieses Land keine CO2-Werte.");
        }

        List<EmissionRecord> recordsToSave = new ArrayList<>(records.size());
        for (EmissionRecord record : records) {
            EmissionRecord recordToSave = repository.findByIso3AndYear(record.getIso3(), record.getYear())
                    .map(existing -> {
                        existing.correct(
                                record.getCountryName(),
                                record.getCo2Kt(),
                                record.getSource(),
                                importedAt);
                        return existing;
                    })
                    .orElse(record);
            recordsToSave.add(recordToSave);
        }
        repository.saveAll(recordsToSave);
        return records.size();
    }
}
