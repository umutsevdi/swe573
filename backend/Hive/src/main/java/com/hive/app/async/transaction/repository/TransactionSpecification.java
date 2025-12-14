package com.hive.app.async.transaction.repository;

import com.hive.app.async.transaction.model.Identifier;
import com.hive.app.async.transaction.model.TransactionEntity;
import com.hive.app.async.transaction.model.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionSpecification {
    public static Specification<TransactionEntity> hasIdentifier(Identifier id) {
        return (root, query, cb) ->
                id == null ? cb.conjunction() : cb.or(cb.and(
                                cb.equal(root.get("srcId"), id.id()),
                                cb.equal(root.get("srcType"), id.type())
                        ),
                        cb.and(
                                cb.equal(root.get("destId"), id.id()),
                                cb.equal(root.get("destType"), id.type()
                                )
                        )
                );
    }

    public static Specification<TransactionEntity> hasType(List<TransactionType> types) {
        return (root, query, cb) ->
                types == null || types.isEmpty() ? cb.conjunction() :
                        cb.and(root.get("type").in(types));
    }

    public static Specification<TransactionEntity> messageContains(String fragment) {
        return (root, query, cb) ->
                (fragment == null || fragment.isBlank())
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("message")), "%" + fragment.toLowerCase() + "%");
    }

    public static Specification<TransactionEntity> isAfter(LocalDateTime date) {
        return (root, query, cb) ->
                date == null ? cb.conjunction() :
                        cb.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<TransactionEntity> isBefore(LocalDateTime date) {
        return (root, query, cb) ->
                date == null ? cb.conjunction() :
                        cb.lessThanOrEqualTo(root.get("createdAt"), date);
    }
}

