package com.hng.nameprocessing.repositories;

import com.hng.nameprocessing.dtos.DataMapping;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SeederRepository {
  private final JdbcTemplate jdbcTemplate;

  public SeederRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private static final String SQL =
      """
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

  public void insertBatch(List<DataMapping> batch) {
    jdbcTemplate.batchUpdate(
        SQL,
        new BatchPreparedStatementSetter() {
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
  }
}
