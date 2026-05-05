package com.hng.nameprocessing.dtos;

import java.util.Objects;
import java.util.stream.Stream;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryParameters {
  String gender;
  String ageGroup;
  String countryId;
  Integer maxAge;
  Integer minAge;
  Float minGenderProbability;
  Float minCountryProbability;

  public boolean isNull() {
    return Stream.of(
            gender,
            ageGroup,
            countryId,
            maxAge,
            minAge,
            minGenderProbability,
            minCountryProbability)
        .allMatch(Objects::isNull);
  }

  public String toKey(){
    return gender+"|"+ageGroup+"|"+countryId+"|"+maxAge+"|"+minAge+"|"+minGenderProbability+"|"+minCountryProbability;
  }
}
