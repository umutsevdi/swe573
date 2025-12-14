package com.hive.app.events.service;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.account.model.UserDTO;
import com.hive.app.account.model.UserRole;
import com.hive.app.account.service.UserService;
import com.hive.app.async.transaction.model.Identifier;
import com.hive.app.async.transaction.model.TransactionRecord;
import com.hive.app.async.transaction.model.TransactionType;
import com.hive.app.async.transaction.service.TransactionService;
import com.hive.app.events.mapper.EventMapper;
import com.hive.app.events.model.*;
import com.hive.app.events.model.request.EventQueryOptions;
import com.hive.app.events.model.request.EventRequest;
import com.hive.app.events.model.request.ParticipationAssignmentRequest;
import com.hive.app.events.model.response.EventListResponse;
import com.hive.app.events.model.response.EventResponse;
import com.hive.app.events.repository.EventRepository;
import com.hive.app.events.repository.EventSpecifications;
import com.hive.app.events.repository.ParticipationRepository;
import com.hive.config.exceptions.LogicException;
import com.hive.config.exceptions.NotFoundException;
import com.hive.config.exceptions.PermissionDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class EventService {
    private final EventRepository repository;
    private final ParticipationRepository participationRepository;
    private final EventMapper mapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final TransactionService transactionService;

    private static void isMutable(boolean isCancelled, LocalDate eventDate) throws LogicException {
        if (isCancelled || !eventDate.isAfter(LocalDate.now())) {
            throw new LogicException("Event is not modifiable.");
        }
    }

    @Cacheable(cacheNames = "getUserEvents", key = "{#userId, #showInactive}")
    public List<EventListResponse> getUserEvents(Long userId, boolean showInactive) {
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
        return repository.findById(id)
                .map(mapper::entityToResponse)
                .orElseThrow(NotFoundException::new);
    }

    @CacheEvict(cacheNames = "getUserEvents", allEntries = true)
    @Transactional
    public EventResponse newEvent(EventRequest request, UserDTO userDTO) {
        request.setTags(request.getTags().stream().map(e -> e.toLowerCase().replace(' ', '-')).toList());
        EventEntity entity = mapper.requestToEntity(request);
        entity.setCancelled(false);
        entity.setOwner(userMapper.dtoToEntity(userDTO));
        var result = mapper.entityToResponse(repository.save(entity));
        transactionService.insertTransaction(new TransactionRecord(
                TransactionType.EVENT_CREATE,
                new Identifier(userDTO.getId(), Identifier.IdType.USER),
                new Identifier(result.getId(), Identifier.IdType.EVENT),
                "User created TYPE titled 'EVENT_NAME'."
                        .replace("TYPE", result.getType().name().toLowerCase())
                        .replace("EVENT_NAME", result.getTitle())
        ));
        return result;
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "eventById", key = "#id"),
            @CacheEvict(cacheNames = "getUserEvents", allEntries = true)
    })
    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request, UserDTO userDTO) throws NotFoundException, LogicException {
        EventDTO event = repository.findById(id)
                .map(mapper::entityToDTO)
                .orElseThrow(NotFoundException::new);
        if (!event.getOwner().getId().equals(userDTO.getId())) {
            throw new PermissionDeniedException();
        }
        isMutable(event.isCancelled(), event.getEventDate());
        log.info("{}", request);
        boolean updated = false;
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
            updated = true;
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
            updated = true;
        }
        if (request.getTags() != null) {
            event.setTags(request.getTags().stream().map(e -> e.toLowerCase().replace(' ', '-')).toList());
            updated = true;
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
            updated = true;
        }
        if (request.getMaxParticipants() != null) {
            event.setMaxParticipants(request.getMaxParticipants());
            updated = true;
        }
        if (request.getType() != null) {
            event.setType(request.getType());
            updated = true;
        }
        if (request.getDuration() != null) {
            event.setDuration(request.getDuration());
            updated = true;
        }
        if (updated) {
            var result = mapper.entityToResponse(repository.save(mapper.dtoToEntity(event)));
            transactionService.insertTransaction(new TransactionRecord(
                    TransactionType.EVENT_UPDATE,
                    new Identifier(userDTO.getId(), Identifier.IdType.USER),
                    new Identifier(result.getId(), Identifier.IdType.EVENT),
                    "User updated TYPE titled 'EVENT_NAME'."
                            .replace("TYPE", result.getType().name().toLowerCase())
                            .replace("EVENT_NAME", result.getTitle())
            ));
            return result;
        }
        return mapper.dtoToResponse(event);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "eventById", key = "#id"),
            @CacheEvict(cacheNames = "getUserEvents", allEntries = true)
    })
    @Transactional
    public void cancelEvent(Long id, UserDTO userDTO) throws NotFoundException, LogicException {
        EventEntity event = repository.findById(id)
                .orElseThrow(NotFoundException::new);
        if (!event.getOwner().getId().equals(userDTO.getId()) &&
                userDTO.getRole() != UserRole.ADMIN &&
                userDTO.getRole() != UserRole.OWNER
        ) {
            throw new PermissionDeniedException();
        }
        isMutable(event.isCancelled(), event.getEventDate());
        event.setCancelled(true);
        repository.save(event);
        transactionService.insertTransaction(new TransactionRecord(
                TransactionType.EVENT_CANCEL,
                new Identifier(userDTO.getId(), Identifier.IdType.USER),
                new Identifier(event.getId(), Identifier.IdType.EVENT),
                "User cancelled TYPE titled 'EVENT_NAME'."
                        .replace("TYPE", event.getType().name().toLowerCase())
                        .replace("EVENT_NAME", event.getTitle())
        ));
    }

    @Cacheable(cacheNames = "getParticipants", key = "#eventId")
    public Map<Long, ParticipationState> getParticipants(Long eventId) {
        return participationRepository.findParticipants(eventId)
                .stream()
                .collect(Collectors.toMap(
                        ParticipantView::getOwnerId,
                        ParticipantView::getState));
    }

    @Transactional
    @CacheEvict(cacheNames = "getParticipants", key = "#eventId")
    public void participate(Long eventId, UserDTO userDTO, String message) throws NotFoundException, LogicException {
        EventEntity e = repository.findById(eventId).orElseThrow(NotFoundException::new);
        isMutable(e.isCancelled(), e.getEventDate());
        if (e.getOwner().getId().equals(userDTO.getId())) {
            throw new LogicException("Can not participate as event owner.");
        }
        if (userDTO.getTimeBank() < e.getDuration()) {
            throw new LogicException("Insufficient time bank.");
        }
        ParticipationEntity entity = participationRepository.findByOwner_IdAndEvent_Id(userDTO.getId(), eventId).orElse(new ParticipationEntity());
        entity.setMessage(message);
        entity.setOwner(userMapper.dtoToEntity(userDTO));
        entity.setEvent(e);
        entity.setState(ParticipationState.SENT);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        participationRepository.save(entity);
        transactionService.insertTransaction(new TransactionRecord(
                TransactionType.PARTICIPATION_NEW,
                new Identifier(userDTO.getId(), Identifier.IdType.USER),
                new Identifier(e.getId(), Identifier.IdType.EVENT),
                "User requested participation to TYPE with the message 'MESSAGE'."
                        .replace("TYPE", e.getType().name().toLowerCase())
                        .replace("MESSAGE", entity.getMessage())
        ));
        userService.freezeTime(userDTO, e.getDuration());
    }

    @Transactional
    @CacheEvict(cacheNames = "getParticipants", key = "#eventId")
    public void withdraw(Long eventId, UserDTO userDTO) throws NotFoundException, LogicException {
        EventEntity e = repository.findById(eventId).orElseThrow(NotFoundException::new);
        isMutable(e.isCancelled(), e.getEventDate());
        ParticipationEntity participation = participationRepository
                .findByOwner_IdAndEvent_Id(userDTO.getId(), eventId)
                .orElseThrow(NotFoundException::new);
        if (!ParticipationState.getOptions(participation.getState())
                .contains(ParticipationState.WITHDRAWN)) {
            throw new LogicException("Not withdrawable.");
        }
        participation.setState(ParticipationState.WITHDRAWN);
        participation.setUpdatedAt(LocalDateTime.now());
        participationRepository.save(participation);
        transactionService.insertTransaction(new TransactionRecord(
                TransactionType.PARTICIPATION_WITHDRAW,
                new Identifier(userDTO.getId(), Identifier.IdType.USER),
                new Identifier(e.getId(), Identifier.IdType.EVENT),
                "User withdrew their participation from TYPE."
                        .replace("TYPE", e.getType().name().toLowerCase())
        ));
        userService.unfreezeTime(userDTO, e.getDuration());
    }

    @Transactional
    @CacheEvict(cacheNames = "getParticipants", key = "#eventId")
    public void assign(Long eventId, UserDTO userDTO, ParticipationAssignmentRequest request) throws NotFoundException,
            LogicException {
        EventDTO event = repository.findById(eventId)
                .map(mapper::entityToDTO)
                .orElseThrow(NotFoundException::new);
        isMutable(event.isCancelled(), event.getEventDate());
        if (!event.getOwner().getId().equals(userDTO.getId()) ||
                request.getState().equals(ParticipationState.WITHDRAWN)) {
            throw new PermissionDeniedException();
        }
        ParticipationEntity participation = participationRepository
                .findByOwner_IdAndEvent_Id(request.getUserId(), eventId)
                .orElseThrow(NotFoundException::new);
        if (!ParticipationState.getOptions(participation.getState())
                .contains(request.getState())) {
            throw new LogicException("Not negotiable.");
        }
        ParticipationState oldState = participation.getState();
        participation.setState(request.getState());
        participation.setUpdatedAt(LocalDateTime.now());
        participationRepository.save(participation);
        transactionService.insertTransaction(new TransactionRecord(
                TransactionType.PARTICIPATION_WITHDRAW,
                new Identifier(eventId, Identifier.IdType.EVENT),
                new Identifier(request.getUserId(), Identifier.IdType.USER),
                "User updated participant's status from FROM to TO."
                        .replace("FROM", oldState.name().toLowerCase())
                        .replace("TO", request.getState().name().toLowerCase())
        ));
        if (request.getState() != ParticipationState.ACCEPTED) {
            userService.unfreezeTime(userMapper.entityToDto(participation.getOwner()), event.getDuration());
        }
    }
}
