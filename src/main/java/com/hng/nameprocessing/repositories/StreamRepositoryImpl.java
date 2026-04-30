package com.hng.nameprocessing.repositories;

import com.hng.nameprocessing.dtos.DataMapping;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Repository
public class StreamRepositoryImpl implements StreamRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Stream<DataMapping> streamAllCustom(Specification<DataMapping> spec, Sort sort) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataMapping> cq = cb.createQuery(DataMapping.class);
        Root<DataMapping> root = cq.from(DataMapping.class);

        Predicate predicate = (spec == null)
                ? null
                : spec.toPredicate(root, cq, cb);

        if (predicate != null) {
            cq.where(predicate);
        }

        if (sort != null && sort.isSorted()) {
            List<Order> orders = new ArrayList<>();

            for (Sort.Order o : sort) {
                Path<?> path = root.get(o.getProperty());
                orders.add(o.isAscending() ? cb.asc(path) : cb.desc(path));
            }

            cq.orderBy(orders);
        }

        return entityManager.createQuery(cq)
                .setHint("org.hibernate.readOnly", true)
                .setHint("org.hibernate.fetchSize", 50)
                .getResultStream();
    }
}