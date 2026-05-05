package com.hng.nameprocessing.services;

import com.hng.nameprocessing.dtos.DataMapping;
import com.hng.nameprocessing.dtos.UploadResult;
import org.apache.commons.csv.CSVRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.DoubleBuffer;
import java.util.List;
import java.util.Optional;

@Service
public class ValidationService {

    public Optional<DataMapping> validate(CSVRecord record, UploadResult result) {

        try {
            String name = record.get("name").trim().toLowerCase();
            String gender = record.get("gender").trim().toLowerCase();
            String genderProbabilityStr = record.get("gender_probability").trim().toLowerCase();
            String age = record.get("age").trim().toLowerCase();
            String ageGroup = record.get("age_group").trim().toLowerCase();
            String countryId = record.get("country_id").trim().toLowerCase();
            String countryName = record.get("country_name").trim().toLowerCase();
            String countryProbabilityStr = record.get("country_probability").trim().toLowerCase();


            if (name == null || name.isBlank()
                    || age == null || gender == null || genderProbabilityStr == null|| ageGroup==null || countryId==null || countryName==null || countryProbabilityStr==null) {
                result.incrementReason("missing_fields");
                return Optional.empty();
            }

            int ageInt = Integer.parseInt(age);
            if (ageInt < 0) {
                result.incrementReason("invalid_age");
                return Optional.empty();
            }

            double genderProbability = Double.parseDouble(genderProbabilityStr);
            double countryProbability = Double.parseDouble(countryProbabilityStr);
            if (countryProbability>1 || countryProbability<0){
                result.incrementReason("invalid_countryProbability");
                return Optional.empty();
            }
            if (genderProbability>1 || genderProbability<0){
                result.incrementReason("invalid_genderProbability");
                return Optional.empty();
            }

            if (!List.of("male", "female").contains(gender.toLowerCase())) {
                result.incrementReason("invalid_gender");
                return Optional.empty();
            }

            if (!List.of("child", "teenager", "adult", "senior").contains(ageGroup.toLowerCase())){
                result.incrementReason("invalid_ageGroup");
                return Optional.empty();
            }

            DataMapping p = new DataMapping();
            p.setName(name);
            p.setGender(gender);
            p.setGenderProbability(Double.parseDouble(genderProbabilityStr));
            p.setAge(Integer.parseInt(age));
            p.setAgeGroup(ageGroup);
            p.setCountryId(countryId);
            p.setCountryName(countryName);
            p.setCountryProbability(Double.parseDouble(countryProbabilityStr));


            return Optional.of(p);

        } catch (Exception e) {
            result.incrementReason("malformed_row");
            return Optional.empty();
        }
    }
}
