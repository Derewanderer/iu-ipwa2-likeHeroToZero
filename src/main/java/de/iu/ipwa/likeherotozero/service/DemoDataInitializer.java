package de.iu.ipwa.likeherotozero.service;

import de.iu.ipwa.likeherotozero.model.EmissionRecord;
import de.iu.ipwa.likeherotozero.repository.EmissionRecordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final EmissionRecordRepository repository;

    public DemoDataInitializer(EmissionRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws IOException {
        if (repository.count() > 0) {
            return;
        }

        List<EmissionRecord> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("data/world-bank-co2.csv").getInputStream(),
                StandardCharsets.UTF_8))) {
            String row;
            reader.readLine();
            while ((row = reader.readLine()) != null) {
                String[] fields = row.split(",", -1);
                if (fields.length != 4) {
                    throw new IOException("Ungueltige Zeile in data/world-bank-co2.csv: " + row);
                }
                records.add(new EmissionRecord(
                        fields[0],
                        fields[1],
                        Integer.parseInt(fields[2]),
                        new BigDecimal(fields[3]),
                        "World Bank / EN.GHG.CO2.MT.CE.AR5 (Offline-Demo)",
                        LocalDateTime.now()));
            }
        }
        if (records.isEmpty()) {
            throw new IOException("Die World-Bank-Demodatei enthaelt keine Datensaetze.");
        }
        repository.saveAll(records);
    }
}
