package com.hng.nameprocessing.repositories;

import com.hng.nameprocessing.dtos.DataMapping;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataRepository
    extends PagingAndSortingRepository<DataMapping, UUID>,
        JpaRepository<DataMapping, UUID>,
        StreamRepository,
        JpaSpecificationExecutor<DataMapping> {

  Optional<DataMapping> findByName(String name);

  Optional<DataMapping> findById(UUID id);

  Page<DataMapping> findAll(Specification<DataMapping> specs, Pageable pageable);
}
