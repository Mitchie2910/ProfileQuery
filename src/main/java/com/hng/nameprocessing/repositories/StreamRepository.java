package com.hng.nameprocessing.repositories;

import com.hng.nameprocessing.dtos.DataMapping;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.stream.Stream;

public interface StreamRepository {
    Stream<DataMapping> streamAllCustom(Specification<DataMapping> spec, Sort sort);
}
