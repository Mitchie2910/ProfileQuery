package com.hng.nameprocessing.repositories;

import com.hng.nameprocessing.dtos.DataMapping;
import com.hng.nameprocessing.dtos.DataRepositoryDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public class CustomDataRepository {

    @PersistenceContext
    EntityManager entityManager;

    public List<DataRepositoryDto> findByGenderOrCountryIdOrAgeGroup(String gender, String countryId, String ageGroup) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<DataRepositoryDto> query = cb.createQuery(DataRepositoryDto.class);

        Root<DataMapping> root = query.from(DataMapping.class);

        query.select(cb.construct(
                DataRepositoryDto.class,
                root.get("id"),
                root.get("name"),
                root.get("gender"),
                root.get("age"),
                root.get("ageGroup"),
                root.get("countryId")
        ));

        List<Predicate> predicates = Stream.of(
                genderPredicate(cb, root, gender),
                countryIdPredicate(cb, root, countryId),
                ageGroupPredicate(cb, root, ageGroup)
        )
                .filter(Objects::nonNull)
                .toList();

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }

    private Predicate genderPredicate(CriteriaBuilder cb, Root<DataMapping> root, String gender){
        return gender == null ? null : cb.equal(root.get("gender"), gender);
    }

    private Predicate countryIdPredicate(CriteriaBuilder cb, Root<DataMapping> root, String countryId){
        return countryId == null ? null : cb.equal(root.get("countryId"), countryId);
    }

    private Predicate ageGroupPredicate(CriteriaBuilder cb, Root<DataMapping> root, String ageGroupPredicate){
        return ageGroupPredicate == null ? null : cb.equal(root.get("ageGroup"), ageGroupPredicate);
    }


}
