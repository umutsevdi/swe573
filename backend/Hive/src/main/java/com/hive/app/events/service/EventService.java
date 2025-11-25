package com.hive.app.events.service;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.account.model.UserDTO;
import com.hive.app.events.mapper.EventMapper;
import com.hive.app.events.model.*;
import com.hive.app.events.repository.EventRepository;
import com.hive.app.events.repository.EventSpecifications;
import com.hive.config.exceptions.NotFoundException;
import com.hive.config.exceptions.PermissionDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class EventService {
    private final EventRepository repository;
    private final EventMapper mapper;
    private final UserMapper userMapper;

    @Cacheable(cacheNames = "getUserEvents", key = "{#userId, #showInactive}")
    public List<EventListView> getUserEvents(Long userId, boolean showInactive) {
        if (showInactive) {
            return repository.findByOwner_Id(userId);
        }
        return repository.findByCancelledIsFalseAndOwner_IdAndEventDateAfter(userId,
                LocalDate.now().minusDays(1));
    }

    public List<EventResponse> getAllEventsBy(EventQueryOptions options) {
        if (options.getStartDate() == null) {
            options.setStartDate(LocalDate.now().minusDays(1));
        }
        Specification<EventEntity> spec = Specification.allOf(
                EventSpecifications.includeCancelled(options.isIncludeCancelled()),
                EventSpecifications.hasText(options.getTextSearch()),
                EventSpecifications.hasText(options.getTextSearch()),
                EventSpecifications.isWithinProximity(options.getUserLocation(), options.getProximity()),
                EventSpecifications.isAfter(options.getStartDate()),
                EventSpecifications.isBefore(options.getEndDate()),
                EventSpecifications.hasTags(options.getTags()),
                EventSpecifications.hasType(options.getType())
        );

        return repository.findAll(spec).stream().map(mapper::entityToResponse).toList();
    }

    @Cacheable(cacheNames = "eventById", key = "#id")
    public EventResponse getEvent(Long id) throws NotFoundException {
        return repository.findByCancelledIsFalseAndId(id)
                .map(mapper::entityToResponse)
                .orElseThrow(NotFoundException::new);
    }


    @CacheEvict(cacheNames = {"getUserEvents", "eventById"}, allEntries = true)
    public EventResponse newEvent(EventRequest request, UserDTO userDTO) {
        request.setTags(request.getTags().stream().map(String::toLowerCase).toList());
        EventEntity entity = mapper.requestToEntity(request);
        entity.setCancelled(false);
        entity.setOwner(userMapper.dtoToEntity(userDTO));
        return mapper.entityToResponse(repository.save(entity));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "eventById", key = "#id"),
            @CacheEvict(cacheNames = "getUserEvents", allEntries = true)
    })
    public EventResponse updateEvent(Long id, EventRequest request, UserDTO userDTO) throws NotFoundException {
        EventDTO event = repository.findByCancelledIsFalseAndId(id)
                .map(mapper::entityToDTO)
                .orElseThrow(NotFoundException::new);
        if (!event.getOwner().getId().equals(userDTO.getId())) {
            throw new PermissionDeniedException();
        }
        log.info("{}", request);
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getTags() != null) {
            event.setTags(request.getTags().stream().map(String::toLowerCase).toList());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getMaxParticipants() != null) {
            event.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getType() != null) {
            event.setType(request.getType());
        }
        if (request.getDuration() != null) {
            event.setDuration(request.getDuration());
        }
        return mapper.entityToResponse(repository.save(mapper.dtoToEntity(event)));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "eventById", key = "#id"),
            @CacheEvict(cacheNames = "getUserEvents", allEntries = true)
    })
    public void cancelEvent(Long id, UserDTO userDTO) throws NotFoundException {
        EventEntity event = repository.findByCancelledIsFalseAndId(id)
                .orElseThrow(NotFoundException::new);
        if (!event.getOwner().getId().equals(userDTO.getId())) {
            throw new PermissionDeniedException();
        }
        event.setCancelled(true);
        repository.save(event);
    }

}
