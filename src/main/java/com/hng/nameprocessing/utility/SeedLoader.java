package com.hng.nameprocessing.utility;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.hng.nameprocessing.dtos.DataMapping;
import com.hng.nameprocessing.repositories.SeederRepository;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedLoader implements CommandLineRunner {

  private final SeederRepository seederRepository;

  public SeedLoader(SeederRepository seederRepository) {
    this.seederRepository = seederRepository;
  }

  @Override
  public void run(String... args) throws Exception {

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    try (InputStream input = getClass().getResourceAsStream("/data/seed_profiles.json")) {

      MappingIterator<DataMapping> it = objectMapper.readerFor(DataMapping.class).readValues(input);

      List<DataMapping> batch = new ArrayList<>();

      int batchSize = 500;

      while (it.hasNext()) {

        batch.add(it.next());

        if (batch.size() == batchSize) {
          seederRepository.insertBatch(batch);
          batch.clear();
        }
      }
      if (!batch.isEmpty()) {
        seederRepository.insertBatch(batch);
      }
    }
  }
}
