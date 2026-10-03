package de.iu.ipwa.likeherotozero.service;

import de.iu.ipwa.likeherotozero.model.EmissionRecord;
import de.iu.ipwa.likeherotozero.repository.EmissionRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class EmissionService {

    private final EmissionRecordRepository repository;

    public EmissionService(EmissionRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EmissionRecord saveManual(
            String iso3,
            String countryName,
            int year,
            BigDecimal co2Kt) {
        String normalizedIso3 = iso3.trim().toUpperCase(Locale.ROOT);
        LocalDateTime updatedAt = LocalDateTime.now();
        EmissionRecord record = repository.findByIso3AndYear(normalizedIso3, year)
                .orElseGet(() -> new EmissionRecord(
                        normalizedIso3,
                        countryName.trim(),
                        year,
                        co2Kt,
                        "Manueller Wissenschaftler:innen-Beitrag",
                        updatedAt));
        record.correct(
                countryName.trim(),
                co2Kt,
                "Manueller Wissenschaftler:innen-Beitrag",
                updatedAt);
        return repository.save(record);
    }

    @Transactional(readOnly = true)
    public List<EmissionRecord> latestYears(String iso3) {
        return repository.findTop6ByIso3OrderByYearDesc(iso3);
    }

    @Transactional(readOnly = true)
    public List<EmissionRecord> allRecords() {
        return repository.findAllByOrderByCountryNameAscYearDesc();
    }

    public static BigDecimal megatonnesToKilotonnes(BigDecimal megatonnes) {
        return megatonnes.multiply(BigDecimal.valueOf(1000))
                .setScale(4, RoundingMode.HALF_UP);
    }
}
