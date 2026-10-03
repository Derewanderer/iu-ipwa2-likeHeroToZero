package de.iu.ipwa.likeherotozero.repository;

import de.iu.ipwa.likeherotozero.model.EmissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmissionRecordRepository extends JpaRepository<EmissionRecord, Long> {

    List<EmissionRecord> findAllByOrderByCountryNameAscYearDesc();

    List<EmissionRecord> findTop6ByIso3OrderByYearDesc(String iso3);

    Optional<EmissionRecord> findFirstByIso3OrderByYearDesc(String iso3);

    Optional<EmissionRecord> findByIso3AndYear(String iso3, int year);
}
