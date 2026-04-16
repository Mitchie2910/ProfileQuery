package com.hng.nameprocessing.dtos;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "api", name = "mapping")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DataMapping {
    @Id
    private UUID id;
    private String name;
    private String gender;
    private Double genderProbability;
    private long sampleSize;
    private int age;
    private String ageGroup;
    private String countryId;
    private BigDecimal countryProbability;
    private Instant createdAt;
}
