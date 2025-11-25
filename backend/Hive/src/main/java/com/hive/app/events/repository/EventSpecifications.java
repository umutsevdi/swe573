package com.hive.app.events.repository;

import com.hive.app.events.model.EventEntity;
import com.hive.app.events.model.EventType;
import com.hive.app.events.model.Location;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public class EventSpecifications {

    public static Specification<EventEntity> includeCancelled(boolean showCancelled) {
        return (root, query, cb) ->
                showCancelled ? cb.conjunction() :
                        cb.equal(root.get("cancelled"), true);
    }

    public static Specification<EventEntity> hasText(String textSearch) {
        return (root, query, cb) ->
                textSearch == null ? cb.conjunction() :
                        cb.like(cb.lower(root.get("description")),
                                "%" + textSearch.toLowerCase() + "%");
    }

    public static Specification<EventEntity> isWithinProximity(Location userLocation, Integer proximity) {
        return (root, query, cb) -> {
            if (userLocation == null || proximity == null) return cb.conjunction();

            double lat = userLocation.getLatitude();
            double lon = userLocation.getLongitude();
            double radiusInDeg = proximity / 111.0; // rough conversion

            return cb.and(
                    cb.between(root.get("latitude"), lat - radiusInDeg, lat + radiusInDeg),
                    cb.between(root.get("longitude"), lon - radiusInDeg, lon + radiusInDeg)
            );
        };
    }

    public static Specification<EventEntity> isAfter(LocalDate startDate) {
        return (root, query, cb) ->
                startDate == null ? cb.conjunction() :
                        cb.greaterThanOrEqualTo(root.get("eventDate"), startDate);
    }

    public static Specification<EventEntity> isBefore(LocalDate endDate) {
        return (root, query, cb) ->
                endDate == null ? cb.conjunction() :
                        cb.lessThanOrEqualTo(root.get("eventDate"), endDate);
    }

    public static Specification<EventEntity> hasTags(List<String> tags) {
        return (root, query, cb) -> {
            if (tags == null || tags.isEmpty()) return cb.conjunction();

            return tags.stream()
                    .map(tag -> cb.like(root.get("tags"), "%" + tag + "%"))
                    .reduce(cb::or)
                    .orElse(cb.conjunction());
        };
    }

    public static Specification<EventEntity> hasType(EventType type) {
        return (root, query, cb) ->
                type == null ? cb.conjunction() :
                        cb.equal(root.get("type"), type);
    }
}

