package de.iu.ipwa.likeherotozero.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "emission_record", uniqueConstraints =
        @UniqueConstraint(name = "uk_emission_country_year", columnNames = {"iso3", "emission_year"}))
public class EmissionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3)
    private String iso3;

    @Column(nullable = false, length = 100)
    private String countryName;

    @Column(name = "emission_year", nullable = false)
    private int year;

    @Column(nullable = false, precision = 16, scale = 4)
    private BigDecimal co2Kt;

    @Column(nullable = false, length = 200)
    private String source;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected EmissionRecord() {
    }

    public EmissionRecord(
            String iso3,
            String countryName,
            int year,
            BigDecimal co2Kt,
            String source,
            LocalDateTime updatedAt) {
        this.iso3 = iso3;
        this.countryName = countryName;
        this.year = year;
        this.co2Kt = co2Kt;
        this.source = source;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getIso3() {
        return iso3;
    }

    public String getCountryName() {
        return countryName;
    }

    public int getYear() {
        return year;
    }

    public BigDecimal getCo2Kt() {
        return co2Kt;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void correct(
            String countryName,
            BigDecimal co2Kt,
            String source,
            LocalDateTime updatedAt) {
        this.countryName = countryName;
        this.co2Kt = co2Kt;
        this.source = source;
        this.updatedAt = updatedAt;
    }
}
