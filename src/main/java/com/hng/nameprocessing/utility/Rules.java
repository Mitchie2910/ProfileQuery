package com.hng.nameprocessing.utility;

import com.hng.nameprocessing.dtos.QueryParameters;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class Rules {
  // Matching fields

  public Map<String, String> genderSynonyms =
      Map.ofEntries(
          Map.entry("men", "male"),
          Map.entry("man", "male"),
          Map.entry("woman", "female"),
          Map.entry("women", "female"),
          Map.entry("guy", "male"),
          Map.entry("guys", "male"),
          Map.entry("gentleman", "male"),
          Map.entry("girl", "female"),
          Map.entry("girls", "female"),
          Map.entry("lady", "female"),
          Map.entry("males", "male"),
          Map.entry("females", "female"),
          Map.entry("male", "male"),
          Map.entry("female", "female"));

  public Pattern aboveAgePattern =
      Pattern.compile("(above|over|greater than|more than|older than|over the age of)\\s+(\\d+)");

  public Pattern belowAgePattern =
      Pattern.compile(
          "(below|under|lesser than|less than|younger than|under the age of)\\s+(\\d+)");

  public Pattern ageBoundary =
          Pattern.compile(
                  "\\b(\\d+)\\s*(?:and|-|–|to)\\s*(\\d+)\\b"
          );

  public Pattern gennderConfidencePattern =
      Pattern.compile(
          "(gender)\\s+(confidence|probability|score)\\s*(above|over|greater than|more than|at least|min|minimum|>=|>)\\s+(\\d*\\.?\\d+)");

  public Pattern genderConfidencePattern1 =
      Pattern.compile(
          "(minimum|min)\\s+(gender)\\s*(confidence|probability|score)\\s+(of\\s+)?(\\d*\\.?\\d+)");

  public Pattern countryConfidencePattern =
      Pattern.compile(
          "(country|location|nationality)\\s+(confidence|probability|score)\\s*(above|over|greater than|more than|at least|min|minimum|>=|>)\\s+(\\d*\\.?\\d+)");

  public Pattern countryConfidencePattern1 =
      Pattern.compile(
          "(minimum|min)\\s+(country)\\s*(confidence|probability|score)\\s+(of\\s+)?(\\d*\\.?\\d+)");

  public Map<String, String> ageGroupMap =
      Map.ofEntries(
          Map.entry("child", "child"),
          Map.entry("children", "child"),
          Map.entry("kid", "child"),
          Map.entry("kids", "child"),
          Map.entry("toddler", "child"),
          Map.entry("infant", "child"),
          Map.entry("teen", "teenager"),
          Map.entry("teens", "teenager"),
          Map.entry("teenager", "teenager"),
          Map.entry("adolescent", "teenager"),
          Map.entry("adult", "adult"),
          Map.entry("adults", "adult"),
          Map.entry("grown-up", "adult"),
          Map.entry("grown ups", "adult"),
          Map.entry("senior", "senior"),
          Map.entry("seniors", "senior"),
          Map.entry("elderly", "senior"),
          Map.entry("retired", "senior"));
  public static final Map<String, String> COUNTRY_MAP = new HashMap<>();
  public static final Map<String, String> NATIONALITY_MAP = new HashMap<>();

  static {
    for (String code : Locale.getISOCountries()) {
      Locale locale = new Locale("", code);
      String name = locale.getDisplayCountry().toLowerCase();

      COUNTRY_MAP.put(name, code);
    }

    try (InputStream is = Rules.class.getClassLoader()
            .getResourceAsStream("countries/countries.csv")) {

      if (is == null) {
        throw new RuntimeException("countries.csv not found in classpath");
      }

      try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

        CSVFormat format = CSVFormat.RFC4180.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();

        CSVParser parser = CSVParser.builder()
                .setFormat(format)
                .setReader(reader)
                .get();

        if (parser.getHeaderNames().isEmpty()) {
          throw new IOException("CSV Headers cannot be empty");
        }

        for (CSVRecord record : parser) {
          NATIONALITY_MAP.put(
                  record.get("demonym").toLowerCase(),
                  record.get("iso2")
          );
        }
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // Extraction methods
  public void extractGender(QueryParameters queryParameters, List<String> tokens) {
    Boolean male = null;
    Boolean female = null;

    for (String token : tokens) {
      if (genderSynonyms.containsKey(token) && Objects.equals(genderSynonyms.get(token), "male")) {
        male = true;
      }
      if (genderSynonyms.containsKey(token)
          && Objects.equals(genderSynonyms.get(token), "female")) {
        female = true;
      }
    }

    if (male == null && female != null) {
      queryParameters.setGender("female");
    }
    if (male != null && female == null) {
      queryParameters.setGender("male");
    }
  }

  public void extractAgeGroup(QueryParameters queryParameters, List<String> tokens) {
    String ageGroup = null;

    for (String token : tokens) {
      if (ageGroupMap.containsKey(token)) {
        ageGroup = ageGroupMap.get(token);
        break;
      }
    }
    queryParameters.setAgeGroup(ageGroup);
  }

  public void extractAgeLimits(QueryParameters queryParameters, String query, List<String> tokens) {

    Integer minAge = null;
    Integer maxAge = null;

    for (String token : tokens) {
      if (token.equals("young")) {
        minAge = 16;
        maxAge = 24;
        queryParameters.setMinAge(minAge);
        queryParameters.setMaxAge(maxAge);
        return;
      }
    }

    Matcher maxAgeMatcher = belowAgePattern.matcher(query);
    Matcher minAgeMatcher = aboveAgePattern.matcher(query);
    Matcher combinedMatcher = ageBoundary.matcher(query);

    if (minAgeMatcher.find()) {
      String age = minAgeMatcher.group(2);
      queryParameters.setMinAge(Integer.parseInt(age));
    }

    if (maxAgeMatcher.find()) {
      String age = maxAgeMatcher.group(2);
      queryParameters.setMaxAge(Integer.parseInt(age));
    }

    if (combinedMatcher.find()) {
      String minMatchAge = combinedMatcher.group(1);
      String maxMatchAge = combinedMatcher.group(3);
      queryParameters.setMinAge(Integer.parseInt(minMatchAge));
      queryParameters.setMaxAge(Integer.parseInt(maxMatchAge));
    }
  }

  public void extractCountryId(QueryParameters queryParameters, List<String> tokens) {
    String countryId = null;

    for (String token : tokens) {
      if (COUNTRY_MAP.containsKey(token)) {
        countryId = COUNTRY_MAP.get(token);
        break;
      }
      if (NATIONALITY_MAP.containsKey(token)) {
        countryId = NATIONALITY_MAP.get(token);
        break;
      }
    }

    queryParameters.setCountryId(countryId);
  }

  public void extractMinGenderProbability(QueryParameters queryParameters, String query) {

    Matcher minGenderMatcher = gennderConfidencePattern.matcher(query);
    Matcher minGenderMatcher2 = genderConfidencePattern1.matcher(query);

    if (minGenderMatcher.find()) {
      String probability = minGenderMatcher.group(4);
      queryParameters.setMinGenderProbability(Float.parseFloat(probability));
    }
    if (minGenderMatcher2.find()) {
      String probability = minGenderMatcher2.group(5);
      queryParameters.setMinGenderProbability(Float.parseFloat(probability));
    }
  }

  public void extractMinCountryProbability(QueryParameters queryParameters, String query) {

    Matcher minCountryMatcher = countryConfidencePattern.matcher(query);
    Matcher minCountryMatcher2 = countryConfidencePattern1.matcher(query);

    if (minCountryMatcher.find()) {
      String probability = minCountryMatcher.group(4);
      queryParameters.setMinCountryProbability(Float.parseFloat(probability));
    }
    if (minCountryMatcher2.find()) {
      String probability = minCountryMatcher2.group(5);
      queryParameters.setMinGenderProbability(Float.parseFloat(probability));
    }
  }
}
