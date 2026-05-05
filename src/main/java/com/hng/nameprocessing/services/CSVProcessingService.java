package com.hng.nameprocessing.services;

import com.hng.nameprocessing.dtos.DataMapping;
import com.hng.nameprocessing.dtos.UploadResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class CSVProcessingService {

    private final ValidationService validationService;
    private final JdbcTemplate jdbcTemplate;
    private final Executor executor;
    private static final int BATCH_SIZE = 1000;

    public CSVProcessingService(ValidationService validationService, JdbcTemplate jdbcTemplate, @Qualifier("asyncExecutor")Executor executor) {
        this.executor = executor;
        this.validationService = validationService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public CompletableFuture<UploadResult> process(Path path) {

        return CompletableFuture.supplyAsync(()->{
            UploadResult result = new UploadResult();
            result.setStatus("success");

            List<DataMapping> batch = new ArrayList<>(BATCH_SIZE);

            try (Reader reader = Files.newBufferedReader(path);
                 CSVParser parser = new CSVParser(reader,
                         CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                for (CSVRecord record : parser) {

                    result.setTotalRows(result.getTotalRows() + 1);

                    Optional<DataMapping> profileOpt =
                            validationService.validate(record, result);

                    if (profileOpt.isEmpty()) {
                        result.setSkipped(result.getSkipped() + 1);
                        continue;
                    }

                    batch.add(profileOpt.get());

                    if (batch.size() >= BATCH_SIZE) {
                        int inserted = bulkInsert(batch, result);
                        result.setInserted(result.getInserted() + inserted);
                        batch.clear();
                    }
                }

                if (!batch.isEmpty()) {
                    int inserted = bulkInsert(batch, result);
                    result.setInserted(result.getInserted() + inserted);
                }

            } catch (IOException e) {
                throw new RuntimeException("File processing failed", e);
            }

            return result;
        }, executor);
    }

    private int bulkInsert(List<DataMapping> batch, UploadResult result) {

        String sql = """
            INSERT INTO api.mapping (
                id,
                name,
                gender,
                gender_probability,
                age,
                age_group,
                country_id,
                country_name,
                country_probability
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (name)
            DO NOTHING
            """;

        int[] counts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DataMapping m = batch.get(i);

                ps.setObject(1, m.getId());
                ps.setObject(2, m.getName());
                ps.setObject(3, m.getGender());
                ps.setDouble(4, m.getGenderProbability());
                ps.setInt(5, m.getAge());
                ps.setString(6, m.getAgeGroup());
                ps.setString(7, m.getCountryId());
                ps.setString(8, m.getCountryName());
                ps.setDouble(9, m.getCountryProbability());
            }

            @Override
            public int getBatchSize() {
                return batch.size();
            }
        });

        int inserted = 0;

        for (int count : counts) {
            if (count == 0) {
                result.incrementReason("duplicate_name");
                result.setSkipped(result.getSkipped() + 1);
            } else {
                inserted++;
            }
        }

        return inserted;
    }
}
