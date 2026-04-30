package com.hng.nameprocessing.dtos;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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
    private UUID id = Generators.timeBasedEpochGenerator().generate();

    private String name;
    private String gender;
    private Double genderProbability;
    private int age;
    private String ageGroup;
    private String countryId;
    private String countryName;
    private Double countryProbability;
    @CreationTimestamp
    private Instant createdAt;
}
