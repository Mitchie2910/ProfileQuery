package com.hng.nameprocessing.repositories;

import com.hng.nameprocessing.dtos.DataMapping;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataRepository extends PagingAndSortingRepository<DataMapping, UUID>,
JpaRepository<DataMapping, UUID>{

     Optional<DataMapping> findByName(String name);

     Optional<DataMapping> findById(UUID id);

     Page<DataMapping> findAll(Specification<DataMapping> specs, Pageable pageable);

}
