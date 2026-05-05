package com.hng.nameprocessing.repositories;

import com.hng.nameprocessing.dtos.DataMapping;
import java.util.stream.Stream;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public interface StreamRepository {
  Stream<DataMapping> streamAllCustom(Specification<DataMapping> spec, Sort sort);
}
